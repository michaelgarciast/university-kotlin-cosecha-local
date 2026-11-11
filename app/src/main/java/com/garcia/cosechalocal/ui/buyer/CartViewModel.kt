package com.garcia.cosechalocal.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.garcia.cosechalocal.data.model.CartItem
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.repository.AuthRepository
import com.garcia.cosechalocal.data.repository.CartRepository
import com.google.firebase.firestore.ListenerRegistration

class CartViewModel : ViewModel() {

    val shippingFee: Int = CartRepository.SHIPPING_FEE
    private var registration: ListenerRegistration? = null
    private var owner: String? = null
    private val _error = MutableLiveData<Exception?>()
    val error: LiveData<Exception?> = _error
    private val _loading = MutableLiveData(true)
    val loading: LiveData<Boolean> = _loading

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _itemCount = MutableLiveData<Int>(0)
    val itemCount: LiveData<Int> get() = _itemCount

    private val _subtotal = MutableLiveData<Int>(0)
    val subtotal: LiveData<Int> get() = _subtotal

    private val _total = MutableLiveData<Int>(0)
    val total: LiveData<Int> get() = _total

    fun connect() {
        val uid = AuthRepository.currentUser?.uid
        if (uid == owner) return
        registration?.remove()
        owner = uid
        _cartItems.value = emptyList()
        recalculateTotals(emptyList())
        if (uid == null) {
            _loading.value = false
            return
        }
        _loading.value = true
        registration = CartRepository.observe(uid, { items ->
            _cartItems.value = items
            recalculateTotals(items)
            _loading.value = false
        }, {
            _error.value = it
            _loading.value = false
        })
    }

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        connect()
        val uid = owner ?: return
        if (quantity > 0) CartRepository.change(uid, product.id, quantity) { _error.value = it }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        val uid = owner ?: return
        val old = _cartItems.value.orEmpty().firstOrNull { it.product.id == productId }?.quantity ?: return
        CartRepository.change(uid, productId, if (newQuantity <= 0) null else newQuantity - old) { _error.value = it }
    }

    fun removeFromCart(productId: String) {
        val uid = owner ?: return
        CartRepository.change(uid, productId, null) { _error.value = it }
    }

    fun clearError() { _error.value = null }

    private fun recalculateTotals(items: List<CartItem>) {
        val totalCount = items.sumOf { it.quantity }
        val sub = items.sumOf { it.product.price * it.quantity }
        _itemCount.value = totalCount
        _subtotal.value = sub
        _total.value = if (items.isNotEmpty()) sub + shippingFee else 0
    }
}
