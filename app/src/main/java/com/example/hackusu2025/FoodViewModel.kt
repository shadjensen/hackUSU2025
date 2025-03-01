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

    private val _recipes = mutableStateListOf<Recipe>()
    val recipes: List<Recipe> get() = _recipes

    fun addRecipe(recipe: Recipe) {
        _recipes.add(recipe)
    }

    fun clearRecipeList() {
        _recipes.clear()
    }

    fun setRecipes(newList: MutableList<Recipe>) {
        _recipes.clear()
        _recipes.addAll(newList)
    }

}