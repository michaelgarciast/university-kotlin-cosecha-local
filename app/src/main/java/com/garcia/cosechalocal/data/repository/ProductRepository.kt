package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.data.model.Product
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

object ProductRepository {

    private val products = FirebaseFirestore.getInstance().collection("products")

    private fun DocumentSnapshot.toProduct(): Product? {
        val status = runCatching {
            ModerationStatus.valueOf(getString("status").orEmpty())
        }.getOrNull() ?: return null

        return Product(
            id = id,
            sellerId = getString("sellerId").orEmpty(),
            name = getString("name").orEmpty(),
            producer = getString("sellerName").orEmpty(),
            distanceKm = 0.0,
            price = getLong("price")?.toInt() ?: 0,
            stock = getLong("stock")?.toInt() ?: 0,
            category = getString("category").orEmpty(),
            description = getString("description").orEmpty(),
            hasLocalImage = getBoolean("hasLocalImage") == true,
            status = status
        )
    }

    private fun observe(
        query: Query,
        onChange: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = query.addSnapshotListener { snapshot, error ->
        if (error != null) {
            onError(error)
            return@addSnapshotListener
        }
        val list = snapshot?.documents.orEmpty()
            .mapNotNull { it.toProduct() }
            .sortedBy { it.name.lowercase() }
        onChange(list)
    }

    fun observeSellerProducts(
        sellerId: String,
        onChange: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = observe(products.whereEqualTo("sellerId", sellerId), onChange, onError)

    fun observeApprovedProducts(
        onChange: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = observe(
        products.whereEqualTo("status", ModerationStatus.APROBADO.name),
        onChange,
        onError
    )

    fun observeAllProducts(
        onChange: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration = observe(products, onChange, onError)

    fun getProduct(
        productId: String,
        onSuccess: (Product?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        products.document(productId).get()
            .addOnSuccessListener { document ->
                onSuccess(if (document.exists()) document.toProduct() else null)
            }
            .addOnFailureListener(onError)
    }

    fun createProduct(
        sellerId: String,
        sellerName: String,
        name: String,
        price: Int,
        stock: Int,
        category: String,
        description: String,
        hasLocalImage: Boolean,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "sellerId" to sellerId,
            "sellerName" to sellerName,
            "name" to name,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "description" to description,
            "hasLocalImage" to hasLocalImage,
            "status" to ModerationStatus.POR_APROBAR.name,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        products.add(data)
            .addOnSuccessListener { onSuccess(it.id) }
            .addOnFailureListener(onError)
    }

    fun updateProduct(
        productId: String,
        name: String,
        price: Int,
        stock: Int,
        category: String,
        description: String,
        hasLocalImage: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        products.document(productId).update(
            mapOf(
                "name" to name,
                "price" to price,
                "stock" to stock,
                "category" to category,
                "description" to description,
                "hasLocalImage" to hasLocalImage,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun setStatus(
        productId: String,
        status: ModerationStatus,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        products.document(productId).update(
            mapOf(
                "status" to status.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        products.document(productId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }
}
