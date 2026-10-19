package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.model.OrderStatus
import com.garcia.cosechalocal.data.model.Product

object SellerRepository {

    const val STORE_NAME = "Finca El Cafetal"

    private fun product(id: String, name: String, price: Int, stock: Int, category: String, description: String) =
        Product(id, name, STORE_NAME, 0.0, price, stock, category, description)

    fun getProducts(): List<Product> = listOf(
        product("s1", "Café de origen 500 g", 32000, 15, "Café", "Café especial suave con notas a chocolate y caramelo. Tostión media artesanal."),
        product("s2", "Café molido 250 g", 18000, 4, "Café", "Café molido fino, ideal para filtro y cafetera italiana."),
        product("s3", "Miel de abejas 350 g", 24000, 22, "Miel", "Miel pura de abejas, cosechada en panales de la finca."),
        product("s4", "Panela orgánica 1 kg", 8500, 40, "Panela", "Panela natural en pastilla, sin químicos."),
        product("s5", "Queso campesino 500 g", 15000, 3, "Lácteos", "Queso fresco campesino elaborado cada mañana."),
        product("s6", "Mora de castilla 1 kg", 9500, 12, "Frutas", "Mora fresca cosechada a mano, perfecta para jugos y postres.")
    )

    fun getOrders(): List<Order> = listOf(
        Order(1042, "Laura Martínez", "2x Café de origen 500 g, 1x Miel de abejas 350 g", 88000, OrderStatus.PENDIENTE, "19 oct 2026"),
        Order(1041, "Carlos Rodríguez", "3x Panela orgánica 1 kg", 25500, OrderStatus.PENDIENTE, "18 oct 2026"),
        Order(1040, "Andrea Gómez", "1x Queso campesino 500 g, 2x Mora de castilla 1 kg", 34000, OrderStatus.PREPARANDO, "17 oct 2026"),
        Order(1039, "Julián Torres", "4x Café molido 250 g", 72000, OrderStatus.ENVIADO, "15 oct 2026"),
        Order(1038, "Marcela Ríos", "1x Café de origen 500 g", 32000, OrderStatus.ENTREGADO, "12 oct 2026"),
        Order(1037, "Santiago Pérez", "2x Miel de abejas 350 g, 2x Panela orgánica 1 kg", 65000, OrderStatus.ENTREGADO, "8 oct 2026")
    )
}
