package com.garcia.cosechalocal.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.ViewModel
import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.model.OrderStatus
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.repository.SellerRepository

class SellerViewModel : ViewModel() {

    private val _products = MutableLiveData(SellerRepository.getProducts())
    private val _orders = MutableLiveData(SellerRepository.getOrders())
    private val query = MutableLiveData("")
    private val statusFilter = MutableLiveData<OrderStatus?>(null)
    private var nextId = 100

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

    fun newProductId(): String = "s${nextId++}"

    fun getProduct(id: String?): Product? = _products.value.orEmpty().firstOrNull { it.id == id }

    fun getOrder(id: Int): Order? = _orders.value.orEmpty().firstOrNull { it.id == id }

    fun createProduct(product: Product) {
        _products.value = listOf(product) + _products.value.orEmpty()
    }

    fun updateProduct(product: Product) {
        _products.value = _products.value.orEmpty().map { if (it.id == product.id) product else it }
    }

    fun deleteProduct(id: String) {
        _products.value = _products.value.orEmpty().filterNot { it.id == id }
    }

    fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        _orders.value = _orders.value.orEmpty().map { if (it.id == orderId) it.copy(status = status) else it }
    }
}
