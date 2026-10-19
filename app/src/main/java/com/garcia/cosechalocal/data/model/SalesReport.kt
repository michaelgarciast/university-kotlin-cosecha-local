package com.garcia.cosechalocal.data.model

enum class SalesPeriod { HOY, SIETE_DIAS, MES }

data class BarEntry(val label: String, val value: Int)

data class SellerSales(val name: String, val amount: Int)

data class SalesReport(
    val totalSales: Int,
    val bars: List<BarEntry>,
    val topSellers: List<SellerSales>
) {
    val commission: Int get() = totalSales / 10
}
