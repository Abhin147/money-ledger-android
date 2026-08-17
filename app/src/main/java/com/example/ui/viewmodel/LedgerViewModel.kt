package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiAssistantRepository
import com.example.data.gemini.MessageSender
import com.example.data.local.MoneyLedgerDatabase
import com.example.data.local.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.BusinessCategories
import com.example.data.model.CurrencyHelper
import com.example.data.model.FundPurpose
import com.example.data.model.LedgerSummary
import com.example.data.model.MonthlyReport
import com.example.data.model.PersonalCategories
import com.example.data.model.TransactionType
import com.example.data.repository.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class NavigationTab(val label: String) {
    DASHBOARD("Dashboard"),
    TRANSACTIONS("Transactions"),
    REPORTS("Reports"),
    AI_ASSISTANT("AI Assistant"),
    SETTINGS("Settings")
}

sealed class ActiveDialog {
    object None : ActiveDialog()
    object AddIncome : ActiveDialog()
    object AddExpense : ActiveDialog()
    data class SettleDebt(val defaultAmountPaise: Long = 0L) : ActiveDialog()
    object TransferFunds : ActiveDialog()
    data class EditTransaction(val transaction: TransactionEntity) : ActiveDialog()
    data class DeleteConfirmation(val transaction: TransactionEntity) : ActiveDialog()
    data class ViewDetails(val transaction: TransactionEntity) : ActiveDialog()
}

data class FilterState(
    val query: String = "",
    val purpose: FundPurpose? = null,
    val type: TransactionType? = null,
    val account: String? = null,
    val category: String? = null
)

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LedgerRepository
    private val aiRepository: GeminiAssistantRepository = GeminiAssistantRepository()

    val currentMonthKey: String = CurrencyHelper.getMonthKey(System.currentTimeMillis())

    private val _selectedMonthKey = MutableStateFlow(currentMonthKey)
    val selectedMonthKey: StateFlow<String> = _selectedMonthKey.asStateFlow()

    private val _activeTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    private val _activeDialog = MutableStateFlow<ActiveDialog>(ActiveDialog.None)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.ASSISTANT,
                message = "👋 Hello! I'm your **Money Ledger AI Assistant**.\n\nI can help you monitor Mom's clothing business funds vs your personal money, verify if any business money was used personally, break down expenses, or help calculate settlements.\n\nHow can I help you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _businessName = MutableStateFlow("Mom's Clothing Business")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _currencySymbol = MutableStateFlow("₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    val allTransactions: StateFlow<List<TransactionEntity>>
    val summary: StateFlow<LedgerSummary>
    val filteredTransactions: StateFlow<List<TransactionEntity>>
    val monthlyReport: StateFlow<MonthlyReport>
    val availableMonths: StateFlow<List<String>>

    init {
        val db = MoneyLedgerDatabase.getDatabase(application)
        repository = LedgerRepository(db.transactionDao())

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        summary = repository.ledgerSummary.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LedgerSummary()
        )

        filteredTransactions = combine(allTransactions, _filterState) { list, filter ->
            list.filter { tx ->
                val matchesQuery = filter.query.isBlank() ||
                        tx.description.contains(filter.query, ignoreCase = true) ||
                        tx.category.contains(filter.query, ignoreCase = true) ||
                        tx.source.contains(filter.query, ignoreCase = true) ||
                        (tx.note?.contains(filter.query, ignoreCase = true) == true)

                val matchesPurpose = filter.purpose == null || tx.purpose == filter.purpose.name
                val matchesType = filter.type == null || tx.type == filter.type.name
                val matchesAccount = filter.account == null || tx.account == filter.account || tx.toAccount == filter.account
                val matchesCategory = filter.category == null || tx.category.equals(filter.category, ignoreCase = true)

                matchesQuery && matchesPurpose && matchesType && matchesAccount && matchesCategory
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        monthlyReport = combine(allTransactions, _selectedMonthKey) { list, month ->
            repository.generateMonthlyReport(month, list)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MonthlyReport(
                monthKey = currentMonthKey,
                monthDisplay = CurrencyHelper.formatMonthYear(System.currentTimeMillis()),
                businessIncomePaise = 0L,
                businessExpensesPaise = 0L,
                personalIncomePaise = 0L,
                personalExpensesPaise = 0L,
                usedPersonallyPaise = 0L,
                returnedToBusinessPaise = 0L,
                businessMoneyRemainingPaise = 0L,
                categoryBreakdown = emptyList()
            )
        )

        availableMonths = allTransactions.map { list: List<TransactionEntity> ->
            val set = mutableSetOf<String>(currentMonthKey)
            for (tx in list) {
                val key = if (tx.monthKey.isNotEmpty()) tx.monthKey else CurrencyHelper.getMonthKey(tx.timestamp)
                set.add(key)
            }
            set.toList().sortedDescending()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(currentMonthKey)
        )

        // Pre-seed sample data on first install if empty
        viewModelScope.launch {
            val list = repository.allTransactions.first()
            if (list.isEmpty()) {
                repository.seedSampleData()
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun openDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun dismissDialog() {
        _activeDialog.value = ActiveDialog.None
    }

    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun setFilterPurpose(purpose: FundPurpose?) {
        _filterState.value = _filterState.value.copy(purpose = purpose)
    }

    fun setFilterType(type: TransactionType?) {
        _filterState.value = _filterState.value.copy(type = type)
    }

    fun setFilterAccount(account: String?) {
        _filterState.value = _filterState.value.copy(account = account)
    }

    fun setFilterCategory(category: String?) {
        _filterState.value = _filterState.value.copy(category = category)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    fun setSelectedMonth(monthKey: String) {
        _selectedMonthKey.value = monthKey
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
        CurrencyHelper.currencySymbol = symbol
    }

    fun setBusinessName(name: String) {
        if (name.isNotBlank()) {
            _businessName.value = name
        }
    }

    fun addIncome(
        amountPaise: Long,
        purpose: FundPurpose,
        source: String,
        category: String,
        account: String,
        description: String,
        note: String?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                type = TransactionType.INCOME.name,
                purpose = purpose.name,
                amountPaise = amountPaise,
                source = source,
                category = category,
                account = account,
                description = description.ifBlank { "Income - $source" },
                note = note,
                timestamp = timestamp,
                monthKey = CurrencyHelper.getMonthKey(timestamp)
            )
            repository.insert(entity)
            dismissDialog()
        }
    }

    fun addExpense(
        amountPaise: Long,
        purpose: FundPurpose,
        category: String,
        account: String,
        description: String,
        note: String?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val sourceName = if (purpose == FundPurpose.BUSINESS) _businessName.value else "Personal"
            val entity = TransactionEntity(
                type = TransactionType.EXPENSE.name,
                purpose = purpose.name,
                amountPaise = amountPaise,
                category = category,
                source = sourceName,
                account = account,
                description = description.ifBlank { "$category Expense" },
                note = note,
                timestamp = timestamp,
                monthKey = CurrencyHelper.getMonthKey(timestamp)
            )
            repository.insert(entity)
            dismissDialog()
        }
    }

    fun addSettlement(
        amountPaise: Long,
        account: String,
        description: String,
        note: String?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                type = TransactionType.SETTLEMENT.name,
                purpose = FundPurpose.PERSONAL.name,
                amountPaise = amountPaise,
                category = "Settlement / Return",
                source = "Personal",
                account = account,
                description = description.ifBlank { "Returned funds to business pool" },
                note = note,
                timestamp = timestamp,
                monthKey = CurrencyHelper.getMonthKey(timestamp)
            )
            repository.insert(entity)
            dismissDialog()
        }
    }

    fun addTransfer(
        amountPaise: Long,
        fromAccount: String,
        toAccount: String,
        description: String,
        note: String?,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val entity = TransactionEntity(
                type = TransactionType.TRANSFER.name,
                purpose = FundPurpose.TRANSFER.name,
                amountPaise = amountPaise,
                category = "Transfer",
                source = fromAccount,
                account = fromAccount,
                toAccount = toAccount,
                description = description.ifBlank { "Transfer from $fromAccount to $toAccount" },
                note = note,
                timestamp = timestamp,
                monthKey = CurrencyHelper.getMonthKey(timestamp)
            )
            repository.insert(entity)
            dismissDialog()
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.update(transaction)
            dismissDialog()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(transaction)
            dismissDialog()
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun sendAiMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            message = userText.trim()
        )

        val updatedList = _chatMessages.value + userMessage
        _chatMessages.value = updatedList
        _isAiThinking.value = true

        viewModelScope.launch {
            val currentSummary = summary.value
            val recentTx = allTransactions.value
            val result = aiRepository.sendMessage(
                history = updatedList.dropLast(1),
                userMessage = userText,
                summary = currentSummary,
                recentTransactions = recentTx
            )

            _isAiThinking.value = false
            result.onSuccess { responseText ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    message = responseText
                )
            }.onFailure { error ->
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = MessageSender.ASSISTANT,
                    message = "⚠️ Could not connect to AI service: ${error.localizedMessage ?: "Unknown error"}. Please check your connection."
                )
            }
        }
    }
}
