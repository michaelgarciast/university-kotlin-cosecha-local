package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.Product

object ProductRepository {

    private val sampleProducts = listOf(
        Product(
            id = "p1",
            name = "Café de origen 500 g",
            producer = "Finca El Cafetal",
            distanceKm = 3.2,
            price = 32000,
            stock = 15,
            category = "Café",
            description = "Café especial suave de origen antioqueño con notas a chocolate, caramelo y frutos rojos. Tostión media artesanal."
        ),
        Product(
            id = "p2",
            name = "Panela orgánica 1 kg",
            producer = "Trapiche San José",
            distanceKm = 5.1,
            price = 8500,
            stock = 30,
            category = "Panela",
            description = "Panela 100% natural y orgánica en pastilla, elaborada con caña de azúcar cultivada libre de agroquímicos."
        ),
        Product(
            id = "p3",
            name = "Miel de abejas 350 g",
            producer = "Apícola La Montaña",
            distanceKm = 2.8,
            price = 18000,
            stock = 12,
            category = "Miel",
            description = "Miel multifloral de abejas 100% pura, cosechada en los bosques nativos de la región con procesos sostenibles."
        ),
        Product(
            id = "p4",
            name = "Queso campesino 500 g",
            producer = "Lácteos Don Pedro",
            distanceKm = 4.5,
            price = 14000,
            stock = 20,
            category = "Lácteos",
            description = "Queso campesino fresco elaborado el mismo día con leche entera de vacas en pastoreo libre."
        ),
        Product(
            id = "p5",
            name = "Aguacate Hass 1 kg",
            producer = "Huerta Verde",
            distanceKm = 1.9,
            price = 12000,
            stock = 25,
            category = "Frutas",
            description = "Aguacates cremosos de calidad exportación, cultivados en las laderas de Santa Elena."
        ),
        Product(
            id = "p6",
            name = "Fresas frescas 500 g",
            producer = "Cultivos El Roble",
            distanceKm = 6.0,
            price = 9500,
            stock = 18,
            category = "Frutas",
            description = "Fresas rojas y dulces recién cosechadas de cultivos limpios en el oriente antioqueño."
        ),
        Product(
            id = "p7",
            name = "Yogurt artesanal 1 L",
            producer = "Lácteos Don Pedro",
            distanceKm = 4.5,
            price = 11000,
            stock = 10,
            category = "Lácteos",
            description = "Yogurt espeso y cremoso con trozos de fruta real, libre de conservantes y colorantes artificiales."
        ),
        Product(
            id = "p8",
            name = "Café tostado especial 250 g",
            producer = "Finca La Esperanza",
            distanceKm = 7.3,
            price = 22000,
            stock = 8,
            category = "Café",
            description = "Café de especialidad con perfil de taza limpio, fragancia intensa a jazmín y acidez brillante."
        )
    )

    fun getProducts(): List<Product> = sampleProducts

    fun getProductById(id: String): Product? = sampleProducts.find { it.id == id }

    fun filterProducts(query: String, category: String): List<Product> {
        return sampleProducts.filter { product ->
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
