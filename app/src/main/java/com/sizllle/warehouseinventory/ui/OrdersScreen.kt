package com.sizllle.warehouseinventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sizllle.warehouseinventory.data.WarehouseRepository
import kotlinx.coroutines.launch

data class OrderGroupItem(
    val boxCode: String,
    val detailCode: String,
    val count: Int
)

@Composable
fun OrdersScreen(
    repository: WarehouseRepository,
    refreshTrigger: Int = 0
) {
    val scope = rememberCoroutineScope()

    var orderItems by remember { mutableStateOf<List<OrderGroupItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var busyBoxCode by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun loadOrders() {
        scope.launch {
            isLoading = true
            errorText = null
            repository.getToOrderProducts()
                .onSuccess { list ->
                    val grouped = list
                        .groupBy { it.boxCode }
                        .map { (boxCode, itemsInGroup) ->
                            val firstDetail = itemsInGroup.firstOrNull { it.detailCode.isNotBlank() }?.detailCode.orEmpty()
                            OrderGroupItem(
                                boxCode = boxCode,
                                detailCode = firstDetail,
                                count = itemsInGroup.size
                            )
                        }
                        .sortedBy { it.boxCode }
                    orderItems = grouped
                }
                .onFailure {
                    errorText = it.localizedMessage ?: "Ошибка загрузки списка закупок"
                }
            isLoading = false
        }
    }

    LaunchedEffect(refreshTrigger) {
        loadOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Товары к заказу",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (errorText != null) {
            Text(
                text = errorText!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (orderItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Список закупок пуст")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(orderItems, key = { it.boxCode }) { group ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Коробка: ${group.boxCode}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Деталь: ${group.detailCode.ifBlank { "—" }}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "К заказу: ${group.count} шт.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    busyBoxCode = group.boxCode
                                    scope.launch {
                                        repository.markAsOrderedByBoxCode(group.boxCode)
                                            .onSuccess {
                                                busyBoxCode = null
                                                loadOrders()
                                            }
                                            .onFailure {
                                                busyBoxCode = null
                                                errorText = it.localizedMessage ?: "Ошибка удаления"
                                            }
                                    }
                                },
                                enabled = busyBoxCode == null
                            ) {
                                Text(if (busyBoxCode == group.boxCode) "..." else "Заказал")
                            }
                        }
                    }
                }
            }
        }
    }
}