package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.BusinessCategories
import com.example.data.model.CurrencyHelper
import com.example.data.model.FundPurpose
import com.example.data.model.PersonalCategories
import com.example.data.model.TransactionType
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveDialog
import com.example.ui.viewmodel.LedgerViewModel

@Composable
fun AppDialogHost(viewModel: LedgerViewModel) {
    val dialogState by viewModel.activeDialog.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    when (val state = dialogState) {
        is ActiveDialog.AddIncome -> {
            AddIncomeDialog(
                businessName = businessName,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { amount, purpose, source, category, account, desc, note ->
                    viewModel.addIncome(
                        amountPaise = amount,
                        purpose = purpose,
                        source = source,
                        category = category,
                        account = account,
                        description = desc,
                        note = note,
                        timestamp = System.currentTimeMillis()
                    )
                }
            )
        }
        is ActiveDialog.AddExpense -> {
            AddExpenseDialog(
                businessName = businessName,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { amount, purpose, category, account, desc, note ->
                    viewModel.addExpense(
                        amountPaise = amount,
                        purpose = purpose,
                        category = category,
                        account = account,
                        description = desc,
                        note = note,
                        timestamp = System.currentTimeMillis()
                    )
                }
            )
        }
        is ActiveDialog.SettleDebt -> {
            val defaultAmount = state.defaultAmountPaise.takeIf { it > 0 }
                ?: summary.businessMoneyUsedPersonallyPaise
            SettleDebtDialog(
                defaultAmountPaise = defaultAmount,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { amount, account, desc, note ->
                    viewModel.addSettlement(
                        amountPaise = amount,
                        account = account,
                        description = desc,
                        note = note,
                        timestamp = System.currentTimeMillis()
                    )
                }
            )
        }
        is ActiveDialog.TransferFunds -> {
            TransferFundsDialog(
                onDismiss = { viewModel.dismissDialog() },
                onSave = { amount, fromAccount, toAccount, desc, note ->
                    viewModel.addTransfer(
                        amountPaise = amount,
                        fromAccount = fromAccount,
                        toAccount = toAccount,
                        description = desc,
                        note = note,
                        timestamp = System.currentTimeMillis()
                    )
                }
            )
        }
        is ActiveDialog.EditTransaction -> {
            EditTransactionDialog(
                transaction = state.transaction,
                businessName = businessName,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { updated -> viewModel.updateTransaction(updated) },
                onDelete = { viewModel.deleteTransaction(state.transaction) }
            )
        }
        is ActiveDialog.DeleteConfirmation -> {
            DeleteConfirmDialog(
                transaction = state.transaction,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { viewModel.deleteTransaction(state.transaction) }
            )
        }
        is ActiveDialog.ViewDetails -> {
            TransactionDetailDialog(
                transaction = state.transaction,
                onDismiss = { viewModel.dismissDialog() },
                onEdit = {
                    viewModel.openDialog(ActiveDialog.EditTransaction(state.transaction))
                },
                onDelete = {
                    viewModel.openDialog(ActiveDialog.DeleteConfirmation(state.transaction))
                }
            )
        }
        ActiveDialog.None -> {}
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddIncomeDialog(
    businessName: String,
    onDismiss: () -> Unit,
    onSave: (amountPaise: Long, purpose: FundPurpose, source: String, category: String, account: String, desc: String, note: String?) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var selectedPurpose by remember { mutableStateOf(FundPurpose.BUSINESS) }
    var selectedSource by remember { mutableStateOf(businessName) }
    var selectedAccount by remember { mutableStateOf(AccountType.BANK) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickSources = if (selectedPurpose == FundPurpose.BUSINESS) {
        listOf(businessName, "Clothing Sale", "Wholesale", "Custom Stitching", "Other")
    } else {
        listOf("Personal", "Salary", "Freelance", "Personal Savings", "Gift", "Other")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_income_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Emerald100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add Money (Income)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = { Text("Amount (${CurrencyHelper.currencySymbol})") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Income Source / Purpose Switcher
                Text(
                    text = "WHOSE MONEY IS THIS?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPurpose == FundPurpose.BUSINESS,
                        onClick = {
                            selectedPurpose = FundPurpose.BUSINESS
                            selectedSource = businessName
                        },
                        label = { Text("Mom's Business") },
                        leadingIcon = {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber100,
                            selectedLabelColor = Amber800
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedPurpose == FundPurpose.PERSONAL,
                        onClick = {
                            selectedPurpose = FundPurpose.PERSONAL
                            selectedSource = "Personal"
                        },
                        label = { Text("Personal") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Indigo100,
                            selectedLabelColor = Indigo800
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Source Category Chips
                Text(
                    text = "SPECIFIC SOURCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickSources.forEach { src ->
                        FilterChip(
                            selected = selectedSource == src,
                            onClick = { selectedSource = src },
                            label = { Text(src, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection
                Text(
                    text = "DEPOSITED INTO ACCOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.all.forEach { acc ->
                        FilterChip(
                            selected = selectedAccount == acc,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Clothing sale / Client payment") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("e.g. Customer UPI / Invoice ref") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val paise = CurrencyHelper.parseAmountToPaise(amountInput)
                            if (paise == null || paise <= 0) {
                                errorMessage = "Please enter a valid amount"
                            } else {
                                val cat = selectedSource
                                onSave(
                                    paise,
                                    selectedPurpose,
                                    selectedSource,
                                    cat,
                                    selectedAccount,
                                    description.ifBlank { "Income - $selectedSource" },
                                    note.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_income_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text("Record Income")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    businessName: String,
    onDismiss: () -> Unit,
    onSave: (amountPaise: Long, purpose: FundPurpose, category: String, account: String, desc: String, note: String?) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var selectedPurpose by remember { mutableStateOf(FundPurpose.BUSINESS) }
    var selectedCategory by remember { mutableStateOf("Fabric") }
    var selectedAccount by remember { mutableStateOf(AccountType.BANK) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = if (selectedPurpose == FundPurpose.BUSINESS) {
        BusinessCategories.expenseCategories
    } else {
        PersonalCategories.expenseCategories
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_expense_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Rose100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = null,
                                tint = Rose700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Add Expense",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = { Text("Amount (${CurrencyHelper.currencySymbol})") },
                    placeholder = { Text("e.g. 1200") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Purpose Switcher: Business Expense vs Personal Expense
                Text(
                    text = "PURPOSE OF THIS EXPENSE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPurpose == FundPurpose.BUSINESS,
                        onClick = {
                            selectedPurpose = FundPurpose.BUSINESS
                            selectedCategory = "Fabric"
                        },
                        label = { Text("Business Expense") },
                        leadingIcon = {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber100,
                            selectedLabelColor = Amber800
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedPurpose == FundPurpose.PERSONAL,
                        onClick = {
                            selectedPurpose = FundPurpose.PERSONAL
                            selectedCategory = "Food & Dining"
                        },
                        label = { Text("Personal Expense") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Indigo100,
                            selectedLabelColor = Indigo800
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Categories
                Text(
                    text = "EXPENSE CATEGORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Account Selection
                Text(
                    text = "PAID FROM ACCOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.all.forEach { acc ->
                        FilterChip(
                            selected = selectedAccount == acc,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. Fabric purchase / Delivery charges") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note") },
                    placeholder = { Text("e.g. Vendor name, invoice #") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val paise = CurrencyHelper.parseAmountToPaise(amountInput)
                            if (paise == null || paise <= 0) {
                                errorMessage = "Please enter a valid amount"
                            } else {
                                onSave(
                                    paise,
                                    selectedPurpose,
                                    selectedCategory,
                                    selectedAccount,
                                    description.ifBlank { "$selectedCategory Expense" },
                                    note.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_expense_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose700)
                    ) {
                        Text("Record Expense")
                    }
                }
            }
        }
    }
}

@Composable
fun SettleDebtDialog(
    defaultAmountPaise: Long,
    onDismiss: () -> Unit,
    onSave: (amountPaise: Long, account: String, desc: String, note: String?) -> Unit
) {
    val initialText = if (defaultAmountPaise > 0) {
        val rupees = defaultAmountPaise / 100.0
        if (defaultAmountPaise % 100 == 0L) rupees.toInt().toString() else rupees.toString()
    } else ""

    var amountInput by remember { mutableStateOf(initialText) }
    var selectedAccount by remember { mutableStateOf(AccountType.BANK) }
    var description by remember { mutableStateOf("Settled personal borrowing to Mom's business") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("settle_debt_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Emerald100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Return to Business",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Return money you previously spent for personal purposes back to Mom's business ledger.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = { Text("Settlement Amount (${CurrencyHelper.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_amount_input"),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account
                Text(
                    text = "RETURNED VIA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.all.forEach { acc ->
                        FilterChip(
                            selected = selectedAccount == acc,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val paise = CurrencyHelper.parseAmountToPaise(amountInput)
                            if (paise == null || paise <= 0) {
                                errorMessage = "Please enter a valid amount"
                            } else {
                                onSave(
                                    paise,
                                    selectedAccount,
                                    description,
                                    note.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_settle_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text("Confirm Settlement")
                    }
                }
            }
        }
    }
}

@Composable
fun TransferFundsDialog(
    onDismiss: () -> Unit,
    onSave: (amountPaise: Long, fromAccount: String, toAccount: String, desc: String, note: String?) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var fromAccount by remember { mutableStateOf(AccountType.BANK) }
    var toAccount by remember { mutableStateOf(AccountType.CASH) }
    var description by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Slate200),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = Slate700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Account Transfer",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Transfers between accounts (e.g. Bank → Cash) do NOT count as expenses and preserve total wealth.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = { Text("Transfer Amount (${CurrencyHelper.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // From Account
                Text(
                    text = "FROM ACCOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.all.forEach { acc ->
                        FilterChip(
                            selected = fromAccount == acc,
                            onClick = { fromAccount = acc },
                            label = { Text(acc, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // To Account
                Text(
                    text = "TO ACCOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.all.forEach { acc ->
                        FilterChip(
                            selected = toAccount == acc,
                            onClick = { toAccount = acc },
                            label = { Text(acc, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. ATM cash withdrawal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val paise = CurrencyHelper.parseAmountToPaise(amountInput)
                            if (paise == null || paise <= 0) {
                                errorMessage = "Please enter a valid amount"
                            } else if (fromAccount == toAccount) {
                                errorMessage = "Source and destination accounts must be different"
                            } else {
                                onSave(
                                    paise,
                                    fromAccount,
                                    toAccount,
                                    description.ifBlank { "Transfer: $fromAccount → $toAccount" },
                                    note.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Transfer")
                    }
                }
            }
        }
    }
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    businessName: String,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: () -> Unit
) {
    val rupees = transaction.amountPaise / 100.0
    val initialAmount = if (transaction.amountPaise % 100 == 0L) rupees.toInt().toString() else rupees.toString()

    var amountInput by remember { mutableStateOf(initialAmount) }
    var description by remember { mutableStateOf(transaction.description) }
    var category by remember { mutableStateOf(transaction.category) }
    var account by remember { mutableStateOf(transaction.account) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it
                        errorMessage = null
                    },
                    label = { Text("Amount (${CurrencyHelper.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMessage != null
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category / Source") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Account
                Text(
                    text = "ACCOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.all.forEach { acc ->
                        FilterChip(
                            selected = account == acc,
                            onClick = { account = acc },
                            label = { Text(acc, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose700),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            val paise = CurrencyHelper.parseAmountToPaise(amountInput)
                            if (paise == null || paise <= 0) {
                                errorMessage = "Invalid amount"
                            } else {
                                onSave(
                                    transaction.copy(
                                        amountPaise = paise,
                                        description = description,
                                        category = category,
                                        account = account,
                                        note = note.ifBlank { null }
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Transaction?") },
        text = {
            Text(
                "Are you sure you want to delete \"${transaction.description}\" (${CurrencyHelper.formatPaise(transaction.amountPaise)})? This will update your live business and personal ledger balances."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Rose700)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val isSettlement = transaction.type == TransactionType.SETTLEMENT.name
    val isTransfer = transaction.type == TransactionType.TRANSFER.name
    val isBusiness = transaction.purpose == FundPurpose.BUSINESS.name

    val (badgeBg, badgeColor, typeLabel) = when {
        isTransfer -> Triple(Slate200, Slate800, "Account Transfer")
        isSettlement -> Triple(Emerald100, Emerald800, "Personal Repayment")
        isIncome -> if (isBusiness) Triple(Amber100, Amber800, "Business Income") else Triple(Indigo100, Indigo800, "Personal Income")
        else -> if (isBusiness) Triple(Amber100, Amber800, "Business Expense") else Triple(Indigo100, Indigo800, "Personal Expense")
    }

    val amountColor = when {
        isTransfer -> MaterialTheme.colorScheme.onSurface
        isSettlement -> Emerald700
        isIncome -> Emerald700
        else -> Rose700
    }

    val prefix = when {
        isTransfer -> ""
        isSettlement -> "+ "
        isIncome -> "+ "
        else -> "− "
    }

    val primaryTitle = transaction.description.ifBlank { transaction.category }.ifBlank { "Transaction" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row: Type Badge + Close Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = typeLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Amount
                Text(
                    text = prefix + CurrencyHelper.formatPaise(transaction.amountPaise),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = amountColor,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Purpose / Title
                Text(
                    text = primaryTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(16.dp))

                // Secondary Information Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Account / Payment Method
                    val accountLabel = when {
                        isTransfer -> "Transfer Route"
                        isSettlement -> "From"
                        isIncome -> "Deposited to"
                        else -> "Paid from"
                    }
                    val accountValue = if (isTransfer && !transaction.toAccount.isNullOrBlank()) {
                        "${transaction.account} → ${transaction.toAccount}"
                    } else {
                        transaction.account
                    }
                    val accountIcon = if (transaction.account.contains("Bank", ignoreCase = true)) {
                        Icons.Default.AccountBalance
                    } else {
                        Icons.Default.Payments
                    }

                    DetailInfoRow(
                        icon = accountIcon,
                        label = accountLabel,
                        value = accountValue
                    )

                    // 2. Date & Time
                    DetailInfoRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Date & Time",
                        value = "${CurrencyHelper.formatDate(transaction.timestamp)} • ${CurrencyHelper.formatTime(transaction.timestamp)}"
                    )

                    // 3. Category (if distinct from description/primaryTitle)
                    val hasDistinctCategory = transaction.category.isNotBlank() &&
                            !transaction.category.equals(primaryTitle, ignoreCase = true)
                    if (hasDistinctCategory) {
                        DetailInfoRow(
                            icon = Icons.Default.Category,
                            label = "Category",
                            value = transaction.category
                        )
                    }

                    // 4. Source / Beneficiary (if present and distinct)
                    if (transaction.source.isNotBlank() && !transaction.source.equals(primaryTitle, ignoreCase = true)) {
                        DetailInfoRow(
                            icon = if (isBusiness) Icons.Default.BusinessCenter else Icons.Default.Person,
                            label = if (isIncome) "Received From" else "Source / Beneficiary",
                            value = transaction.source
                        )
                    }

                    // 5. Note (if present)
                    if (!transaction.note.isNullOrBlank()) {
                        DetailInfoRow(
                            icon = Icons.Default.Description,
                            label = "Note",
                            value = transaction.note
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose700),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            )
        }
    }
}
