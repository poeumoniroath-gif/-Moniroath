package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.SalesViewModel
import com.example.ui.components.CartSheet
import com.example.ui.components.SaleConfirmDialog
import com.example.ui.components.SaleSuccessDialog
import com.example.util.Formatters

@Composable
fun SaleScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val outOfStockProducts by viewModel.outOfStockProducts.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeProduct by viewModel.activeProductForSale.collectAsStateWithLifecycle()
    val activeQuantity by viewModel.activeQuantity.collectAsStateWithLifecycle()
    val lastSaleSuccess by viewModel.lastSaleSuccess.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotalItems by viewModel.cartTotalItems.collectAsStateWithLifecycle()
    val cartTotalRiel by viewModel.cartTotalRiel.collectAsStateWithLifecycle()

    var showCartSheet by remember { mutableStateOf(false) }

    val filteredProducts = if (selectedCategory == ProductCategory.ALL) {
        products
    } else {
        products.filter { it.category == selectedCategory }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Low Stock Warning Banner for Cashier
            if (outOfStockProducts.isNotEmpty() || lowStockProducts.isNotEmpty()) {
                Surface(
                    color = Color(0xFFFFFBEB),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCD34D)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("pos_low_stock_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ការដាស់តឿនស្តុក: មាន ${outOfStockProducts.size} មុខអស់ស្តុក, ${lowStockProducts.size} មុខសល់តិច",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }

            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ProductCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectCategory(category) }
                            .testTag("category_filter_${category.name}")
                    ) {
                        Text(
                            text = category.titleKh,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 155.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("products_grid"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = if (cartItems.isNotEmpty()) 90.dp else 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val cartItem = cartItems.find { it.product.id == product.id }
                    ProductCard(
                        product = product,
                        inCartCount = cartItem?.quantity ?: 0,
                        onClick = { viewModel.openSaleDialog(product) },
                        onQuickAdd = { viewModel.addToCart(product, 1) }
                    )
                }
            }
        }

        // Floating Cart Bar (Appears when cart has items)
        AnimatedVisibility(
            visible = cartItems.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCartSheet = true }
                    .testTag("floating_cart_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "កន្ត្រក",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "$cartTotalItems កែវ ក្នុងកន្ត្រក",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = Formatters.formatRiel(cartTotalRiel),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF34D399)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showCartSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("open_cart_sheet_button")
                    ) {
                        Text(
                            text = "គិតលុយ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Active Sale Confirmation Dialog
        activeProduct?.let { product ->
            SaleConfirmDialog(
                product = product,
                quantity = activeQuantity,
                onIncrement = { viewModel.incrementQuantity() },
                onDecrement = { viewModel.decrementQuantity() },
                onSelectQuickQty = { qty -> viewModel.setQuantity(qty) },
                onAddToCart = { viewModel.addToCart(product, activeQuantity) },
                onQuickConfirm = { paymentMethod -> viewModel.quickSellSingle(paymentMethod) },
                onDismiss = { viewModel.closeSaleDialog() }
            )
        }

        // Cart Bottom Sheet
        if (showCartSheet) {
            CartSheet(
                items = cartItems,
                totalRiel = cartTotalRiel,
                totalItems = cartTotalItems,
                onIncrement = { id -> viewModel.updateCartItemQuantity(id, 1) },
                onDecrement = { id -> viewModel.updateCartItemQuantity(id, -1) },
                onRemove = { id -> viewModel.removeCartItem(id) },
                onClearCart = { viewModel.clearCart() },
                onConfirmCheckout = { paymentMethod ->
                    showCartSheet = false
                    viewModel.checkoutCart(paymentMethod)
                },
                onDismiss = { showCartSheet = false }
            )
        }

        // Sale Success Feedback Overlay
        lastSaleSuccess?.let { successEvent ->
            SaleSuccessDialog(
                event = successEvent,
                onDismiss = { viewModel.dismissSuccessFeedback() }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    inCartCount: Int,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = product.isOutOfStock
    val isLowStock = product.isLowStock

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .alpha(if (isOutOfStock) 0.65f else 1.0f)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                inCartCount > 0 -> MaterialTheme.colorScheme.primary
                isOutOfStock -> Color(0xFFFCA5A5)
                isLowStock -> Color(0xFFFCD34D)
                else -> Color(product.primaryColorHex).copy(alpha = 0.25f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stock Indicator Badge at top of card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isOutOfStock -> Color(0xFFFEF2F2)
                        isLowStock -> Color(0xFFFFFBEB)
                        else -> Color(0xFFF0FDF4)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isOutOfStock -> Color(0xFFFCA5A5)
                            isLowStock -> Color(0xFFFCD34D)
                            else -> Color(0xFFBBF7D0)
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isOutOfStock -> "🚫 អស់ស្តុក"
                            isLowStock -> "⚠️ សល់ ${product.stockCount}"
                            else -> "ស្តុក: ${product.stockCount}"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isOutOfStock -> Color(0xFFDC2626)
                            isLowStock -> Color(0xFFD97706)
                            else -> Color(0xFF15803D)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // In Cart Count Badge
                if (inCartCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$inCartCount",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Emoji Icon Box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(product.primaryColorHex).copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.iconEmoji,
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name
            Text(
                text = product.nameKh,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category Label
            Text(
                text = product.categoryKh,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Tag Pill + Quick Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(product.primaryColorHex).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(product.primaryColorHex).copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = Formatters.formatRiel(product.priceRiel),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(product.primaryColorHex)
                        )
                    )
                }

                FilledIconButton(
                    onClick = onQuickAdd,
                    enabled = !isOutOfStock,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("quick_add_${product.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isOutOfStock) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isOutOfStock) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = Color(0xFFF1F5F9),
                        disabledContentColor = Color(0xFF94A3B8)
                    )
                ) {
                    Icon(
                        imageVector = if (isOutOfStock) Icons.Default.Block else Icons.Default.Add,
                        contentDescription = "ដាក់កន្ត្រកភ្លាមៗ",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
