package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FundPurpose
import com.example.data.model.TransactionType

/**
 * Room Entity representing every incoming, outgoing, settlement, or transfer transaction.
 * All amounts are stored accurately as Long in minor units (paise/cents).
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // INCOME, EXPENSE, SETTLEMENT, TRANSFER
    val type: String = TransactionType.EXPENSE.name,

    // BUSINESS, PERSONAL, OTHER, TRANSFER
    val purpose: String = FundPurpose.PERSONAL.name,

    // Amount stored in paise (1 INR = 100 paise)
    val amountPaise: Long = 0L,

    // Category (e.g., Fabric, Food, Delivery, Clothing Sale, Salary, Settlement)
    val category: String = "",

    // Source or Beneficiary (e.g., "Mom's Clothing Business", "Personal", "Vendor X")
    val source: String = "",

    // Account where money is located: "Bank Account", "Cash", "Other Account"
    val account: String = "Bank Account",

    // Destination account (used if type == TRANSFER)
    val toAccount: String? = null,

    // Short summary description (e.g. "Clothing sale", "Fabric purchase")
    val description: String = "",

    // Optional detailed note
    val note: String? = null,

    // Milliseconds epoch timestamp
    val timestamp: Long = System.currentTimeMillis(),

    // "yyyy-MM" format for fast monthly report grouping and indexing
    val monthKey: String = ""
)
