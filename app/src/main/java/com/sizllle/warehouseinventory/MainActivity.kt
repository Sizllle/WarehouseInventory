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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sizllle.warehouseinventory.data.WarehouseRepository
import com.sizllle.warehouseinventory.ui.AddProductScreen
import com.sizllle.warehouseinventory.ui.OrdersScreen
import com.sizllle.warehouseinventory.ui.WarehouseScreen
import com.sizllle.warehouseinventory.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = WarehouseRepository()

        setContent {
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
    }
}