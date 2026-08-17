package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CurrencyHelper
import com.example.data.model.MonthlyReport
import com.example.ui.components.CategoryBarChart
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo700
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.theme.Rose700
import com.example.ui.viewmodel.LedgerViewModel

@Composable
fun ReportsScreen(
    viewModel: LedgerViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableMonths.collectAsStateWithLifecycle()
    val selectedMonthKey by viewModel.selectedMonthKey.collectAsStateWithLifecycle()
    val businessName by viewModel.businessName.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Chips
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                availableMonths.forEach { monthKey ->
                    FilterChip(
                        selected = selectedMonthKey == monthKey,
                        onClick = { viewModel.setSelectedMonth(monthKey) },
                        label = { Text(monthKey) }
                    )
                }
            }
        }

        // Monthly Overview Card
        item {
            MonthlySummaryCard(report = report, businessName = businessName)
        }

        // Business vs Personal P&L
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Business Column
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
                                tint = Amber700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Business",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Amber700
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportStat(label = "Income", amount = CurrencyHelper.formatPaise(report.businessIncomePaise), color = Emerald700)
                        ReportStat(label = "Expenses", amount = CurrencyHelper.formatPaise(report.businessExpensesPaise), color = Rose600)
                        val net = report.businessIncomePaise - report.businessExpensesPaise
                        ReportStat(label = "Net Margin", amount = CurrencyHelper.formatPaise(net), color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Personal Column
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
                                tint = Indigo700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Personal",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Indigo700
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportStat(label = "Income", amount = CurrencyHelper.formatPaise(report.personalIncomePaise), color = Emerald700)
                        ReportStat(label = "Expenses", amount = CurrencyHelper.formatPaise(report.personalExpensesPaise), color = Rose600)
                        val deficit = report.personalExpensesPaise - report.personalIncomePaise
                        val netLabel = if (deficit > 0) "Deficit" else "Savings"
                        val netColor = if (deficit > 0) Rose600 else Emerald700
                        ReportStat(label = netLabel, amount = CurrencyHelper.formatPaise(kotlin.math.abs(deficit)), color = netColor)
                    }
                }
            }
        }

        // Category Spending Breakdown
        item {
            CategoryBarChart(items = report.categoryBreakdown)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MonthlySummaryCard(
    report: MonthlyReport,
    businessName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_summary_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.monthDisplay.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald100
                ) {
                    Text(
                        text = "Monthly Report",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Emerald800
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Highlight: Business Money Remaining
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BUSINESS MONEY REMAINING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyHelper.formatPaise(report.businessMoneyRemainingPaise),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald700
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown Rows
            SummaryLine(
                label = "Business Income",
                value = CurrencyHelper.formatPaise(report.businessIncomePaise),
                color = Emerald700
            )
            SummaryLine(
                label = "Business Expenses",
                value = "- " + CurrencyHelper.formatPaise(report.businessExpensesPaise),
                color = Rose600
            )
            SummaryLine(
                label = "Used Personally",
                value = if (report.usedPersonallyPaise > 0) "- " + CurrencyHelper.formatPaise(report.usedPersonallyPaise) else CurrencyHelper.formatPaise(0),
                color = if (report.usedPersonallyPaise > 0) Rose600 else MaterialTheme.colorScheme.onSurface
            )
            SummaryLine(
                label = "Returned to Business",
                value = if (report.returnedToBusinessPaise > 0) "+ " + CurrencyHelper.formatPaise(report.returnedToBusinessPaise) else CurrencyHelper.formatPaise(0),
                color = Emerald700
            )
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
private fun ReportStat(label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
