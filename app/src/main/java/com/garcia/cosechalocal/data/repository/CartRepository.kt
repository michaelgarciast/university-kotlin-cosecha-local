package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.CartItem
import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.data.model.Product
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.UUID

object CartRepository {
    const val SHIPPING_FEE = 7000
    const val MAX_ITEMS = 5
    private val db = FirebaseFirestore.getInstance()

    fun observe(uid: String, onChange: (List<CartItem>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        db.collection("carts").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
            } else {
                val items = (snapshot?.get("items") as? List<*>).orEmpty().mapNotNull { raw ->
                    val row = raw as? Map<*, *> ?: return@mapNotNull null
                    val id = row["productId"] as? String ?: return@mapNotNull null
                    val quantity = (row["quantity"] as? Number)?.toInt() ?: return@mapNotNull null
                    if (quantity < 1) return@mapNotNull null
                    CartItem(
                        Product(
                            id, row["sellerId"] as? String ?: "", row["name"] as? String ?: "",
                            row["sellerName"] as? String ?: "", 0.0,
                            (row["unitPrice"] as? Number)?.toInt() ?: 0,
                            0, "", "", false, ModerationStatus.APROBADO
                        ), quantity
                    )
                }
                onChange(items)
            }
        }

    fun change(uid: String, productId: String, delta: Int?, onError: (Exception) -> Unit) {
        val cart = db.collection("carts").document(uid)
        val product = db.collection("products").document(productId)
        db.runTransaction { tx ->
            val current = tx.get(cart)
            val fresh = if (delta != null) tx.get(product) else null
            val items = (current.get("items") as? List<*>).orEmpty()
                .mapNotNull { raw ->
                    val row = raw as? Map<*, *> ?: return@mapNotNull null
                    val id = row["productId"] as? String ?: return@mapNotNull null
                    id to row
                }.toMap().toMutableMap()
            if (delta == null) {
                items.remove(productId)
            } else {
                val old = (items[productId]?.get("quantity") as? Number)?.toInt() ?: 0
                val quantity = old + delta
                if (quantity <= 0) items.remove(productId)
                else {
                    val available = fresh ?: throw IllegalStateException("Producto no disponible")
                    val stock = (available.getLong("stock") ?: 0).toInt()
                    if (!available.exists() || available.getString("status") != "APROBADO" ||
                        quantity > stock || stock < 1) {
                        throw IllegalStateException("Producto no disponible o stock insuficiente")
                    }
                    if (!items.containsKey(productId) && items.size >= MAX_ITEMS) {
                        throw IllegalStateException("Máximo $MAX_ITEMS productos por pedido")
                    }
                    items[productId] = mapOf(
                        "productId" to productId,
                        "sellerId" to available.getString("sellerId").orEmpty(),
                        "sellerName" to available.getString("sellerName").orEmpty(),
                        "name" to available.getString("name").orEmpty(),
                        "unitPrice" to (available.getLong("price") ?: 0),
                        "quantity" to quantity
                    )
                }
            }
            tx.set(cart, mapOf(
                "items" to items.values.toList(),
                "shippingFee" to SHIPPING_FEE,
                "checkoutId" to (current.getString("checkoutId") ?: UUID.randomUUID().toString()),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            null
        }.addOnFailureListener(onError)
    }
}
