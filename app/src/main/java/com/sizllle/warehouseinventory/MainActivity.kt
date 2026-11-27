package com.sizllle.warehouseinventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.sizllle.warehouseinventory.data.WarehouseRepository
import com.sizllle.warehouseinventory.ui.AddProductScreen
import com.sizllle.warehouseinventory.ui.OrdersScreen
import com.sizllle.warehouseinventory.ui.WarehouseScreen
import com.sizllle.warehouseinventory.R

class MainActivity : ComponentActivity() {
    // Removed @Preview and @Composable from onCreate method.
    // ComponentActivity's onCreate accepts a Bundle parameter which cannot be invoked by Compose Preview tooling,
    // causing "params don't add up to total params" IllegalStateException during preview rendering.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = WarehouseRepository()

        setContent {
            WarehouseInventoryApp(repository = repository)
        }
    }
}

// Extracted root UI into a standalone @Composable function for clean separation and preview support
@Composable
fun WarehouseInventoryApp(repository: WarehouseRepository = remember { WarehouseRepository() }) {
    MaterialTheme {
        var selectedTab by remember { mutableIntStateOf(0) }
        var refreshCounter by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            refreshCounter++
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_search),
                            contentDescription = null) },
                        label = { Text("Склад") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(painterResource(id = R.drawable.ic_add),
                            contentDescription = null) },
                        label = { Text("Добавить") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = {
                            selectedTab = 2
                            refreshCounter++
                        },
                        icon = { Icon(painterResource(id = R.drawable.ic_shopping_cart),
                            contentDescription = null) },
                        label = { Text("Закупки") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> WarehouseScreen(
                        repository = repository,
                        refreshTrigger = refreshCounter
                    )
                    1 -> AddProductScreen(
                        repository = repository,
                        onProductAdded = { refreshCounter++ }
                    )
                    2 -> OrdersScreen(
                        repository = repository,
                        refreshTrigger = refreshCounter
                    )
                }
            }
        }
    }
}

// Dedicated @Preview function without illegal parameters
@Preview(showBackground = true)
@Composable
fun WarehouseInventoryAppPreview() {
    WarehouseInventoryApp()
}