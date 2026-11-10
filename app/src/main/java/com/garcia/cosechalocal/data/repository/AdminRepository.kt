package com.garcia.cosechalocal.data.repository

import com.garcia.cosechalocal.data.model.BarEntry
import com.garcia.cosechalocal.data.model.ModerationStatus
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.model.ProductModeration
import com.garcia.cosechalocal.data.model.SalesPeriod
import com.garcia.cosechalocal.data.model.SalesReport
import com.garcia.cosechalocal.data.model.SellerSales

object AdminRepository {

    const val TOTAL_PRODUCTS = 842
    const val SALES_TODAY = 5_100_000

    fun getOrdersLast7Days(): List<BarEntry> = listOf(
        BarEntry("Lun", 42), BarEntry("Mar", 55), BarEntry("Mié", 38), BarEntry("Jue", 61),
        BarEntry("Vie", 74), BarEntry("Sáb", 88), BarEntry("Dom", 47)
    )

    private fun moderation(id: String, name: String, seller: String, price: Int, category: String, status: ModerationStatus) =
        ProductModeration(
            Product(id, name, seller, 0.0, price, 20, category, name),
            status,
            seller
        )

    fun getProducts(): List<ProductModeration> = listOf(
        moderation("a1", "Café de origen 500 g", "Finca El Cafetal", 32000, "Café", ModerationStatus.APROBADO),
        moderation("a2", "Café molido 250 g", "Finca El Cafetal", 18000, "Café", ModerationStatus.POR_APROBAR),
        moderation("a3", "Panela orgánica 1 kg", "Trapiche San José", 8500, "Panela", ModerationStatus.APROBADO),
        moderation("a4", "Panela con limón 500 g", "Trapiche San José", 7000, "Panela", ModerationStatus.POR_APROBAR),
        moderation("a5", "Miel de abejas 350 g", "Apiario La Colmena", 24000, "Miel", ModerationStatus.SUSPENDIDO),
        moderation("a6", "Miel con propóleo 250 g", "Apiario La Colmena", 28000, "Miel", ModerationStatus.POR_APROBAR),
        moderation("a7", "Queso campesino 500 g", "Lácteos del Valle", 15000, "Lácteos", ModerationStatus.APROBADO),
        moderation("a8", "Yogur natural 1 L", "Lácteos del Valle", 9500, "Lácteos", ModerationStatus.SUSPENDIDO)
    )

    fun getReport(period: SalesPeriod): SalesReport = when (period) {
        SalesPeriod.HOY -> SalesReport(
            5_100_000,
            listOf(BarEntry("6-10h", 600_000), BarEntry("10-14h", 1_700_000), BarEntry("14-18h", 1_900_000), BarEntry("18-22h", 900_000)),
            listOf(SellerSales("Finca El Cafetal", 1_800_000), SellerSales("Lácteos del Valle", 1_300_000), SellerSales("Trapiche San José", 900_000))
        )
        SalesPeriod.SIETE_DIAS -> SalesReport(
            31_800_000,
            listOf(
                BarEntry("Lun", 3_200_000), BarEntry("Mar", 4_100_000), BarEntry("Mié", 3_600_000), BarEntry("Jue", 4_800_000),
                BarEntry("Vie", 5_600_000), BarEntry("Sáb", 6_500_000), BarEntry("Dom", 4_000_000)
            ),
            listOf(SellerSales("Finca El Cafetal", 10_400_000), SellerSales("Trapiche San José", 7_900_000), SellerSales("Lácteos del Valle", 6_700_000))
        )
        SalesPeriod.MES -> SalesReport(
            128_400_000,
            listOf(BarEntry("Sem 1", 29_500_000), BarEntry("Sem 2", 33_200_000), BarEntry("Sem 3", 31_100_000), BarEntry("Sem 4", 34_600_000)),
            listOf(SellerSales("Finca El Cafetal", 41_300_000), SellerSales("Trapiche San José", 30_800_000), SellerSales("Lácteos del Valle", 27_100_000))
        )
    }
}
