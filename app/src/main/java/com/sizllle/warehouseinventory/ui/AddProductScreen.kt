package com.sizllle.warehouseinventory.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sizllle.warehouseinventory.data.WarehouseRepository
import com.sizllle.warehouseinventory.model.Product
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    repository: WarehouseRepository,
    onProductAdded: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    var boxCode by remember { mutableStateOf("") }
    var detailCode by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var carBrand by remember { mutableStateOf(Product.CAR_BRANDS.first()) }
    var carBrandExpanded by remember { mutableStateOf(false) }
    var characteristics by remember { mutableStateOf("") }

    var rack by remember { mutableIntStateOf(1) }
    var rackExpanded by remember { mutableStateOf(false) }

    var shelf by remember { mutableIntStateOf(1) }
    var shelfExpanded by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Добавить товар на склад",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = boxCode,
            onValueChange = {
                boxCode = it
                statusMessage = null
            },
            label = { Text("Код коробки *") },
            singleLine = true,
            isError = isError && boxCode.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = detailCode,
            onValueChange = { detailCode = it },
            label = { Text("Код детали") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text("Производитель") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Выпадающий список марок авто
        ExposedDropdownMenuBox(
            expanded = carBrandExpanded,
            onExpandedChange = { carBrandExpanded = !carBrandExpanded }
        ) {
            OutlinedTextField(
                value = carBrand,
                onValueChange = {},
                readOnly = true,
                label = { Text("Марка машины") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = carBrandExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = carBrandExpanded,
                onDismissRequest = { carBrandExpanded = false }
            ) {
                Product.CAR_BRANDS.forEach { brand ->
                    DropdownMenuItem(
                        text = { Text(brand) },
                        onClick = {
                            carBrand = brand
                            carBrandExpanded = false
                        }
                    )
                }
            }
        }

        // Стеллаж (1..8) и Полка (1..9)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = rackExpanded,
                onExpandedChange = { rackExpanded = !rackExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = "Стеллаж $rack",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Стеллаж *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rackExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = rackExpanded,
                    onDismissRequest = { rackExpanded = false }
                ) {
                    (1..8).forEach { r ->
                        DropdownMenuItem(
                            text = { Text("Стеллаж $r") },
                            onClick = {
                                rack = r
                                rackExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = shelfExpanded,
                onExpandedChange = { shelfExpanded = !shelfExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = "Полка $shelf",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Полка *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shelfExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = shelfExpanded,
                    onDismissRequest = { shelfExpanded = false }
                ) {
                    (1..9).forEach { s ->
                        DropdownMenuItem(
                            text = { Text("Полка $s") },
                            onClick = {
                                shelf = s
                                shelfExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = characteristics,
            onValueChange = { characteristics = it },
            label = { Text("Характеристики") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        if (statusMessage != null) {
            Text(
                text = statusMessage!!,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                if (boxCode.isBlank()) {
                    isError = true
                    statusMessage = "Укажите код коробки"
                    return@Button
                }
                isError = false
                isSaving = true
                statusMessage = null

                scope.launch {
                    val result = repository.addProduct(
                        boxCode = boxCode,
                        detailCode = detailCode,
                        manufacturer = manufacturer,
                        carBrand = carBrand,
                        characteristics = characteristics,
                        rack = rack,
                        shelf = shelf
                    )
                    isSaving = false
                    result.onSuccess {
                        boxCode = ""
                        detailCode = ""
                        manufacturer = ""
                        carBrand = Product.CAR_BRANDS.first()
                        characteristics = ""
                        statusMessage = "Товар добавлен на склад"
                        onProductAdded()
                    }.onFailure { err ->
                        isError = true
                        statusMessage = "Ошибка: ${err.localizedMessage ?: "не удалось сохранить"}"
                    }
                }
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(if (isSaving) "Сохранение..." else "Сохранить товар")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}