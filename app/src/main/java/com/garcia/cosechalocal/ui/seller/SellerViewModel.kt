package com.garcia.cosechalocal.ui.seller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.model.OrderStatus
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.model.UserProfile
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.MissingProfileException
import com.garcia.cosechalocal.data.repository.ProductImageRepository
import com.garcia.cosechalocal.data.repository.ProductRepository
import com.garcia.cosechalocal.data.repository.SellerRepository
import com.garcia.cosechalocal.utils.ProductImageStore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class SellerViewModel(application: Application) : AndroidViewModel(application) {

    private val imageRepository = ProductImageRepository(application)

    private val _products = MutableLiveData<List<Product>>(emptyList())
    private val _productsLoading = MutableLiveData(true)
    private val _productsError = MutableLiveData<Exception?>(null)
    private var productsRegistration: ListenerRegistration? = null
    private var profile: UserProfile? = null

    private val _orders = MutableLiveData(SellerRepository.getOrders())
    private val query = MutableLiveData("")
    private val statusFilter = MutableLiveData<OrderStatus?>(null)

    val productsLoading: LiveData<Boolean> = _productsLoading
    val productsError: LiveData<Exception?> = _productsError

    init {
        val uid = AuthRepository.currentUser?.uid
        if (uid == null) {
            _productsLoading.value = false
            _productsError.value = MissingProfileException()
        } else {
            AuthRepository.loadCurrentProfile(
                onSuccess = { profile = it },
                onError = { _productsError.value = it }
            )
            productsRegistration = ProductRepository.observeSellerProducts(
                sellerId = uid,
                onChange = { list ->
                    _products.value = list
                    _productsLoading.value = false
                },
                onError = { error ->
                    _productsLoading.value = false
                    _productsError.value = error
                }
            )
        }
    }

    override fun onCleared() {
        productsRegistration?.remove()
        super.onCleared()
    }

    val filteredProducts: LiveData<List<Product>> = MediatorLiveData<List<Product>>().apply {
        fun update() {
            val q = query.value.orEmpty().trim()
            val list = _products.value.orEmpty()
            value = if (q.isEmpty()) list else list.filter {
                it.name.contains(q, true) || it.category.contains(q, true)
            }
        }
        addSource(_products) { update() }
        addSource(query) { update() }
    }

    val filteredOrders: LiveData<List<Order>> = MediatorLiveData<List<Order>>().apply {
        fun update() {
            val s = statusFilter.value
            val list = _orders.value.orEmpty()
            value = if (s == null) list else list.filter { it.status == s }
        }
        addSource(_orders) { update() }
        addSource(statusFilter) { update() }
    }

    val monthlySales: LiveData<Int> = _orders.map { list ->
        list.filter { it.status != OrderStatus.PENDIENTE }.sumOf { it.total }
    }
    val pendingOrders: LiveData<Int> = _orders.map { list ->
        list.count { it.status == OrderStatus.PENDIENTE }
    }
    val activeProducts: LiveData<Int> = _products.map { list ->
        list.count { it.stock > 0 }
    }

    fun setQuery(text: String) {
        query.value = text
    }

    fun setStatusFilter(status: OrderStatus?) {
        statusFilter.value = status
    }

    fun clearProductsError() {
        _productsError.value = null
    }

    fun loadProduct(productId: String, onResult: (Product?) -> Unit, onError: (Exception) -> Unit) =
        ProductRepository.getProduct(productId, onResult, onError)

    // imageBytes != null reemplaza la foto local; imageRemoved pide borrar la
    // existente. El BLOB se escribe en Room solo después de que Firestore
    // confirma el documento.
    fun saveProduct(
        editing: Product?,
        name: String,
        price: Int,
        stock: Int,
        category: String,
        description: String,
        imageBytes: ByteArray?,
        imageRemoved: Boolean,
        onSaved: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val seller = profile
        if (seller == null) {
            onError(MissingProfileException())
            return
        }
        val hasImage = imageBytes != null || (editing?.hasLocalImage == true && !imageRemoved)

        if (editing == null) {
            ProductRepository.createProduct(
                sellerId = seller.uid,
                sellerName = seller.name,
                name = name,
                price = price,
                stock = stock,
                category = category,
                description = description,
                hasLocalImage = hasImage,
                onSuccess = { newId ->
                    if (imageBytes != null) persistImage(newId, imageBytes)
                    onSaved()
                },
                onError = onError
            )
        } else {
            ProductRepository.updateProduct(
                productId = editing.id,
                name = name,
                price = price,
                stock = stock,
                category = category,
                description = description,
                hasLocalImage = hasImage,
                onSuccess = {
                    when {
                        imageBytes != null -> persistImage(editing.id, imageBytes)
                        imageRemoved && editing.hasLocalImage -> removeImage(editing.id)
                    }
                    onSaved()
                },
                onError = onError
            )
        }
    }

    fun deleteProduct(
        product: Product,
        onDeleted: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        ProductRepository.deleteProduct(
            productId = product.id,
            onSuccess = {
                if (product.hasLocalImage) removeImage(product.id)
                onDeleted()
            },
            onError = onError
        )
    }

    private fun persistImage(productId: String, imageData: ByteArray) {
        viewModelScope.launch {
            runCatching { imageRepository.saveImage(productId, imageData, "image/jpeg") }
            ProductImageStore.invalidate(productId)
        }
    }

    private fun removeImage(productId: String) {
        viewModelScope.launch {
            runCatching { imageRepository.deleteImage(productId) }
            ProductImageStore.invalidate(productId)
        }
    }

    fun getOrder(id: Int): Order? = _orders.value.orEmpty().firstOrNull { it.id == id }

    fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        _orders.value = _orders.value.orEmpty().map { if (it.id == orderId) it.copy(status = status) else it }
    }
}
