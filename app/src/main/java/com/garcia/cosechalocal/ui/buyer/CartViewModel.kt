package com.garcia.cosechalocal.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.garcia.cosechalocal.data.model.CartItem
import com.garcia.cosechalocal.data.model.Product

class CartViewModel : ViewModel() {

    val shippingFee: Int = 7000

    private val _cartItems = MutableLiveData<List<CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private val _itemCount = MutableLiveData<Int>(0)
    val itemCount: LiveData<Int> get() = _itemCount

    private val _subtotal = MutableLiveData<Int>(0)
    val subtotal: LiveData<Int> get() = _subtotal

    private val _total = MutableLiveData<Int>(0)
    val total: LiveData<Int> get() = _total

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentList = _cartItems.value.orEmpty().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }

        if (existingIndex != -1) {
            val currentItem = currentList[existingIndex]
            val newQty = (currentItem.quantity + quantity).coerceAtMost(product.stock)
            currentList[existingIndex] = currentItem.copy(quantity = newQty)
        } else {
            val initialQty = quantity.coerceAtMost(product.stock)
            currentList.add(CartItem(product, initialQty))
        }

        _cartItems.value = currentList
        recalculateTotals(currentList)
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        val currentList = _cartItems.value.orEmpty().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == productId }

        if (existingIndex != -1) {
            val maxStock = currentList[existingIndex].product.stock
            if (newQuantity <= 0) {
                currentList.removeAt(existingIndex)
            } else {
                val clampedQty = newQuantity.coerceAtMost(maxStock)
                currentList[existingIndex] = currentList[existingIndex].copy(quantity = clampedQty)
            }
            _cartItems.value = currentList
            recalculateTotals(currentList)
        }
    }

    fun removeFromCart(productId: String) {
        val currentList = _cartItems.value.orEmpty().toMutableList()
        currentList.removeAll { it.product.id == productId }
        _cartItems.value = currentList
        recalculateTotals(currentList)
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        recalculateTotals(emptyList())
    }

    private fun recalculateTotals(items: List<CartItem>) {
        val totalCount = items.sumOf { it.quantity }
        val sub = items.sumOf { it.product.price * it.quantity }
        val tot = if (items.isNotEmpty()) sub + shippingFee else 0

        _itemCount.value = totalCount
        _subtotal.value = sub
        _total.value = tot
    }
}
