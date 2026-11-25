package com.sizllle.warehouseinventory.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.sizllle.warehouseinventory.data.SearchField
import com.sizllle.warehouseinventory.data.WarehouseRepository
import com.sizllle.warehouseinventory.model.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseScreen(
    repository: WarehouseRepository,
    refreshTrigger: Int = 0
) {
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var selectedField by remember { mutableStateOf(SearchField.BOX_CODE) }
    var fieldDropdownExpanded by remember { mutableStateOf(false) }

    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var isMarkingUsed by remember { mutableStateOf(false) }

    fun loadProducts() {
        scope.launch {
            isLoading = true
            errorText = null
            repository.getInStockProducts(query = query, searchField = selectedField)
                .onSuccess { products = it }
                .onFailure { errorText = it.localizedMessage ?: "Ошибка загрузки" }
            isLoading = false
        }
    }

    LaunchedEffect(query, selectedField, refreshTrigger) {
        loadProducts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Поиск товара") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = fieldDropdownExpanded,
                onExpandedChange = { fieldDropdownExpanded = !fieldDropdownExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedField.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Искать по") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = fieldDropdownExpanded,
                    onDismissRequest = { fieldDropdownExpanded = false }
                ) {
                    SearchField.entries.forEach { field ->
                        DropdownMenuItem(
                            text = { Text(field.label) },
                            onClick = {
                                selectedField = field
                                fieldDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedButton(onClick = { loadProducts() }) {
                Text("Обновить")
            }
        }

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
        } else if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Товаров не найдено")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProduct = item }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Коробка: ${item.boxCode}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Деталь: ${item.detailCode.ifBlank { "—" }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Стеллаж ${item.rack} — Полка ${item.shelf}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // Детальный экран товара в виде диалога
    val product = selectedProduct
    if (product != null) {
        AlertDialog(
            onDismissRequest = { if (!isMarkingUsed) selectedProduct = null },
            title = { Text("Карточка товара") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Код коробки: ${product.boxCode}", fontWeight = FontWeight.Bold)
                    Text("Код детали: ${product.detailCode.ifBlank { "Не указан" }}")
                    Text("Производитель: ${product.manufacturer.ifBlank { "Не указан" }}")
                    Text("Марка машины: ${product.carBrand.ifBlank { "Не указана" }}")
                    Text("Локация: Стеллаж ${product.rack} — Полка ${product.shelf}")
                    if (product.characteristics.isNotBlank()) {
                        Text("Характеристики:\n${product.characteristics}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isMarkingUsed = true
                        scope.launch {
                            repository.markAsUsed(product.id)
                                .onSuccess {
                                    isMarkingUsed = false
                                    selectedProduct = null
                                    loadProducts()
                                }
                                .onFailure {
                                    isMarkingUsed = false
                                    errorText = it.localizedMessage ?: "Ошибка списания"
                                    selectedProduct = null
                                }
                        }
                    },
                    enabled = !isMarkingUsed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (isMarkingUsed) "Списание..." else "Использовал")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { selectedProduct = null },
                    enabled = !isMarkingUsed
                ) {
                    Text("Закрыть")
                }
            }
        )
    }
}