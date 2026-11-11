package com.garcia.cosechalocal.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.repository.ProductRepository
import com.google.firebase.firestore.ListenerRegistration

// Catálogo del comprador: solo productos con estado APROBADO, tal como exigen
// las reglas de Firestore para este rol.
class CatalogViewModel : ViewModel() {

    private val _products = MutableLiveData<List<Product>>(emptyList())
    val products: LiveData<List<Product>> = _products

    private val _productsLoading = MutableLiveData(true)
    val productsLoading: LiveData<Boolean> = _productsLoading

    private val _productsError = MutableLiveData<Exception?>(null)
    val productsError: LiveData<Exception?> = _productsError

    private var registration: ListenerRegistration? = null

    init {
        registration = ProductRepository.observeApprovedProducts(
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

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }

    fun clearProductsError() {
        _productsError.value = null
    }

    fun filter(query: String, category: String): List<Product> {
        return _products.value.orEmpty().filter { product ->
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.producer.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
            val matchesCategory = category.equals("Todo", ignoreCase = true) ||
                    product.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }
}
