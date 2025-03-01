package com.example.hackusu2025

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hackusu2025.ui.theme.HackUSU2025Theme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


data class Ingredient(val name: String, var quantity: Int)
data class Recipe(val name: String, val url: String, val score: Double)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HackUSU2025Theme {
                NavigationScreen()
            }
        }
    }
}

@Composable
fun PopulateIngredientScreen(viewModel: FoodViewModel = viewModel(),
                             onFindRecipeButton: () -> Unit){
    val items = viewModel.ingredients

    Box(modifier = Modifier.fillMaxSize()){
    Scaffold(
        topBar = {
            TopMenuBar(
            onClearList = {
                viewModel.clearIngredients()
                Log.d("Ingredients Cleared", "All Items in List are ${items.toList().toString()}")
            }
        ) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            DynamicIngredientScreen(
                items,
                onAddItem = { newIngredient ->
                    viewModel.addIngredient(newIngredient)
                },
                onUpdateQuantity = {index, newQuantity ->
                    viewModel.updateIngredientQuantity(index, newQuantity)
                })
        }
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {

    FindRecipesButton(
        onFindRecipes = { onFindRecipeButton() })
        }
    }

}

@Composable
fun NavigationScreen() {
    val navController = rememberNavController()
    val viewModel: FoodViewModel = viewModel()
    var currentScreen by remember {mutableStateOf("populateIngredients")}

    NavHost(navController = navController, startDestination = "populateIngredients") {
        composable("populateIngredients") {
            PopulateIngredientScreen (
                viewModel = viewModel,
                onFindRecipeButton = { navController.navigate("recipeDisplay")}
            )
        }

        composable("recipeDisplay") {
            DisplayRecipeScreen(
                onReturnToIngredients = { navController.popBackStack() }
            )
        }

    }



    when (currentScreen) {
        "populateIngredients" -> PopulateIngredientScreen(
            viewModel = viewModel,
            onFindRecipeButton = { currentScreen = "recipeDisplay" }
        )

        "recipeDisplay" -> DisplayRecipeScreen(
            onReturnToIngredients = {currentScreen = "populateIngredients" })
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopMenuBar(onClearList: () -> Unit){
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("My App", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Clear Ingredient List") },
                    onClick = { onClearList() }
                )
                DropdownMenuItem(
                    text = { Text("Option 2") },
                    onClick = { menuExpanded = false }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Blue)
    )
}

@Composable
fun DynamicIngredientScreen(items: List<Ingredient>, onAddItem: (Ingredient) -> Unit, onUpdateQuantity: (Int, Int) -> Unit) {

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(items.size) { index ->
                IngredientScreen(item = items[index],
                    onQuantityChange = {newQuantity ->
                        Log.d("Ingredient Quantity Update", "${items[index].name} changed from ${items[index].quantity} to ${newQuantity}")
                        onUpdateQuantity(index, newQuantity)
                    }
                )}
            item {
                AddIngredientButton("null", onAddItem)
            }
        }

    }
}

@Composable
fun IngredientScreen(item: Ingredient, onQuantityChange: (Int) -> Unit) {

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
    ) {

        Text(text = item.name,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            textAlign = TextAlign.Start
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            //Decrement Button
            Button(onClick = { if (item.quantity > 1)
                onQuantityChange(item.quantity - 1)
            }){
                Text("-")
            }
            // Read-only quantity display
            TextField(
                value = item.quantity.toString(),
                onValueChange = {}, // No-op to prevent user editing
                readOnly = true, // Makes the field non-editable
                modifier = Modifier.padding(horizontal = 8.dp)
                    .width(70.dp)
            )
            Button(onClick = {
                onQuantityChange(item.quantity + 1)
            }) {
                Text("+")
            }



        }

    }

}

@Composable
fun AddIngredientButton(state: String, onAddItem: (Ingredient) -> Unit){
    var isExpanded by remember { mutableStateOf(false) }
    var ingredientName by remember { mutableStateOf("")}
    var quantity by remember { mutableStateOf(1) }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isExpanded){
        //this is the composable when the user is inputting a new ingredient

            Row{
                //input for ingredient name
                TextField(
                    value = ingredientName,
                    onValueChange = {ingredientName = it},
                    label = { Text("Ingredient Name")}
                )
                //input for quantity
                TextField(
                    value = quantity.toString(),
                    onValueChange = {newValue -> quantity = newValue.toIntOrNull() ?: 1},
                    label = { Text("Quantity") }
                )
            }

            Button(
                onClick = {
                    if (ingredientName.isNotBlank()) {
                        onAddItem(Ingredient(ingredientName, quantity))
                        ingredientName = ""
                        quantity = 1
                        isExpanded = false
                    }

                }
            )
            { Text("Add")}

        } else {
            //initial button
            Button(onClick = { isExpanded = true}) {
                Text("Add Ingredient")
            }
        }
    }
}

@Composable
fun FindRecipesButton(onFindRecipes: () -> Unit){
    Log.d("FindRecipesButton", "Rendered")

    Button (
            onClick = { onFindRecipes() },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(100.dp)
            )
        {
            Text("Find Recipes", fontSize = 24.sp)
    }
}


@Composable
fun DisplayRecipeScreen(onReturnToIngredients: ()-> Unit){
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)){
        LazyColumn()
        {



        }
    }
    Text("This screen displays Ingredients")
}


@Composable
fun RecipeScreen(recipe: Recipe){
    val context = LocalContext.current

    Button (
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.url))
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text("recipe", fontSize = 18.sp)
    }

}