package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AppDialogHost
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.LedgerViewModel
import com.example.ui.viewmodel.NavigationTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CleanBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Money Ledger Logo",
                                modifier = Modifier.size(30.dp).clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (activeTab) {
                                NavigationTab.DASHBOARD -> "Money Ledger"
                                NavigationTab.TRANSACTIONS -> "Transactions"
                                NavigationTab.REPORTS -> "Reports"
                                NavigationTab.AI_ASSISTANT -> "AI Assistant"
                                NavigationTab.SETTINGS -> "Settings"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.3).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CleanNavBg,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == NavigationTab.DASHBOARD,
                    onClick = { viewModel.selectTab(NavigationTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontWeight = if (activeTab == NavigationTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanOnBlueContainer,
                        selectedTextColor = CleanOnBlueContainer,
                        indicatorColor = CleanBlueContainer,
                        unselectedIconColor = CleanTextSecondary,
                        unselectedTextColor = CleanTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.TRANSACTIONS,
                    onClick = { viewModel.selectTab(NavigationTab.TRANSACTIONS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Transactions") },
                    label = { Text("History", fontWeight = if (activeTab == NavigationTab.TRANSACTIONS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanOnBlueContainer,
                        selectedTextColor = CleanOnBlueContainer,
                        indicatorColor = CleanBlueContainer,
                        unselectedIconColor = CleanTextSecondary,
                        unselectedTextColor = CleanTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.REPORTS,
                    onClick = { viewModel.selectTab(NavigationTab.REPORTS) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text("Reports", fontWeight = if (activeTab == NavigationTab.REPORTS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanOnBlueContainer,
                        selectedTextColor = CleanOnBlueContainer,
                        indicatorColor = CleanBlueContainer,
                        unselectedIconColor = CleanTextSecondary,
                        unselectedTextColor = CleanTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.AI_ASSISTANT,
                    onClick = { viewModel.selectTab(NavigationTab.AI_ASSISTANT) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant") },
                    label = { Text("AI Helper", fontWeight = if (activeTab == NavigationTab.AI_ASSISTANT) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanOnBlueContainer,
                        selectedTextColor = CleanOnBlueContainer,
                        indicatorColor = CleanBlueContainer,
                        unselectedIconColor = CleanTextSecondary,
                        unselectedTextColor = CleanTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = activeTab == NavigationTab.SETTINGS,
                    onClick = { viewModel.selectTab(NavigationTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = if (activeTab == NavigationTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CleanOnBlueContainer,
                        selectedTextColor = CleanOnBlueContainer,
                        indicatorColor = CleanBlueContainer,
                        unselectedIconColor = CleanTextSecondary,
                        unselectedTextColor = CleanTextSecondary
                    )
                )
            }
        },
        floatingActionButton = {
            if (activeTab == NavigationTab.TRANSACTIONS) {
                FloatingActionButton(
                    onClick = { viewModel.openDialog(ActiveDialog.AddExpense) },
                    containerColor = CleanBluePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_tx")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                NavigationTab.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                NavigationTab.REPORTS -> ReportsScreen(viewModel = viewModel)
                NavigationTab.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                NavigationTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    // Modal dialogs host
    AppDialogHost(viewModel = viewModel)
}
