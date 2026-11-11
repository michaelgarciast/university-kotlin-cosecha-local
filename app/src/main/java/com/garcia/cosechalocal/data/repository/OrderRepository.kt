package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.model.OrderStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.DateFormat
import java.util.UUID

object OrderRepository {
    private val db = FirebaseFirestore.getInstance()
    private val orders = db.collection("orders")

    fun checkout(
        uid: String,
        address: String,
        location: Map<String, Any>? = null,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val cartRef = db.collection("carts").document(uid)
        val buyerRef = db.collection("users").document(uid)
        db.runTransaction { tx ->
            val cart = tx.get(cartRef)
            val buyer = tx.get(buyerRef)
            val items = (cart.get("items") as? List<*>).orEmpty().map {
                @Suppress("UNCHECKED_CAST")
                it as? Map<String, Any> ?: throw IllegalStateException("Carrito inválido")
            }
            if (items.isEmpty() || items.size > CartRepository.MAX_ITEMS) {
                throw IllegalStateException("El carrito está vacío o supera el límite")
            }
            val checkoutId = cart.getString("checkoutId") ?: throw IllegalStateException("Recarga el carrito")
            val orderRef = orders.document(checkoutId)
            val existing = tx.get(orderRef)
            if (existing.exists()) return@runTransaction checkoutId
            val products = items.map { row ->
                val id = row["productId"] as? String ?: throw IllegalStateException("Producto inválido")
                tx.get(db.collection("products").document(id))
            }
            var subtotal = 0L
            val orderItems = items.zip(products).map { (row, product) ->
                val qty = (row["quantity"] as? Number)?.toLong() ?: 0L
                val price = product.getLong("price") ?: 0L
                if ((row["unitPrice"] as? Number)?.toLong() != price) {
                    throw IllegalStateException("El precio cambió. Quita y agrega el producto de nuevo")
                }
                val stock = product.getLong("stock") ?: 0L
                if (!product.exists() || product.getString("status") != "APROBADO" ||
                    qty < 1 || qty > stock || price < 0) {
                    throw IllegalStateException("Stock cambiado: revisa tu carrito")
                }
                val line = Math.multiplyExact(qty, price)
                subtotal = Math.addExact(subtotal, line)
                mapOf(
                    "productId" to product.id, "sellerId" to product.getString("sellerId").orEmpty(),
                    "name" to product.getString("name").orEmpty(), "unitPrice" to price,
                    "quantity" to qty, "subtotal" to line
                )
            }
            val fee = CartRepository.SHIPPING_FEE.toLong()
            val orderData = mutableMapOf<String, Any>(
                "buyerId" to uid, "buyerName" to buyer.getString("name").orEmpty(),
                "items" to orderItems,
                "sellerIds" to orderItems.map { it["sellerId"] as String }.distinct(),
                "deliveryAddress" to address, "subtotal" to subtotal,
                "shippingFee" to fee, "total" to Math.addExact(subtotal, fee),
                "paymentStatus" to "PENDIENTE_PAGO", "deliveryStatus" to "PENDIENTE",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            location?.let { orderData["location"] = it }
            tx.set(orderRef, orderData)
            tx.set(cartRef, mapOf(
                "items" to emptyList<Any>(), "shippingFee" to CartRepository.SHIPPING_FEE,
                "checkoutId" to UUID.randomUUID().toString(),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            checkoutId
        }.addOnSuccessListener(onSuccess).addOnFailureListener(onError)
    }

    fun observeBuyer(uid: String, onChange: (List<Order>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        listen(orders.whereEqualTo("buyerId", uid), null, onChange, onError)

    fun observeSeller(uid: String, onChange: (List<Order>) -> Unit, onError: (Exception) -> Unit): ListenerRegistration =
        listen(orders.whereArrayContains("sellerIds", uid), uid, onChange, onError)

    private fun listen(
        query: com.google.firebase.firestore.Query,
        sellerId: String?,
        onChange: (List<Order>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = query.addSnapshotListener { snapshot, error ->
        if (error != null) onError(error)
        else onChange(snapshot?.documents.orEmpty().mapNotNull { it.toOrder(sellerId) }
            .sortedByDescending { it.createdAt })
    }

    private fun DocumentSnapshot.toOrder(sellerId: String?): Order? {
        val rows = (get("items") as? List<*>).orEmpty().mapNotNull { it as? Map<*, *> }
        val visible = if (sellerId == null) rows else rows.filter { it["sellerId"] == sellerId }
        if (visible.isEmpty()) return null
        val time = getTimestamp("createdAt")?.toDate()
        return Order(
            id = id, buyerName = getString("buyerName").orEmpty(),
            itemsSummary = visible.joinToString(", ") { "${it["quantity"]}x ${it["name"]}" },
            total = visible.sumOf { (it["subtotal"] as? Number)?.toLong() ?: 0L }.toInt() +
                if (sellerId == null) (getLong("shippingFee") ?: 0).toInt() else 0,
            status = runCatching { OrderStatus.valueOf(getString("deliveryStatus").orEmpty()) }
                .getOrDefault(OrderStatus.PENDIENTE),
            date = time?.let { DateFormat.getDateInstance().format(it) }.orEmpty(),
            paymentStatus = getString("paymentStatus").orEmpty(),
            address = getString("deliveryAddress").orEmpty(),
            createdAt = time?.time ?: 0L
        )
    }

    fun cancel(orderId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        orders.document(orderId).update(
            mapOf("paymentStatus" to "CANCELADO", "updatedAt" to FieldValue.serverTimestamp())
        ).addOnSuccessListener { onSuccess() }.addOnFailureListener(onError)
    }
}
