package com.example.hackusu2025

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hackusu2025.ui.theme.HackUSU2025Theme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


data class Ingredient(val name: String, var quantity: Int, var unit: String)
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
                },
                onUpdateUnit = { index, newUnit ->
                    viewModel.updateIngredientUnit(index, newUnit)
                },
                onSpecifyQuantity = { index, checked ->
                    Log.d("Specify Quantity", "Specify quantity was checked at ${index} with ${checked}")
                    if (checked) {
                        viewModel.updateIngredientUnit(index, "count")
                        viewModel.updateIngredientQuantity(index, 1)
                    } else {
                        viewModel.updateIngredientQuantity(index, -1)
                        viewModel.updateIngredientUnit(index, "count")
                    }
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

    NavHost(navController = navController, startDestination = "populateIngredients") {
        composable("populateIngredients") {
            PopulateIngredientScreen (
                viewModel = viewModel,
                onFindRecipeButton = { navController.navigate("recipeDisplay")}
            )
        }

        composable("recipeDisplay") {
            DisplayRecipeScreen(
                viewModel,
                onReturnToIngredients = { navController.popBackStack() }
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopMenuBar(onClearList: () -> Unit){
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("SYVR", color = Color.White) },
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
fun DynamicIngredientScreen(items: List<Ingredient>, onAddItem: (Ingredient) -> Unit, onUpdateQuantity: (Int, Int) -> Unit, onUpdateUnit: (Int, String) -> Unit, onSpecifyQuantity: (Int, Boolean) -> Unit) {

    val units = mutableListOf("teaspoon", "tablespoon", "fluid ounce", "cup", "pint", "quart", "gallon", "count")

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(items.size) { index ->
                IngredientScreen(
                    index = index,
                    item = items[index],
                    units = units.toList(),
                    onQuantityChange = {newQuantity ->
                        Log.d("Ingredient Quantity Update", "${items[index].name} changed from ${items[index].quantity} to ${newQuantity}")
                        onUpdateQuantity(index, newQuantity)
                    },
                    onUnitSelected = { newUnit ->
                        onUpdateUnit(index, newUnit)
                    },
                    onSpecifyQuantity = { index, bool ->
                        onSpecifyQuantity(index, bool)
                    }
                )}
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                AddIngredientButton("null", onAddItem)
                }
            }
        }

    }
}

@Composable
fun IngredientScreen(index: Int,
                     item: Ingredient,
                     units: List<String>,
                     onQuantityChange: (Int) -> Unit,
                     onUnitSelected: (String) -> Unit,
                     onSpecifyQuantity: (Int, Boolean) -> Unit
) {
    var specifyQuantity by remember { mutableStateOf(true)}
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember {mutableStateOf("gallon")}

    Column(modifier = Modifier.padding(16.dp)){



        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = item.name,
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                textAlign = TextAlign.Start
            )

            if (specifyQuantity) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    //Decrement Button
                    Button(onClick = {
                        if (item.quantity > 1)
                            onQuantityChange(item.quantity - 1)
                    }) {
                        Text("-")
                    }
                    // Read-only quantity display
                    TextField(
                        value = item.quantity.toString(),
                        onValueChange = {}, // No-op to prevent user editing
                        readOnly = true, // Makes the field non-editable
                        modifier = Modifier.padding(horizontal = 8.dp)
                            .width(75.dp)
                    )
                    Button(onClick = {
                        onQuantityChange(item.quantity + 1)
                    }) {
                        Text("+")
                    }
                }

            }
        }

        Row (modifier = Modifier
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clickable { expanded = true  }
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(text = selectedUnit, modifier = Modifier.padding(8.dp))

                DropdownMenu(expanded = expanded, onDismissRequest = {expanded = false}
                ) {
                    units.forEach {unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                selectedUnit = unit
                                onUnitSelected(unit)
                                expanded = false
                            }

                        )
                    }
                }

            }

            //specify quantity checkbox

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = specifyQuantity,
                    onCheckedChange = { isChecked ->
                        specifyQuantity = isChecked
                        onSpecifyQuantity(index, isChecked)  }
                )
                Text("Specify Quantity", modifier = Modifier.padding(start = 4.dp))

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
        if (!isExpanded){
            //initial button
            Button(onClick = { isExpanded = true}) {
                Text("Add Ingredient", modifier = Modifier.fillMaxWidth())
            }
        } else {
        //this is the composable when the user is inputting a new ingredient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column (modifier = Modifier.padding(16.dp))
            {
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            //input for ingredient name
                            TextField(
                                value = ingredientName,
                                onValueChange = {ingredientName = it},
                                label = { Text("Ingredient Name")},
                                modifier = Modifier
                                    .weight(2f)
                                    .heightIn(min = 56.dp)
                            )
                            //input for quantity
                            TextField(
                                value = quantity.toString(),
                                onValueChange = {newValue -> quantity = newValue.toIntOrNull() ?: 1},
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 56.dp)
                            )
                        }
                    }
                }


                Button(
                    onClick = {
                        if (ingredientName.isNotBlank()) {
                            onAddItem(Ingredient(ingredientName, quantity, "count"))
                            ingredientName = ""
                            quantity = 1
                            isExpanded = false
                        }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                { Text("Add")}



            }
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
fun DisplayRecipeScreen(viewModel: FoodViewModel,
                        onReturnToIngredients: ()-> Unit){
    var selectedCategory by remember { mutableStateOf("") }
    val foodCategories = mutableListOf(
        "",
        "Desserts",
        "Breakfast",
        "Meat-Based",
        "Pasta & Italian",
        "Seafood",
        "Soups & Stews",
        "Vegetarian & Vegan",
        "Baking & Breads",
        "Mexican",
        "Asian",
        "Slow Cooker & Instant Pot",
        "Healthy & Diet-Friendly",
        "Drinks & Cocktails",
        "Holiday & Seasonal"
    )

    Log.d("Conversion", "Begin converting input to output")
    var convertedIngredients = ArrayList<Tuple>()
    viewModel.ingredients.forEach{
        convertedIngredients.add(Tuple(it.name, it.quantity * 1.0, it.unit))
    }

    val context : Context = LocalContext.current
    val returnRecipes = ReadIngredients.getBestFits(convertedIngredients, 1, selectedCategory, context)
    Log.d("Conversion", "Successfully generated recipe list")


    val recipes = mutableListOf(Recipe("", "", 1.0))
    recipes.clear()

    returnRecipes.forEach{ item ->
        val parts = item.split(", ")
        val score = parts[0].toDouble()
        val url = parts[1]

        val name = extractRecipeName(url)

        recipes.add(Recipe(name, url, score))
    }

    Log.d("Conversion", "Finished creating recipe objects")
    recipes.sortByDescending { it.score }

    viewModel.setRecipes(recipes)
    Log.d("Conversion", "Assigned recipes to view model")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ){
        DoubleTopBar(dropDownItems = foodCategories.toList(),
            onItemSelected = {item -> selectedCategory = item} ,
            searchText = "",
            onSearchTextChange = {item -> Log.d("Search", "search value changed")})

        viewModel.recipes.forEach { recipe ->
            RecipeScreen(recipe)
        }
    }
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
        Text(recipe.name, fontSize = 18.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubleTopBar(
    dropDownItems: List<String>,
    onItemSelected: (String) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit)
{
    var selectedItem by remember {mutableStateOf("")}
    var expanded by remember {mutableStateOf(false)}
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Column {
        TopAppBar(
            title = { Text("Category", color = Color.White) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp),
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            actions = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { expanded = true }
                ) {
                    Text(
                        text = selectedItem,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                            .background(Color.Gray.copy(alpha = 0.2f))  // Make the dropdown a little lighter
                        ,


                    ) {


                        dropDownItems.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    onItemSelected(item)
                                    expanded = false
                                    selectedItem = item
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

//this function was generated by ChatGPT
fun extractRecipeName(url: String): String {
    // Regular expression to capture the recipe name from the URL
    val regex = """/recipe/(\d+)/(.+)""".toRegex()

    val matchResult = regex.find(url)
    return matchResult
        ?.groupValues
        ?.get(2)
        ?.dropLast(1)
        ?.replace("-", " ")?.toLowerCase() ?: "Unknown"
}