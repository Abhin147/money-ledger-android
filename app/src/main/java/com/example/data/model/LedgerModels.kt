package com.example.data.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Transaction Type:
 * - INCOME: Money deposited or received
 * - EXPENSE: Money spent
 * - SETTLEMENT: Personal funds returned to business pool to clear debt
 * - TRANSFER: Inter-account move (Bank <-> Cash) without changing net wealth
 */
enum class TransactionType(val title: String) {
    INCOME("Income"),
    EXPENSE("Expense"),
    SETTLEMENT("Return to Business"),
    TRANSFER("Account Transfer")
}

/**
 * Fund Allocation Purpose:
 * - BUSINESS: Mother's Clothing Business
 * - PERSONAL: User's personal money & expenses
 * - OTHER: Neutral or non-classified
 * - TRANSFER: Inter-account movement
 */
enum class FundPurpose(val title: String) {
    BUSINESS("Business"),
    PERSONAL("Personal"),
    OTHER("Other"),
    TRANSFER("Transfer")
}

/**
 * Common Accounts where money physically resides
 */
object AccountType {
    const val BANK = "Bank Account"
    const val CASH = "Cash"
    const val OTHER = "Other Account"
    val all = listOf(BANK, CASH, OTHER)
}

/**
 * Predefined Business Categories
 */
object BusinessCategories {
    const val CLOTHING_SALE = "Clothing Sale"
    const val WHOLESALE = "Wholesale Order"
    const val CUSTOM_STITCHING = "Custom Stitching"
    const val FABRIC = "Fabric"
    const val PACKAGING = "Packaging"
    const val DELIVERY = "Delivery"
    const val TRANSPORT = "Transport"
    const val SUPPLIES = "Supplies"
    const val MARKETING = "Marketing"
    const val EQUIPMENT = "Equipment / Tools"
    const val OTHER = "Other Business"

    val incomeSources = listOf(
        "Mom's Clothing Business",
        CLOTHING_SALE,
        WHOLESALE,
        CUSTOM_STITCHING,
        OTHER
    )

    val expenseCategories = listOf(
        FABRIC,
        PACKAGING,
        DELIVERY,
        TRANSPORT,
        SUPPLIES,
        MARKETING,
        EQUIPMENT,
        OTHER
    )
}

/**
 * Predefined Personal Categories
 */
object PersonalCategories {
    const val SALARY = "Salary"
    const val FREELANCE = "Freelance"
    const val SAVINGS = "Personal Savings"
    const val GIFT = "Gift"
    const val FOOD = "Food & Dining"
    const val TRAVEL = "Travel"
    const val SHOPPING = "Shopping"
    const val EDUCATION = "Education"
    const val ENTERTAINMENT = "Entertainment"
    const val UTILITIES = "Bills & Utilities"
    const val HEALTHCARE = "Healthcare"
    const val RENT = "Rent"
    const val OTHER = "Other Personal"

    val incomeSources = listOf(
        "Personal",
        SALARY,
        FREELANCE,
        SAVINGS,
        GIFT,
        OTHER
    )

    val expenseCategories = listOf(
        FOOD,
        TRAVEL,
        SHOPPING,
        EDUCATION,
        ENTERTAINMENT,
        UTILITIES,
        HEALTHCARE,
        RENT,
        OTHER
    )
}

/**
 * Comprehensive calculations for the Mixed Ledger
 */
data class LedgerSummary(
    val totalAccountBalancePaise: Long = 0L,
    val bankBalancePaise: Long = 0L,
    val cashBalancePaise: Long = 0L,
    val otherBalancePaise: Long = 0L,

    // Business calculations
    val totalBusinessIncomePaise: Long = 0L,
    val totalBusinessExpensesPaise: Long = 0L,
    val netBusinessEarnedPaise: Long = 0L, // Income - Expense
    val businessMoneyUsedPersonallyPaise: Long = 0L, // Active personal deficit borrowing from business
    val businessMoneyRemainingPaise: Long = 0L, // Net Business Earned - Used Personally

    // Personal calculations
    val totalPersonalIncomePaise: Long = 0L,
    val totalPersonalExpensesPaise: Long = 0L,
    val totalReturnedToBusinessPaise: Long = 0L,
    val personalMoneyRemainingPaise: Long = 0L, // Personal money left after personal spending & settlements

    // Other non-business / non-personal
    val totalOtherIncomePaise: Long = 0L,
    val totalOtherExpensesPaise: Long = 0L,

    val transactionCount: Int = 0
)

/**
 * Monthly Summary Report Model
 */
data class MonthlyReport(
    val monthKey: String, // e.g. "2026-08"
    val monthDisplay: String, // e.g. "August 2026"
    val businessIncomePaise: Long,
    val businessExpensesPaise: Long,
    val personalIncomePaise: Long,
    val personalExpensesPaise: Long,
    val usedPersonallyPaise: Long,
    val returnedToBusinessPaise: Long,
    val businessMoneyRemainingPaise: Long,
    val categoryBreakdown: List<CategorySpendItem>
)

data class CategorySpendItem(
    val category: String,
    val purpose: FundPurpose,
    val amountPaise: Long,
    val percentage: Float
)

/**
 * Helper to format financial amounts
 */
object CurrencyHelper {
    var currencySymbol: String = "₹"

    fun formatPaise(amountPaise: Long, showSign: Boolean = false, forcePositive: Boolean = false): String {
        val displayAmount = if (forcePositive) kotlin.math.abs(amountPaise) else amountPaise
        val rupees = displayAmount / 100.0
        val isNegative = rupees < 0

        val numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            minimumFractionDigits = if (displayAmount % 100 == 0L) 0 else 2
            maximumFractionDigits = 2
        }

        val formattedNumber = numberFormat.format(kotlin.math.abs(rupees))
        val sign = when {
            isNegative -> "- "
            showSign && rupees > 0 -> "+ "
            else -> ""
        }

        return "$sign$currencySymbol$formattedNumber"
    }

    fun parseAmountToPaise(amountText: String): Long? {
        val sanitized = amountText.replace(",", "").trim()
        if (sanitized.isEmpty()) return null
        return try {
            val doubleVal = sanitized.toDouble()
            if (doubleVal < 0) null else (doubleVal * 100).toLong()
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getMonthKey(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
