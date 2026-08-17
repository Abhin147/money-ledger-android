package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.CategorySpendItem
import com.example.data.model.CurrencyHelper
import com.example.data.model.FundPurpose
import com.example.data.model.LedgerSummary
import com.example.data.model.MonthlyReport
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import kotlin.math.max

class LedgerRepository(private val dao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    val ledgerSummary: Flow<LedgerSummary> = allTransactions.map { list ->
        calculateSummary(list)
    }

    fun getTransactionsByMonth(monthKey: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsByMonth(monthKey)

    fun getTransactionById(id: Long): Flow<TransactionEntity?> =
        dao.getTransactionById(id)

    suspend fun insert(transaction: TransactionEntity): Long {
        val monthKey = if (transaction.monthKey.isEmpty()) {
            CurrencyHelper.getMonthKey(transaction.timestamp)
        } else {
            transaction.monthKey
        }
        return dao.insertTransaction(transaction.copy(monthKey = monthKey))
    }

    suspend fun update(transaction: TransactionEntity) {
        val monthKey = CurrencyHelper.getMonthKey(transaction.timestamp)
        dao.updateTransaction(transaction.copy(monthKey = monthKey))
    }

    suspend fun delete(transaction: TransactionEntity) = dao.deleteTransaction(transaction)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()

    fun calculateSummary(transactions: List<TransactionEntity>): LedgerSummary {
        var bankPaise = 0L
        var cashPaise = 0L
        var otherPaise = 0L

        var bIncome = 0L
        var bExpense = 0L
        var pIncome = 0L
        var pExpense = 0L
        var oIncome = 0L
        var oExpense = 0L
        var returnedPaise = 0L

        for (tx in transactions) {
            when (tx.type) {
                TransactionType.INCOME.name -> {
                    when (tx.account) {
                        AccountType.BANK -> bankPaise += tx.amountPaise
                        AccountType.CASH -> cashPaise += tx.amountPaise
                        else -> otherPaise += tx.amountPaise
                    }

                    when (tx.purpose) {
                        FundPurpose.BUSINESS.name -> bIncome += tx.amountPaise
                        FundPurpose.PERSONAL.name -> pIncome += tx.amountPaise
                        else -> oIncome += tx.amountPaise
                    }
                }
                TransactionType.EXPENSE.name -> {
                    when (tx.account) {
                        AccountType.BANK -> bankPaise -= tx.amountPaise
                        AccountType.CASH -> cashPaise -= tx.amountPaise
                        else -> otherPaise -= tx.amountPaise
                    }

                    when (tx.purpose) {
                        FundPurpose.BUSINESS.name -> bExpense += tx.amountPaise
                        FundPurpose.PERSONAL.name -> pExpense += tx.amountPaise
                        else -> oExpense += tx.amountPaise
                    }
                }
                TransactionType.SETTLEMENT.name -> {
                    // Settlement: Personal money repaid to business pool
                    returnedPaise += tx.amountPaise
                }
                TransactionType.TRANSFER.name -> {
                    // Moving money between physical accounts
                    when (tx.account) {
                        AccountType.BANK -> bankPaise -= tx.amountPaise
                        AccountType.CASH -> cashPaise -= tx.amountPaise
                        else -> otherPaise -= tx.amountPaise
                    }
                    when (tx.toAccount) {
                        AccountType.BANK -> bankPaise += tx.amountPaise
                        AccountType.CASH -> cashPaise += tx.amountPaise
                        else -> otherPaise += tx.amountPaise
                    }
                }
            }
        }

        val totalAccountBalance = bankPaise + cashPaise + otherPaise
        val netBusinessEarned = bIncome - bExpense

        // Business money used personally calculation
        val personalDeficit = pExpense - pIncome
        val businessUsedPersonally = if (personalDeficit > 0) {
            max(0L, personalDeficit - returnedPaise)
        } else {
            0L
        }

        val personalRemaining = if (pIncome > pExpense) {
            max(0L, (pIncome - pExpense) - returnedPaise)
        } else {
            0L
        }

        val businessRemaining = max(0L, netBusinessEarned - businessUsedPersonally)

        return LedgerSummary(
            totalAccountBalancePaise = totalAccountBalance,
            bankBalancePaise = bankPaise,
            cashBalancePaise = cashPaise,
            otherBalancePaise = otherPaise,
            totalBusinessIncomePaise = bIncome,
            totalBusinessExpensesPaise = bExpense,
            netBusinessEarnedPaise = netBusinessEarned,
            businessMoneyUsedPersonallyPaise = businessUsedPersonally,
            businessMoneyRemainingPaise = businessRemaining,
            totalPersonalIncomePaise = pIncome,
            totalPersonalExpensesPaise = pExpense,
            totalReturnedToBusinessPaise = returnedPaise,
            personalMoneyRemainingPaise = personalRemaining,
            totalOtherIncomePaise = oIncome,
            totalOtherExpensesPaise = oExpense,
            transactionCount = transactions.size
        )
    }

    fun generateMonthlyReport(monthKey: String, transactions: List<TransactionEntity>): MonthlyReport {
        val monthTransactions = transactions.filter {
            val key = if (it.monthKey.isNotEmpty()) it.monthKey else CurrencyHelper.getMonthKey(it.timestamp)
            key == monthKey
        }

        var bIncome = 0L
        var bExpense = 0L
        var pIncome = 0L
        var pExpense = 0L
        var returned = 0L

        val categoryMap = mutableMapOf<Pair<String, FundPurpose>, Long>()

        for (tx in monthTransactions) {
            when (tx.type) {
                TransactionType.INCOME.name -> {
                    if (tx.purpose == FundPurpose.BUSINESS.name) bIncome += tx.amountPaise
                    else if (tx.purpose == FundPurpose.PERSONAL.name) pIncome += tx.amountPaise
                }
                TransactionType.EXPENSE.name -> {
                    val purpose = if (tx.purpose == FundPurpose.BUSINESS.name) FundPurpose.BUSINESS else FundPurpose.PERSONAL
                    if (tx.purpose == FundPurpose.BUSINESS.name) bExpense += tx.amountPaise
                    else pExpense += tx.amountPaise

                    val cat = if (tx.category.isNotBlank()) tx.category else "Uncategorized"
                    val key = Pair(cat, purpose)
                    categoryMap[key] = (categoryMap[key] ?: 0L) + tx.amountPaise
                }
                TransactionType.SETTLEMENT.name -> {
                    returned += tx.amountPaise
                }
            }
        }

        val totalExpense = bExpense + pExpense
        val categoryBreakdown = categoryMap.map { (key, amount) ->
            CategorySpendItem(
                category = key.first,
                purpose = key.second,
                amountPaise = amount,
                percentage = if (totalExpense > 0) (amount.toFloat() / totalExpense.toFloat()) * 100f else 0f
            )
        }.sortedByDescending { it.amountPaise }

        val personalDeficit = pExpense - pIncome
        val usedPersonally = if (personalDeficit > 0) max(0L, personalDeficit - returned) else 0L
        val businessRemaining = max(0L, (bIncome - bExpense) - usedPersonally)

        val monthDisplay = if (monthTransactions.isNotEmpty()) {
            CurrencyHelper.formatMonthYear(monthTransactions.first().timestamp)
        } else {
            monthKey
        }

        return MonthlyReport(
            monthKey = monthKey,
            monthDisplay = monthDisplay,
            businessIncomePaise = bIncome,
            businessExpensesPaise = bExpense,
            personalIncomePaise = pIncome,
            personalExpensesPaise = pExpense,
            usedPersonallyPaise = usedPersonally,
            returnedToBusinessPaise = returned,
            businessMoneyRemainingPaise = businessRemaining,
            categoryBreakdown = categoryBreakdown
        )
    }

    suspend fun seedSampleData() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        fun timeDaysAgo(days: Int, hour: Int = 14, min: Int = 30): Long {
            cal.time = Date(now)
            cal.add(Calendar.DAY_OF_YEAR, -days)
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)
            return cal.timeInMillis
        }

        val sampleTransactions = listOf(
            // Business income: ₹10,000
            TransactionEntity(
                type = TransactionType.INCOME.name,
                purpose = FundPurpose.BUSINESS.name,
                amountPaise = 1000000L, // ₹10,000.00
                category = "Clothing Sale",
                source = "Mom's Clothing Business",
                account = AccountType.BANK,
                description = "Mom's bulk cotton sarees sale",
                note = "Received via UPI into personal bank account",
                timestamp = timeDaysAgo(4, 11, 15),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(4))
            ),
            // Personal income: ₹5,000
            TransactionEntity(
                type = TransactionType.INCOME.name,
                purpose = FundPurpose.PERSONAL.name,
                amountPaise = 500000L, // ₹5,000.00
                category = "Freelance",
                source = "Personal",
                account = AccountType.BANK,
                description = "Graphic design freelance gig",
                note = "Direct client payment",
                timestamp = timeDaysAgo(3, 15, 0),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(3))
            ),
            // Personal spending: ₹6,000 (Exceeds personal 5,000 by 1,000 -> Uses business money!)
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                purpose = FundPurpose.PERSONAL.name,
                amountPaise = 600000L, // ₹6,000.00
                category = "Shopping",
                source = "Personal",
                account = AccountType.BANK,
                description = "Monthly groceries & new headphones",
                note = "Used bank card (Spent ₹1,000 from Mom's business fund)",
                timestamp = timeDaysAgo(2, 19, 45),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(2))
            ),
            // Returned to business: ₹500
            TransactionEntity(
                type = TransactionType.SETTLEMENT.name,
                purpose = FundPurpose.PERSONAL.name,
                amountPaise = 50000L, // ₹500.00
                category = "Settlement",
                source = "Personal",
                account = AccountType.BANK,
                description = "Partial return of borrowed money to business",
                note = "Reduced business money used personally to ₹500",
                timestamp = timeDaysAgo(1, 10, 30),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(1))
            ),
            // Business expense: Fabric ₹1,200
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                purpose = FundPurpose.BUSINESS.name,
                amountPaise = 120000L, // ₹1,200.00
                category = "Fabric",
                source = "Mom's Clothing Business",
                account = AccountType.BANK,
                description = "Raw silk fabric rolls for tailoring",
                note = "Invoice #8841",
                timestamp = timeDaysAgo(1, 16, 20),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(1))
            ),
            // Business expense: Packaging ₹350
            TransactionEntity(
                type = TransactionType.EXPENSE.name,
                purpose = FundPurpose.BUSINESS.name,
                amountPaise = 35000L, // ₹350.00
                category = "Packaging",
                source = "Mom's Clothing Business",
                account = AccountType.BANK,
                description = "Parcel boxes & courier polybags",
                note = "Order of 50 boxes",
                timestamp = timeDaysAgo(0, 9, 15),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(0))
            ),
            // Cash withdrawal transfer: Bank -> Cash ₹2,000
            TransactionEntity(
                type = TransactionType.TRANSFER.name,
                purpose = FundPurpose.TRANSFER.name,
                amountPaise = 200000L, // ₹2,000.00
                category = "ATM Withdrawal",
                source = AccountType.BANK,
                account = AccountType.BANK,
                toAccount = AccountType.CASH,
                description = "ATM cash withdrawal for market expenses",
                note = "Moved between accounts; doesn't affect total wealth",
                timestamp = timeDaysAgo(0, 10, 0),
                monthKey = CurrencyHelper.getMonthKey(timeDaysAgo(0))
            )
        )

        dao.clearAll()
        dao.insertAll(sampleTransactions)
    }
}
