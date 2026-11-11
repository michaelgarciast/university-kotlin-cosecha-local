package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.Order
import com.garcia.cosechalocal.data.model.OrderStatus

object SellerRepository {

    // Los pedidos siguen en memoria hasta la fase 5 (carrito y órdenes).
    fun getOrders(): List<Order> = listOf(
        Order(1042, "Laura Martínez", "2x Café de origen 500 g, 1x Miel de abejas 350 g", 88000, OrderStatus.PENDIENTE, "19 oct 2026"),
        Order(1041, "Carlos Rodríguez", "3x Panela orgánica 1 kg", 25500, OrderStatus.PENDIENTE, "18 oct 2026"),
        Order(1040, "Andrea Gómez", "1x Queso campesino 500 g, 2x Mora de castilla 1 kg", 34000, OrderStatus.PREPARANDO, "17 oct 2026"),
        Order(1039, "Julián Torres", "4x Café molido 250 g", 72000, OrderStatus.ENVIADO, "15 oct 2026"),
        Order(1038, "Marcela Ríos", "1x Café de origen 500 g", 32000, OrderStatus.ENTREGADO, "12 oct 2026"),
        Order(1037, "Santiago Pérez", "2x Miel de abejas 350 g, 2x Panela orgánica 1 kg", 65000, OrderStatus.ENTREGADO, "8 oct 2026")
    )
}
