package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.SalesViewModel
import com.example.ui.theme.SlushieCyanSecondary
import com.example.ui.theme.SlushiePinkPrimary
import com.example.util.Formatters

@Composable
fun InventoryScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val outOfStockProducts by viewModel.outOfStockProducts.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf(ProductCategory.ALL) }
    var filterOnlyAlertStock by remember { mutableStateOf(false) }

    // Dialog States
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showLowStockAlertModal by remember { mutableStateOf(false) }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.nameKh.contains(searchQuery, ignoreCase = true) ||
                product.id.contains(searchQuery, ignoreCase = true)
        val matchesCategory = (selectedFilterCategory == ProductCategory.ALL) || (product.category == selectedFilterCategory)
        val matchesAlert = if (filterOnlyAlertStock) (product.isOutOfStock || product.isLowStock) else true
        matchesSearch && matchesCategory && matchesAlert
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header / KPI Summary Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Stock Alert Summary Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Out of stock card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                filterOnlyAlertStock = !filterOnlyAlertStock
                            }
                            .testTag("kpi_out_of_stock_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (outOfStockProducts.isNotEmpty()) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (outOfStockProducts.isNotEmpty()) Color(0xFFFCA5A5) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "អស់ស្តុក",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${outOfStockProducts.size} មុខ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Low stock card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                filterOnlyAlertStock = !filterOnlyAlertStock
                            }
                            .testTag("kpi_low_stock_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (lowStockProducts.isNotEmpty()) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (lowStockProducts.isNotEmpty()) Color(0xFFFCD34D) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "សល់តិច",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${lowStockProducts.size} មុខ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }

                    // Total inventory items
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ទំនិញសរុប",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${products.size} មុខ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Alert summary banner if low/out of stock exists
                if (outOfStockProducts.isNotEmpty() || lowStockProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLowStockAlertModal = true }
                            .testTag("low_stock_alert_summary_banner"),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF7ED),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDBA74))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚠️ មាន ${outOfStockProducts.size + lowStockProducts.size} មុខទំនិញត្រូវបន្ថែមស្តុកបន្ទាន់!",
                                    color = Color(0xFFC2410C),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "មើលលម្អិត >",
                                color = Color(0xFFEA580C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input"),
                    placeholder = { Text("ស្វែងរកឈ្មោះ ឬលេខកូដទំនិញ...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category and Alert Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (filterOnlyAlertStock) Color(0xFFF97316) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (filterOnlyAlertStock) Color(0xFFEA580C) else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { filterOnlyAlertStock = !filterOnlyAlertStock }
                                .testTag("filter_chip_alert_only")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (filterOnlyAlertStock) Color.White else Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ទំនិញសល់តិច/អស់ស្តុក",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (filterOnlyAlertStock) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    items(ProductCategory.values()) { cat ->
                        val isSelected = (selectedFilterCategory == cat) && !filterOnlyAlertStock
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    selectedFilterCategory = cat
                                    filterOnlyAlertStock = false
                                }
                                .testTag("filter_chip_cat_${cat.name}")
                        ) {
                            Text(
                                text = cat.titleKh,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Products Inventory List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("inventory_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📦", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "មិនមានទំនិញត្រូវស្វែងរកទេ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "សូមចុចប៊ូតុង '+' ខាងក្រោមដើម្បីបន្ថែមទំនិញថ្មី",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { item ->
                        InventoryItemCard(
                            product = item,
                            onQuickRestock = { amount -> viewModel.quickRestock(item.id, amount) },
                            onEdit = { productToEdit = item },
                            onDelete = { productToDelete = item }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add New Item
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_new_product_fab"),
            containerColor = SlushiePinkPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "បន្ថែមទំនិញ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            ProductEditDialog(
                initialProduct = null,
                onSave = { newProduct ->
                    viewModel.saveProduct(newProduct)
                    showAddDialog = false
                },
                onDismiss = { showAddDialog = false }
            )
        }

        // Edit Product Dialog
        productToEdit?.let { editProduct ->
            ProductEditDialog(
                initialProduct = editProduct,
                onSave = { updatedProduct ->
                    viewModel.saveProduct(updatedProduct)
                    productToEdit = null
                },
                onDismiss = { productToEdit = null }
            )
        }

        // Delete Confirmation Dialog
        productToDelete?.let { delProduct ->
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "លុបទំនិញពីប្រព័ន្ធ?",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "តើអ្នកពិតជាចង់លុបទំនិញ '${delProduct.nameKh}' ចេញពីស្តុកមែនទេ? សកម្មភាពនេះមិនអាចត្រឡប់វិញបានឡើយ។",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(delProduct.id, delProduct.nameKh)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.testTag("confirm_delete_product_button")
                    ) {
                        Text("លុបចេញ", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { productToDelete = null }) {
                        Text("បោះបង់")
                    }
                }
            )
        }

        // Low Stock Alert Summary Modal
        if (showLowStockAlertModal) {
            LowStockAlertModal(
                lowStockList = lowStockProducts,
                outOfStockList = outOfStockProducts,
                onQuickRestock = { id, amount -> viewModel.quickRestock(id, amount) },
                onDismiss = { showLowStockAlertModal = false }
            )
        }
    }
}

@Composable
fun InventoryItemCard(
    product: Product,
    onQuickRestock: (Int) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventory_item_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (product.isOutOfStock) Color(0xFFFCA5A5)
            else if (product.isLowStock) Color(0xFFFCD34D)
            else Color(product.primaryColorHex).copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Product Emoji + Name + Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(product.primaryColorHex).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = product.iconEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = product.nameKh,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = product.categoryKh,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Formatters.formatRiel(product.priceRiel),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(product.primaryColorHex)
                            )
                        }
                    }
                }

                // Edit & Delete Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Product",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_product_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Product",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Stock details & Quick Restock buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Stock Badge & Alert threshold
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            product.isOutOfStock -> Color(0xFFFEF2F2)
                            product.isLowStock -> Color(0xFFFFFBEB)
                            else -> Color(0xFFF0FDF4)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                product.isOutOfStock -> Color(0xFFFCA5A5)
                                product.isLowStock -> Color(0xFFFCD34D)
                                else -> Color(0xFFBBF7D0)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            product.isOutOfStock -> Color(0xFFEF4444)
                                            product.isLowStock -> Color(0xFFF59E0B)
                                            else -> Color(0xFF10B981)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    product.isOutOfStock -> "អស់ស្តុក (0)"
                                    product.isLowStock -> "សល់តិច: ${product.stockCount}"
                                    else -> "ស្តុក: ${product.stockCount}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    product.isOutOfStock -> Color(0xFFDC2626)
                                    product.isLowStock -> Color(0xFFD97706)
                                    else -> Color(0xFF15803D)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "(ប្រកាសអាសន្ន: ≤ ${product.lowStockThreshold})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick restock pills (+5, +10)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ថែមស្តុក:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onQuickRestock(5) }
                            .testTag("quick_restock_5_${product.id}")
                    ) {
                        Text(
                            text = "+5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F766E).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onQuickRestock(10) }
                            .testTag("quick_restock_10_${product.id}")
                    ) {
                        Text(
                            text = "+10",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4338CA).copy(alpha = 0.15f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onQuickRestock(20) }
                            .testTag("quick_restock_20_${product.id}")
                    ) {
                        Text(
                            text = "+20",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditDialog(
    initialProduct: Product?,
    onSave: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = initialProduct != null

    var nameKh by remember { mutableStateOf(initialProduct?.nameKh ?: "") }
    var priceStr by remember { mutableStateOf(initialProduct?.priceRiel?.toString() ?: "4000") }
    var stockStr by remember { mutableStateOf(initialProduct?.stockCount?.toString() ?: "20") }
    var thresholdStr by remember { mutableStateOf(initialProduct?.lowStockThreshold?.toString() ?: "5") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: ProductCategory.DRINKS_SWEETS) }
    var iconEmoji by remember { mutableStateOf(initialProduct?.iconEmoji ?: "🥤") }
    var colorHex by remember { mutableStateOf(initialProduct?.primaryColorHex ?: 0xFF00BCD4) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetEmojis = listOf("🥤", "🧃", "🍧", "🍿", "🍘", "🍙", "🍜", "🥟", "🥛", "🍓", "🍬", "🎁", "🪭", "🐲", "🧁")
    val presetColors = listOf(
        0xFF00BCD4, // Cyan
        0xFFE91E63, // Pink
        0xFFFF9800, // Orange
        0xFF4CAF50, // Green
        0xFF9C27B0, // Purple
        0xFF3F51B5, // Indigo
        0xFF009688, // Teal
        0xFFFF5722  // Deep Orange
    )

    fun handleSave() {
        val trimmedName = nameKh.trim()
        if (trimmedName.isEmpty()) {
            errorMessage = "សូមវាយបញ្ចូលឈ្មោះទំនិញ!"
            return
        }

        val price = priceStr.toIntOrNull()
        if (price == null || price <= 0) {
            errorMessage = "តម្លៃទំនិញត្រូវតែធំជាង 0 រៀល!"
            return
        }

        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            errorMessage = "ចំនួនស្តុកត្រូវតែជាលេខវិជ្ជមាន (≥ 0)!"
            return
        }

        val threshold = thresholdStr.toIntOrNull() ?: 5

        val productId = initialProduct?.id ?: ("prod_" + System.currentTimeMillis())

        val product = Product(
            id = productId,
            nameKh = trimmedName,
            priceRiel = price,
            category = selectedCategory,
            categoryKh = selectedCategory.titleKh,
            iconEmoji = iconEmoji,
            primaryColorHex = colorHex,
            descriptionKh = initialProduct?.descriptionKh ?: "",
            stockCount = stock,
            lowStockThreshold = threshold
        )
        onSave(product)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("product_edit_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEdit) "✏️ កែប្រែទំនិញ" else "➕ បន្ថែមទំនិញថ្មី",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                if (errorMessage != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Name
                item {
                    Column {
                        Text(text = "ឈ្មោះទំនិញ (Item Name)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = nameKh,
                            onValueChange = { nameKh = it },
                            modifier = Modifier.fillMaxWidth().testTag("product_input_name"),
                            placeholder = { Text("ឧ. Slushie ផ្លែស្ត្របឺរី") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Retail Price
                item {
                    Column {
                        Text(text = "តម្លៃលក់រាយ (Price in Riel)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) priceStr = it },
                            modifier = Modifier.fillMaxWidth().testTag("product_input_price"),
                            placeholder = { Text("4000") },
                            suffix = { Text("៛", fontWeight = FontWeight.Bold) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Stock Count & Low Stock Alert Threshold Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ចំនួនស្តុក (Stock Units)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = stockStr,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) stockStr = it },
                                modifier = Modifier.fillMaxWidth().testTag("product_input_stock"),
                                placeholder = { Text("20") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ប្រកាសអាសន្ន (Threshold)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = thresholdStr,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) thresholdStr = it },
                                modifier = Modifier.fillMaxWidth().testTag("product_input_threshold"),
                                placeholder = { Text("5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Category Selection
                item {
                    Column {
                        Text(text = "ប្រភេទមុខទំនិញ (Category)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                ProductCategory.DRINKS_SWEETS,
                                ProductCategory.SNACKS,
                                ProductCategory.TOYS
                            ).forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedCategory = cat }
                                        .testTag("select_cat_${cat.name}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Text(
                                        text = cat.titleKh,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Icon / Emoji selector
                item {
                    Column {
                        Text(text = "រូបតំណាង Emoji: $iconEmoji", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presetEmojis) { emoji ->
                                val isSelected = iconEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            1.5.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { iconEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                // Color Tag selector
                item {
                    Column {
                        Text(text = "ពណ៌សម្គាល់ (Color Tag)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presetColors) { hex ->
                                val isSelected = colorHex == hex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(hex))
                                        .clickable { colorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("បោះបង់")
                        }

                        Button(
                            onClick = { handleSave() },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_product_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlushiePinkPrimary)
                        ) {
                            Text(
                                text = if (isEdit) "រក្សាទុកការកែប្រែ" else "រក្សាទុកទំនិញថ្មី",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockAlertModal(
    lowStockList: List<Product>,
    outOfStockList: List<Product>,
    onQuickRestock: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("low_stock_alert_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "បញ្ជីទំនិញត្រូវថែមស្តុក",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val allAlertItems = outOfStockList + lowStockList

                if (allAlertItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎉 ស្តុកទាំងអស់មានបរិមាណគ្រប់គ្រាន់!",
                            color = Color(0xFF15803D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allAlertItems, key = { it.id }) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (item.isOutOfStock) Color(0xFFFEF2F2) else Color(0xFFFFFBEB),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (item.isOutOfStock) Color(0xFFFCA5A5) else Color(0xFFFCD34D)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = item.iconEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.nameKh,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (item.isOutOfStock) "⚠️ អស់ពីស្តុក (0)" else "⚠️ នៅសល់: ${item.stockCount} (កម្រិត: ${item.lowStockThreshold})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isOutOfStock) Color(0xFFDC2626) else Color(0xFFD97706)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onQuickRestock(item.id, 10) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (item.isOutOfStock) Color(0xFFDC2626) else Color(0xFFD97706),
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("modal_restock_10_${item.id}")
                                    ) {
                                        Text("+10 ស្តុក", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("បិទផ្ទាំង")
                }
            }
        }
    }
}
