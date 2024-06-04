package com.orangeskystorm.kithenhelper.ui.home

data class RecepiesCategoriesResponse (
    val categories: Array<RecepieCategories>
)

data class RecepieCategories(
    val idCategory: String,
    val strCategory: String,
    val strCategoryThumb: String,
    val strCategoryDescription: String,
)
