package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.components.HeroAccountCard
import com.example.ui.components.SummaryStatsGrid
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.LedgerViewModel
import com.example.ui.viewmodel.NavigationTab

@Composable
fun DashboardScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val allTx by viewModel.allTransactions.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()

    val recentTransactions = allTx.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            HeroAccountCard(
                summary = summary,
                businessName = businessName,
                onSettleClick = {
                    viewModel.openDialog(ActiveDialog.SettleDebt(summary.businessMoneyUsedPersonallyPaise))
                }
            )
        }

        // Quick Action Buttons
        item {
            QuickActionsRow(
                onAddIncome = { viewModel.openDialog(ActiveDialog.AddIncome) },
                onAddExpense = { viewModel.openDialog(ActiveDialog.AddExpense) },
                onSettle = { viewModel.openDialog(ActiveDialog.SettleDebt(summary.businessMoneyUsedPersonallyPaise)) },
                onTransfer = { viewModel.openDialog(ActiveDialog.TransferFunds) }
            )
        }

        // Summary Stats Grid
        item {
            SummaryStatsGrid(summary = summary)
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (allTx.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.selectTab(NavigationTab.TRANSACTIONS) }
                    ) {
                        Text(
                            text = "View All (${allTx.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Recent Transactions List
        if (recentTransactions.isEmpty()) {
            item {
                EmptyPlaceholder(
                    title = "No Transactions Recorded",
                    subtitle = "Tap '+ Add Money' or '- Add Expense' to start tracking mixed business and personal funds.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            }
        } else {
            items(recentTransactions, key = { it.id }) { tx ->
                TransactionItemCard(
                    transaction = tx,
                    onClick = { viewModel.openDialog(ActiveDialog.ViewDetails(tx)) },
                    onEdit = { viewModel.openDialog(ActiveDialog.EditTransaction(tx)) },
                    onDelete = { viewModel.openDialog(ActiveDialog.DeleteConfirmation(tx)) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun QuickActionsRow(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onSettle: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Two Large Primary Action Buttons (Clean Minimalism Pill Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddIncome,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("add_income_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanBluePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Income",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = onAddExpense,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("add_expense_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CleanBlueContainer,
                    contentColor = CleanOnBlueContainer
                ),
                shape = RoundedCornerShape(50),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CleanOnBlueContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Two Secondary Helper Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSettle,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorderSolid)
            ) {
                Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(15.dp), tint = Amber700)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Return ₹",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                )
            }

            OutlinedButton(
                onClick = onTransfer,
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, CleanBorderSolid)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp), tint = CleanTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Bank ⇄ Cash",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                )
            }
        }
    }
}
