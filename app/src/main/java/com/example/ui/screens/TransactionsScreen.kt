package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountType
import com.example.data.model.FundPurpose
import com.example.data.model.TransactionType
import com.example.ui.components.EmptyPlaceholder
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.LedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val filteredTx by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transactions_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Box
        item {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = filterState.query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search description, category, note...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (filterState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transactions_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        // Horizontal Filter Chips (Purpose & Type)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // All Chip
                    FilterChip(
                        selected = filterState.purpose == null && filterState.type == null && filterState.account == null,
                        onClick = { viewModel.resetFilters() },
                        label = { Text("All (${filteredTx.size})") }
                    )

                    // Business Chip
                    FilterChip(
                        selected = filterState.purpose == FundPurpose.BUSINESS,
                        onClick = {
                            viewModel.setFilterPurpose(
                                if (filterState.purpose == FundPurpose.BUSINESS) null else FundPurpose.BUSINESS
                            )
                        },
                        label = { Text("Business") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber100,
                            selectedLabelColor = Amber800
                        )
                    )

                    // Personal Chip
                    FilterChip(
                        selected = filterState.purpose == FundPurpose.PERSONAL,
                        onClick = {
                            viewModel.setFilterPurpose(
                                if (filterState.purpose == FundPurpose.PERSONAL) null else FundPurpose.PERSONAL
                            )
                        },
                        label = { Text("Personal") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Indigo100,
                            selectedLabelColor = Indigo800
                        )
                    )

                    // Income Chip
                    FilterChip(
                        selected = filterState.type == TransactionType.INCOME,
                        onClick = {
                            viewModel.setFilterType(
                                if (filterState.type == TransactionType.INCOME) null else TransactionType.INCOME
                            )
                        },
                        label = { Text("Income (+)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald100,
                            selectedLabelColor = Emerald800
                        )
                    )

                    // Expense Chip
                    FilterChip(
                        selected = filterState.type == TransactionType.EXPENSE,
                        onClick = {
                            viewModel.setFilterType(
                                if (filterState.type == TransactionType.EXPENSE) null else TransactionType.EXPENSE
                            )
                        },
                        label = { Text("Expenses (-)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Rose100,
                            selectedLabelColor = Rose800
                        )
                    )

                    // Settlement / Return Chip
                    FilterChip(
                        selected = filterState.type == TransactionType.SETTLEMENT,
                        onClick = {
                            viewModel.setFilterType(
                                if (filterState.type == TransactionType.SETTLEMENT) null else TransactionType.SETTLEMENT
                            )
                        },
                        label = { Text("Settlements") }
                    )

                    // Bank Filter
                    FilterChip(
                        selected = filterState.account == AccountType.BANK,
                        onClick = {
                            viewModel.setFilterAccount(
                                if (filterState.account == AccountType.BANK) null else AccountType.BANK
                            )
                        },
                        label = { Text("Bank") }
                    )

                    // Cash Filter
                    FilterChip(
                        selected = filterState.account == AccountType.CASH,
                        onClick = {
                            viewModel.setFilterAccount(
                                if (filterState.account == AccountType.CASH) null else AccountType.CASH
                            )
                        },
                        label = { Text("Cash") }
                    )
                }
            }
        }

        // Active Filter Clear indicator if active
        val hasActiveFilter = filterState.purpose != null || filterState.type != null || filterState.account != null || filterState.query.isNotEmpty()
        if (hasActiveFilter) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredTx.size} filtered results",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    TextButton(onClick = { viewModel.resetFilters() }) {
                        Text("Reset Filters", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Transactions List
        if (filteredTx.isEmpty()) {
            item {
                EmptyPlaceholder(
                    title = "No Transactions Found",
                    subtitle = if (hasActiveFilter) "No transactions match your current search/filter criteria." else "Start adding your first income or expense transaction.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            }
        } else {
            items(filteredTx, key = { it.id }) { tx ->
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
