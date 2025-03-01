package com.example.hackusu2025

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class FoodViewModel : ViewModel() {

    private val _ingredients = mutableStateListOf<Ingredient>()
    val ingredients: List<Ingredient> get() = _ingredients


    fun addIngredient(igredient: Ingredient) {
        _ingredients.add(igredient)
    }

    fun updateIngredientQuantity(index: Int, newQuantity: Int) {
        _ingredients[index] = _ingredients[index].copy(quantity = newQuantity)
    }

    fun clearIngredients() {
        _ingredients.clear()
    }

}