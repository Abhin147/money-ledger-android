package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.CategorySpendItem
import com.example.data.model.CurrencyHelper
import com.example.data.model.FundPurpose
import com.example.data.model.LedgerSummary
import com.example.data.model.TransactionType
import com.example.ui.theme.*

@Composable
fun HeroAccountCard(
    summary: LedgerSummary,
    businessName: String,
    onSettleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Main Card: Total Account Balance (Clean Sky Blue Container #D3E3FD)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_account_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CleanBlueContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOTAL ACCOUNT BALANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold,
                        color = CleanOnBlueContainer.copy(alpha = 0.75f)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyHelper.formatPaise(summary.totalAccountBalancePaise),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CleanOnBlueContainer,
                        fontSize = 36.sp,
                        letterSpacing = (-0.5).sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account breakdown pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CleanOnBlueContainer.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = CleanOnBlueContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bank: ${CurrencyHelper.formatPaise(summary.bankBalancePaise)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CleanOnBlueContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CleanOnBlueContainer.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = CleanOnBlueContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cash: ${CurrencyHelper.formatPaise(summary.cashBalancePaise)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CleanOnBlueContainer,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2-Column Split Cards: Business Money vs Personal Money (Clean Minimalist White Cards)
        val totalSplit = (summary.businessMoneyRemainingPaise + summary.personalMoneyRemainingPaise).coerceAtLeast(1L)
        val businessRatio = (summary.businessMoneyRemainingPaise.toFloat() / totalSplit).coerceIn(0.05f, 0.95f)
        val personalRatio = (summary.personalMoneyRemainingPaise.toFloat() / totalSplit).coerceIn(0.05f, 0.95f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Business Money Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BUSINESS MONEY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CleanTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyHelper.formatPaise(summary.businessMoneyRemainingPaise),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Clean progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CleanSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(businessRatio)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CleanBluePrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Remaining for Mom",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CleanTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Personal Money Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CleanBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PERSONAL MONEY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CleanTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyHelper.formatPaise(summary.personalMoneyRemainingPaise),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Clean progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CleanSurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(personalRatio)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(CleanBluePrimary)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (summary.businessMoneyUsedPersonallyPaise > 0) "In deficit" else "Yours to spend",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (summary.businessMoneyUsedPersonallyPaise > 0) CleanAlertText else CleanTextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Warning Card if Business Money was used personally
        AnimatedVisibility(visible = summary.businessMoneyUsedPersonallyPaise > 0L) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CleanAlertBg),
                border = BorderStroke(1.dp, CleanAlertText.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BUSINESS USED PERSONALLY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CleanAlertText.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyHelper.formatPaise(summary.businessMoneyUsedPersonallyPaise),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CleanAlertText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }

                    Surface(
                        onClick = onSettleClick,
                        shape = RoundedCornerShape(50),
                        color = CleanAlertBadge,
                        modifier = Modifier.testTag("settle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Warning • Return ₹",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CleanAlertText
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatsGrid(
    summary: LedgerSummary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "INCOME & EXPENSES BREAKDOWN",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Business Stats
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BusinessCenter,
                            contentDescription = null,
                            tint = Amber600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Business Ledger",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Amber700
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    StatRow(
                        label = "Income",
                        amount = CurrencyHelper.formatPaise(summary.totalBusinessIncomePaise),
                        isPositive = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatRow(
                        label = "Expenses",
                        amount = CurrencyHelper.formatPaise(summary.totalBusinessExpensesPaise),
                        isPositive = false
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatRow(
                        label = "Net Earned",
                        amount = CurrencyHelper.formatPaise(summary.netBusinessEarnedPaise),
                        isNeutral = true
                    )
                }
            }

            // Personal Stats
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Indigo600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Personal Ledger",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Indigo700
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    StatRow(
                        label = "Income",
                        amount = CurrencyHelper.formatPaise(summary.totalPersonalIncomePaise),
                        isPositive = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatRow(
                        label = "Expenses",
                        amount = CurrencyHelper.formatPaise(summary.totalPersonalExpensesPaise),
                        isPositive = false
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatRow(
                        label = "Returned",
                        amount = CurrencyHelper.formatPaise(summary.totalReturnedToBusinessPaise),
                        isPositive = true
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    amount: String,
    isPositive: Boolean = false,
    isNeutral: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        val color = when {
            isNeutral -> MaterialTheme.colorScheme.onSurface
            isPositive -> Emerald700
            else -> Rose600
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME.name
    val isSettlement = transaction.type == TransactionType.SETTLEMENT.name
    val isTransfer = transaction.type == TransactionType.TRANSFER.name
    val isBusiness = transaction.purpose == FundPurpose.BUSINESS.name

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Pill
            val iconBg = when {
                isTransfer -> Slate200
                isSettlement -> Emerald100
                isIncome -> if (isBusiness) Amber100 else Indigo100
                else -> if (isBusiness) Amber100 else Rose100
            }

            val iconTint = when {
                isTransfer -> Slate700
                isSettlement -> Emerald700
                isIncome -> if (isBusiness) Amber700 else Indigo700
                else -> if (isBusiness) Amber700 else Rose700
            }

            val iconVector = when {
                isTransfer -> Icons.Default.SwapHoriz
                isSettlement -> Icons.Default.Handshake
                isIncome -> Icons.Default.ArrowDownward
                else -> Icons.Default.ArrowUpward
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                val titleText = transaction.description.ifBlank { transaction.category }.ifBlank { "Transaction" }
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Purpose Badge
                    PurposeBadge(purpose = transaction.purpose, type = transaction.type)

                    // Optional Category text if distinct from title
                    val showCategory = transaction.category.isNotBlank() &&
                            !transaction.category.equals(titleText, ignoreCase = true)

                    if (showCategory) {
                        Text(
                            text = "• ${transaction.category}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    // Account text (e.g. Bank Account, Cash, or Transfer destination)
                    val accountDisplay = if (isTransfer && !transaction.toAccount.isNullOrBlank()) {
                        "${transaction.account} → ${transaction.toAccount}"
                    } else {
                        transaction.account
                    }

                    if (accountDisplay.isNotBlank()) {
                        Text(
                            text = "• $accountDisplay",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount & Date
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                val amountColor = when {
                    isTransfer -> MaterialTheme.colorScheme.onSurface
                    isSettlement -> Emerald700
                    isIncome -> Emerald700
                    else -> Rose600
                }

                val prefix = when {
                    isTransfer -> ""
                    isSettlement -> "↩ "
                    isIncome -> "+ "
                    else -> "- "
                }

                Text(
                    text = prefix + CurrencyHelper.formatPaise(transaction.amountPaise),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    ),
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = CurrencyHelper.formatDate(transaction.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun PurposeBadge(purpose: String, type: String) {
    val isBusiness = purpose == FundPurpose.BUSINESS.name
    val isTransfer = type == TransactionType.TRANSFER.name
    val isSettlement = type == TransactionType.SETTLEMENT.name

    val (bg, textColor, label) = when {
        isTransfer -> Triple(Slate200, Slate700, "Transfer")
        isSettlement -> Triple(Emerald100, Emerald800, "Settlement")
        isBusiness -> Triple(Amber100, Amber800, "Business")
        else -> Triple(Indigo100, Indigo700, "Personal")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun CategoryBarChart(
    items: List<CategorySpendItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spending recorded for this month yet.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "SPENDING BY CATEGORY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            items.take(7).forEach { item ->
                val barColor = if (item.purpose == FundPurpose.BUSINESS) Amber500 else Indigo500
                val progress by animateFloatAsState(
                    targetValue = (item.percentage / 100f).coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 600),
                    label = "bar_progress"
                )

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(barColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PurposeBadge(purpose = item.purpose.name, type = TransactionType.EXPENSE.name)
                        }

                        Text(
                            text = "${CurrencyHelper.formatPaise(item.amountPaise)} (${item.percentage.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyPlaceholder(
    title: String,
    subtitle: String,
    icon: ImageVector = Icons.Default.Category,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
