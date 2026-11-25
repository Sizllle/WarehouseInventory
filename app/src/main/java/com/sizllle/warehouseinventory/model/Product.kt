package com.sizllle.warehouseinventory.model

data class Product(
    val id: String = "",
    val boxCode: String = "",
    val detailCode: String = "",
    val manufacturer: String = "",
    val carBrand: String = "",
    val characteristics: String = "",
    val rack: Int = 1,
    val shelf: Int = 1,
    val status: String = STATUS_IN_STOCK,
    val searchKeywords: List<String> = emptyList()
    ) {
    companion object {
        const val STATUS_IN_STOCK = "in_stock"
        const val STATUS_TO_ORDER = "to_order"

        val CAR_BRANDS = listOf(
            "Не указано",
            "Audi",
            "BMW",
            "Mercedes-Benz",
            "Volkswagen",
            "Toyota",
            "Hyundai",
            "Honda",
            "Kia",
            "Ford",
            "Skoda",
            "Renault",
            "Ланос",
            "Сэнс",
            "Жигуль",
            "Таврия",
            "Другая"
        )
    }
}