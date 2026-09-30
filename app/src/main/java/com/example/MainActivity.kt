package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.SalesViewModel
import com.example.ui.components.CloudConfigDialog
import com.example.ui.components.HeaderBar
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SaleScreen
import com.example.ui.theme.JollySlushieTheme
import com.example.ui.theme.SlushiePinkPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JollySlushieTheme {
                JollySlushieApp()
            }
        }
    }
}

data class NavTabItem(
    val id: Int,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

@Composable
fun JollySlushieApp(
    viewModel: SalesViewModel = viewModel()
) {
    val currentUserSession by viewModel.currentUserSession.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val todayRevenue by viewModel.todayTotalRevenue.collectAsStateWithLifecycle()
    val todayItemsCount by viewModel.todayTotalItemsCount.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()
    val alertStockProducts by viewModel.alertStockProducts.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCloudSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { fb ->
            snackbarHostState.showSnackbar(fb.message)
            viewModel.clearFeedback()
        }
    }

    // Role-based Access Gate: If not logged in, show Login PIN Keypad screen
    if (currentUserSession == null) {
        LoginScreen(
            onLoginSuccess = { role, pin ->
                viewModel.login(role, pin)
            }
        )
        return
    }

    val isAdmin = currentUserSession?.role == UserRole.ADMIN

    // Role-Based Navigation Tabs Guard:
    // Admin gets Sales, Inventory, Reports, History
    // Cashier gets Sales POS only (preventing inventory modification, editing pricing, administrative controls)
    val navTabs = if (isAdmin) {
        listOf(
            NavTabItem(
                id = 0,
                title = "លក់ទំនិញ",
                selectedIcon = Icons.Filled.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                testTag = "nav_tab_sale"
            ),
            NavTabItem(
                id = 1,
                title = "ស្តុកទំនិញ",
                selectedIcon = Icons.Filled.Inventory,
                unselectedIcon = Icons.Outlined.Inventory,
                testTag = "nav_tab_inventory"
            ),
            NavTabItem(
                id = 2,
                title = "របាយការណ៍",
                selectedIcon = Icons.Filled.Assessment,
                unselectedIcon = Icons.Outlined.Assessment,
                testTag = "nav_tab_report"
            ),
            NavTabItem(
                id = 3,
                title = "ប្រវត្តិការលក់",
                selectedIcon = Icons.Filled.History,
                unselectedIcon = Icons.Outlined.History,
                testTag = "nav_tab_history"
            )
        )
    } else {
        listOf(
            NavTabItem(
                id = 0,
                title = "លក់ទំនិញ (POS)",
                selectedIcon = Icons.Filled.Storefront,
                unselectedIcon = Icons.Outlined.Storefront,
                testTag = "nav_tab_sale"
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HeaderBar(
                todayRevenue = todayRevenue,
                todayItemsCount = todayItemsCount,
                isOnline = isOnline,
                currentUserSession = currentUserSession,
                alertStockCount = alertStockProducts.size,
                onOpenAlerts = {
                    if (isAdmin) {
                        viewModel.selectTab(1)
                    }
                },
                onOpenSettings = {
                    if (isAdmin) {
                        showCloudSettingsDialog = true
                    }
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        },
        bottomBar = {
            if (navTabs.size > 1) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navTabs.forEach { tab ->
                        val isSelected = selectedTab == tab.id
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab.id) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SlushiePinkPrimary,
                                selectedTextColor = SlushiePinkPrimary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // View Guard: Cashier is locked to SaleScreen (0)
            if (!isAdmin || selectedTab == 0) {
                SaleScreen(viewModel = viewModel)
            } else {
                when (selectedTab) {
                    1 -> InventoryScreen(viewModel = viewModel)
                    2 -> ReportScreen(viewModel = viewModel)
                    3 -> HistoryScreen(viewModel = viewModel)
                    else -> SaleScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showCloudSettingsDialog && isAdmin) {
        CloudConfigDialog(
            config = cloudConfig,
            onSave = { updated ->
                viewModel.updateCloudConfig(updated)
                showCloudSettingsDialog = false
            },
            onDismiss = { showCloudSettingsDialog = false }
        )
    }
}
