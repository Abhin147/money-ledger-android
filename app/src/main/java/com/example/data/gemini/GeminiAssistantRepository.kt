package com.example.data.gemini

import com.example.BuildConfig
import com.example.data.local.TransactionEntity
import com.example.data.model.CurrencyHelper
import com.example.data.model.LedgerSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAssistantRepository(private val apiService: GeminiApiService = GeminiApiService.create()) {

    fun buildSystemInstruction(summary: LedgerSummary, recentTransactions: List<TransactionEntity>): GeminiContent {
        val txSummary = recentTransactions.take(8).joinToString("\n") { tx ->
            "- [${tx.type}] ${CurrencyHelper.formatPaise(tx.amountPaise)} (${tx.purpose} - ${tx.category}): ${tx.description} on ${CurrencyHelper.formatDate(tx.timestamp)}"
        }

        val promptText = """
            You are the specialized AI Financial Assistant for the "Money Ledger" Android application.
            The user tracks personal money and their mother's clothing-selling business money which are mixed in the same bank account.
            
            Current Live Ledger Financial Snapshot:
            - Total Account Balance: ${CurrencyHelper.formatPaise(summary.totalAccountBalancePaise)} (Bank: ${CurrencyHelper.formatPaise(summary.bankBalancePaise)}, Cash: ${CurrencyHelper.formatPaise(summary.cashBalancePaise)})
            - Business Money Remaining: ${CurrencyHelper.formatPaise(summary.businessMoneyRemainingPaise)}
            - Business Money Used Personally (Personal Deficit): ${CurrencyHelper.formatPaise(summary.businessMoneyUsedPersonallyPaise)}
            - Total Business Income: ${CurrencyHelper.formatPaise(summary.totalBusinessIncomePaise)}
            - Total Business Expenses: ${CurrencyHelper.formatPaise(summary.totalBusinessExpensesPaise)}
            - Total Personal Income: ${CurrencyHelper.formatPaise(summary.totalPersonalIncomePaise)}
            - Total Personal Expenses: ${CurrencyHelper.formatPaise(summary.totalPersonalExpensesPaise)}
            - Money Returned to Business: ${CurrencyHelper.formatPaise(summary.totalReturnedToBusinessPaise)}
            - Personal Money Remaining: ${CurrencyHelper.formatPaise(summary.personalMoneyRemainingPaise)}
            
            Recent Transactions:
            $txSummary
            
            Core Principles:
            1. Your primary job is to help the user answer: "How much of the money currently in my account belongs to Mom's clothing business, how much is mine, and how much business money have I used personally?"
            2. If "Business Money Used Personally" is greater than zero, guide the user on returning/settling that amount so the business funds stay intact.
            3. Keep answers clear, supportive, concise, and structured with bold highlights and bullet points. Use Indian Rupee (${CurrencyHelper.currencySymbol}) formatting.
            4. Help the user categorize expenses (e.g. fabric vs delivery vs personal shopping) or plan settlements when requested.
        """.trimIndent()

        return GeminiContent(
            role = "system",
            parts = listOf(GeminiPart(text = promptText))
        )
    }

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        summary: LedgerSummary,
        recentTransactions: List<TransactionEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide an intelligent local fallback so the user always receives full functionality
            val localResponse = generateLocalAnalysis(userMessage, summary)
            return@withContext Result.success(localResponse)
        }

        try {
            val contentList = mutableListOf<GeminiContent>()

            // Convert conversation history
            for (msg in history) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                contentList.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = msg.message))))
            }

            // Append current message
            contentList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMessage))))

            val request = GeminiRequest(
                contents = contentList,
                systemInstruction = buildSystemInstruction(summary, recentTransactions),
                generationConfig = GeminiGenerationConfig(temperature = 0.6f)
            )

            val response = apiService.generateContent(apiKey = apiKey, request = request)

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                val errorMsg = response.error?.message ?: "No response generated."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // If network fails or quota exceeded, fallback to smart offline analysis
            val fallback = generateLocalAnalysis(userMessage, summary)
            Result.success(fallback)
        }
    }

    private fun generateLocalAnalysis(prompt: String, summary: LedgerSummary): String {
        val lower = prompt.lowercase()
        val cur = CurrencyHelper.currencySymbol
        val busRem = CurrencyHelper.formatPaise(summary.businessMoneyRemainingPaise)
        val usedPers = CurrencyHelper.formatPaise(summary.businessMoneyUsedPersonallyPaise)
        val persRem = CurrencyHelper.formatPaise(summary.personalMoneyRemainingPaise)
        val totBal = CurrencyHelper.formatPaise(summary.totalAccountBalancePaise)

        return when {
            lower.contains("business") || lower.contains("mom") || lower.contains("clothing") -> {
                """
                📊 **Mom's Clothing Business Breakdown:**
                - **Business Money Remaining:** $busRem
                - **Total Business Income:** ${CurrencyHelper.formatPaise(summary.totalBusinessIncomePaise)}
                - **Total Business Expenses:** ${CurrencyHelper.formatPaise(summary.totalBusinessExpensesPaise)}
                - **Business Net Earned:** ${CurrencyHelper.formatPaise(summary.netBusinessEarnedPaise)}
                
                💡 *Tip:* Out of your total account balance of $totBal, exactly **$busRem** is owned by Mom's business.
                """.trimIndent()
            }
            lower.contains("owe") || lower.contains("used") || lower.contains("debt") || lower.contains("settle") || lower.contains("return") -> {
                if (summary.businessMoneyUsedPersonallyPaise > 0L) {
                    """
                    ⚠️ **Business Money Used Personally:**
                    You currently have **$usedPers** borrowed from Mom's business funds.
                    
                    **To settle:**
                    1. Tap **"Settle / Return"** on the dashboard.
                    2. Deposit or allocate **$usedPers** back to the business ledger.
                    3. Once settled, your Business Money Remaining will increase back to its full balance.
                    """.trimIndent()
                } else {
                    """
                    ✅ **No Business Debt!**
                    You haven't used any of Mom's business money for personal expenses. All personal spending was covered by your own personal income.
                    """.trimIndent()
                }
            }
            lower.contains("personal") || lower.contains("my money") || lower.contains("mine") -> {
                """
                👤 **Your Personal Money Status:**
                - **Personal Money Remaining:** $persRem
                - **Total Personal Income:** ${CurrencyHelper.formatPaise(summary.totalPersonalIncomePaise)}
                - **Total Personal Expenses:** ${CurrencyHelper.formatPaise(summary.totalPersonalExpensesPaise)}
                ${if (summary.businessMoneyUsedPersonallyPaise > 0) "- ⚠️ Personal Deficit: $usedPers borrowed from business" else "- ✅ Personal balance is healthy"}
                """.trimIndent()
            }
            else -> {
                """
                💼 **Money Ledger Snapshot:**
                - **Total Account Balance:** $totBal
                - **Mom's Business Remaining:** $busRem
                - **Your Personal Remaining:** $persRem
                - **Business Money Used Personally:** $usedPers
                
                You can ask me to analyze specific categories (Fabric, Delivery, Food), help settle debts, or calculate monthly profits!
                """.trimIndent()
            }
        }
    }
}
