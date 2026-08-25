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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.CloudConfigDialog
import com.example.ui.SalesViewModel
import com.example.ui.components.HeaderBar
import com.example.ui.screens.HistoryScreen
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
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

@Composable
fun JollySlushieApp(
    viewModel: SalesViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val todayRevenue by viewModel.todayTotalRevenue.collectAsStateWithLifecycle()
    val todayItemsCount by viewModel.todayTotalItemsCount.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()

    var showCloudSettingsDialog by remember { mutableStateOf(false) }

    val navTabs = listOf(
        NavTabItem(
            title = "លក់ទំនិញ",
            selectedIcon = Icons.Filled.Storefront,
            unselectedIcon = Icons.Outlined.Storefront,
            testTag = "nav_tab_sale"
        ),
        NavTabItem(
            title = "របាយការណ៍",
            selectedIcon = Icons.Filled.Assessment,
            unselectedIcon = Icons.Outlined.Assessment,
            testTag = "nav_tab_report"
        ),
        NavTabItem(
            title = "ប្រវត្តិការលក់",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History,
            testTag = "nav_tab_history"
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HeaderBar(
                todayRevenue = todayRevenue,
                todayItemsCount = todayItemsCount,
                isOnline = isOnline,
                onOpenSettings = { showCloudSettingsDialog = true }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                navTabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> SaleScreen(viewModel = viewModel)
                1 -> ReportScreen(viewModel = viewModel)
                2 -> HistoryScreen(viewModel = viewModel)
            }
        }
    }

    if (showCloudSettingsDialog) {
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
