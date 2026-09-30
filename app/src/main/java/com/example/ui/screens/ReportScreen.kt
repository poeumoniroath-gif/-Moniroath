package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DaySalesStat
import com.example.ui.MonthSalesStat
import com.example.ui.ProductSaleSummary
import com.example.ui.ReportPeriod
import com.example.ui.SalesViewModel
import com.example.util.Formatters
import com.example.util.SyncState

@Composable
fun ReportScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPeriod by viewModel.selectedReportPeriod.collectAsStateWithLifecycle()
    val cloudConfig by viewModel.cloudConfig.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    // Daily States
    val selectedDate by viewModel.selectedReportDate.collectAsStateWithLifecycle()
    val allDates by viewModel.allSaleDates.collectAsStateWithLifecycle()
    val dailySales by viewModel.selectedDateSales.collectAsStateWithLifecycle()
    val dailyCashRevenue by viewModel.selectedDateCashRevenue.collectAsStateWithLifecycle()
    val dailyAbaRevenue by viewModel.selectedDateAbaRevenue.collectAsStateWithLifecycle()
    val dailyProductSummaries by viewModel.selectedDateProductSummaries.collectAsStateWithLifecycle()
    val dailyClosure by viewModel.selectedDateClosure.collectAsStateWithLifecycle()

    // Weekly States
    val selectedWeeklyDate by viewModel.selectedWeeklyDate.collectAsStateWithLifecycle()
    val weeklyRevenue by viewModel.weeklyRevenue.collectAsStateWithLifecycle()
    val weeklyCashRevenue by viewModel.weeklyCashRevenue.collectAsStateWithLifecycle()
    val weeklyAbaRevenue by viewModel.weeklyAbaRevenue.collectAsStateWithLifecycle()
    val weeklyItemsCount by viewModel.weeklyItemsCount.collectAsStateWithLifecycle()
    val weeklyTransactionsCount by viewModel.weeklyTransactionsCount.collectAsStateWithLifecycle()
    val weeklyDailyBreakdown by viewModel.weeklyDailyBreakdown.collectAsStateWithLifecycle()
    val weeklyProductSummaries by viewModel.weeklyProductSummaries.collectAsStateWithLifecycle()

    // Monthly States
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthlyRevenue by viewModel.monthlyRevenue.collectAsStateWithLifecycle()
    val monthlyCashRevenue by viewModel.monthlyCashRevenue.collectAsStateWithLifecycle()
    val monthlyAbaRevenue by viewModel.monthlyAbaRevenue.collectAsStateWithLifecycle()
    val monthlyItemsCount by viewModel.monthlyItemsCount.collectAsStateWithLifecycle()
    val monthlyTransactionsCount by viewModel.monthlyTransactionsCount.collectAsStateWithLifecycle()
    val monthlyDailyBreakdown by viewModel.monthlyDailyBreakdown.collectAsStateWithLifecycle()
    val monthlyProductSummaries by viewModel.monthlyProductSummaries.collectAsStateWithLifecycle()

    // Annual States
    val selectedAnnualYear by viewModel.selectedAnnualYear.collectAsStateWithLifecycle()
    val annualRevenue by viewModel.annualRevenue.collectAsStateWithLifecycle()
    val annualCashRevenue by viewModel.annualCashRevenue.collectAsStateWithLifecycle()
    val annualAbaRevenue by viewModel.annualAbaRevenue.collectAsStateWithLifecycle()
    val annualItemsCount by viewModel.annualItemsCount.collectAsStateWithLifecycle()
    val annualTransactionsCount by viewModel.annualTransactionsCount.collectAsStateWithLifecycle()
    val annualMonthlyBreakdown by viewModel.annualMonthlyBreakdown.collectAsStateWithLifecycle()
    val annualProductSummaries by viewModel.annualProductSummaries.collectAsStateWithLifecycle()
    val allAvailableYears by viewModel.allAvailableYears.collectAsStateWithLifecycle()

    val dailyTotalRevenue = dailySales.sumOf { it.totalPrice.toLong() }
    val dailyTotalItems = dailySales.sumOf { it.quantity }
    val dailyTotalTransactions = dailySales.size
    val isDateToday = selectedDate == Formatters.getTodayIsoString()

    var showCloseDayDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Feedback message banner if any
        if (feedbackMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (feedbackMessage?.isSuccess == true) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (feedbackMessage?.isSuccess == true) Color(0xFF86EFAC) else Color(0xFFFECACA)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearFeedback() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (feedbackMessage?.isSuccess == true) Icons.Default.CheckCircle else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = if (feedbackMessage?.isSuccess == true) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feedbackMessage?.message.orEmpty(),
                            color = if (feedbackMessage?.isSuccess == true) Color(0xFF15803D) else Color(0xFFB91C1C),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Top Period Selector Tabs (Daily, Weekly, Annually)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReportPeriod.values().forEach { period ->
                        val isSelected = currentPeriod == period
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setReportPeriod(period) }
                                .testTag("report_period_tab_${period.name.lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = period.iconEmoji,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = period.titleKh,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = period.titleEn,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFFF1F5F9) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- PERIOD CONTENT DISPATCHER ---
        when (currentPeriod) {
            ReportPeriod.DAILY -> {
                // Date Picker Carousel
                item {
                    Column {
                        Text(
                            text = "ជ្រើសរើសកាលបរិច្ឆេទ (Select Date)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allDates) { dateStr ->
                                val isSelected = dateStr == selectedDate
                                val isDateEntryToday = dateStr == Formatters.getTodayIsoString()
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectReportDate(dateStr) }
                                        .testTag("date_chip_$dateStr")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isDateEntryToday) "ថ្ងៃនេះ ($dateStr)" else dateStr,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Date Title Banner
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isDateToday) "របាយការណ៍លក់ប្រចាំថ្ងៃ (ថ្ងៃនេះ)" else "របាយការណ៍លក់ប្រចាំថ្ងៃ",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = Formatters.formatDateToKhmer(selectedDate),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }

                            if (dailyClosure != null) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF10B981),
                                    contentColor = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "បានបិទបញ្ជី",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Daily KPI Cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiCard(
                            title = "ប្រាក់ចំណូលសរុបប្រចាំថ្ងៃ (Total Daily Revenue)",
                            value = Formatters.formatRiel(dailyTotalRevenue),
                            subtitle = Formatters.formatUsd(dailyTotalRevenue),
                            icon = Icons.Default.MonetizationOn,
                            backgroundColor = Color(0xFF0F172A),
                            contentColor = Color(0xFFFFD54F),
                            titleColor = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "សាច់ប្រាក់ (Cash)",
                                    value = Formatters.formatRiel(dailyCashRevenue),
                                    emoji = "💵",
                                    backgroundColor = Color(0xFFF0FDF4),
                                    contentColor = Color(0xFF059669),
                                    titleColor = Color(0xFF166534)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ABA Pay (ABA)",
                                    value = Formatters.formatRiel(dailyAbaRevenue),
                                    emoji = "📲",
                                    backgroundColor = Color(0xFFF0F9FF),
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF075985)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ចំនួនទំនិញលក់",
                                    value = "$dailyTotalItems មុខ",
                                    icon = Icons.Default.ShoppingBag,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF64748B)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ចំនួនលើកលក់",
                                    value = "$dailyTotalTransactions លើក",
                                    icon = Icons.Default.PointOfSale,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = Color(0xFF7C3AED),
                                    titleColor = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                // Daily Product Breakdown Table Section
                item {
                    ProductBreakdownCard(
                        title = "តារាងលម្អិតតាមមុខទំនិញ (Daily Product Breakdown)",
                        productSummaries = dailyProductSummaries
                    )
                }

                // Close Day Action Button
                item {
                    if (isDateToday) {
                        if (dailyClosure == null) {
                            Button(
                                onClick = { showCloseDayDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("end_day_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF334155),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "បញ្ចប់ការលក់ថ្ងៃនេះ (បិទបញ្ជី)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "បានបិទបញ្ជីលក់សម្រាប់ថ្ងៃនេះរួចរាល់",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D),
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "ទិន្នន័យទាំងអស់ត្រូវបានរក្សាទុកក្នុងប្រព័ន្ធដោយសុវត្ថិភាព",
                                            color = Color(0xFF166534),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ReportPeriod.WEEKLY -> {
                // Weekly Navigator Bar
                item {
                    val (startIso, endIso) = Formatters.getWeekBoundaries(selectedWeeklyDate)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.previousWeek() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week")
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "របាយការណ៍ប្រចាំសប្ដាហ៍ (Weekly Report)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "$startIso ដល់ $endIso",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.nextWeek() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Week")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.resetToCurrentWeek() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("សប្ដាហ៍នេះ (This Week)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Weekly Summary KPI Cards
                item {
                    val avgDaily = if (weeklyRevenue > 0L) weeklyRevenue / 7 else 0L
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiCard(
                            title = "ចំណូលសរុបប្រចាំសប្ដាហ៍ (Total Weekly Revenue)",
                            value = Formatters.formatRiel(weeklyRevenue),
                            subtitle = Formatters.formatUsd(weeklyRevenue),
                            icon = Icons.Default.MonetizationOn,
                            backgroundColor = Color(0xFF0F172A),
                            contentColor = Color(0xFFFFD54F),
                            titleColor = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "មធ្យម/ថ្ងៃ (Avg Daily)",
                                    value = Formatters.formatRiel(avgDaily),
                                    icon = Icons.Default.TrendingUp,
                                    backgroundColor = Color(0xFFFAF5FF),
                                    contentColor = Color(0xFF9333EA),
                                    titleColor = Color(0xFF6B21A8)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ទំនិញលក់បាន",
                                    value = "$weeklyItemsCount កែវ",
                                    icon = Icons.Default.ShoppingBag,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "សាច់ប្រាក់ (Cash)",
                                    value = Formatters.formatRiel(weeklyCashRevenue),
                                    emoji = "💵",
                                    backgroundColor = Color(0xFFF0FDF4),
                                    contentColor = Color(0xFF059669),
                                    titleColor = Color(0xFF166534)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ABA Pay",
                                    value = Formatters.formatRiel(weeklyAbaRevenue),
                                    emoji = "📲",
                                    backgroundColor = Color(0xFFF0F9FF),
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF075985)
                                )
                            }
                        }
                    }
                }

                // Weekly 7-Day Day-by-Day Breakdown
                item {
                    WeeklyBreakdownCard(dailyBreakdown = weeklyDailyBreakdown)
                }

                // Weekly Top Selling Products
                item {
                    ProductBreakdownCard(
                        title = "ទំនិញលក់ដាច់ប្រចាំសប្ដាហ៍ (Weekly Top Sellers)",
                        productSummaries = weeklyProductSummaries
                    )
                }
            }

            ReportPeriod.MONTHLY -> {
                // Monthly Navigator Bar
                item {
                    val monthKhmerTitle = Formatters.formatMonthYearKhmer(selectedMonthYear, selectedMonth)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.previousMonth() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "របាយការណ៍ប្រចាំខែ (Monthly Report)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = monthKhmerTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.nextMonth() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.resetToCurrentMonth() },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ខែនេះ (This Month)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Monthly Summary KPI Cards
                item {
                    val activeDaysCount = monthlyDailyBreakdown.size.coerceAtLeast(1)
                    val avgDaily = if (monthlyRevenue > 0L) monthlyRevenue / activeDaysCount else 0L
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiCard(
                            title = "ចំណូលសរុបប្រចាំខែ (Total Monthly Revenue)",
                            value = Formatters.formatRiel(monthlyRevenue),
                            subtitle = Formatters.formatUsd(monthlyRevenue),
                            icon = Icons.Default.MonetizationOn,
                            backgroundColor = Color(0xFF0F172A),
                            contentColor = Color(0xFFFFD54F),
                            titleColor = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "មធ្យម/ថ្ងៃ (Avg Daily)",
                                    value = Formatters.formatRiel(avgDaily),
                                    icon = Icons.Default.TrendingUp,
                                    backgroundColor = Color(0xFFFAF5FF),
                                    contentColor = Color(0xFF9333EA),
                                    titleColor = Color(0xFF6B21A8)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ទំនិញលក់បាន",
                                    value = "$monthlyItemsCount កែវ",
                                    icon = Icons.Default.ShoppingBag,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "សាច់ប្រាក់ (Cash)",
                                    value = Formatters.formatRiel(monthlyCashRevenue),
                                    emoji = "💵",
                                    backgroundColor = Color(0xFFF0FDF4),
                                    contentColor = Color(0xFF059669),
                                    titleColor = Color(0xFF166534)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ABA Pay",
                                    value = Formatters.formatRiel(monthlyAbaRevenue),
                                    emoji = "📲",
                                    backgroundColor = Color(0xFFF0F9FF),
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF075985)
                                )
                            }
                        }
                    }
                }

                // Monthly Daily Breakdown Card
                item {
                    MonthlyDayBreakdownCard(dailyBreakdown = monthlyDailyBreakdown)
                }

                // Monthly Top-Selling Products
                item {
                    ProductBreakdownCard(
                        title = "ទំនិញលក់ដាច់ប្រចាំខែ (Monthly Top Sellers)",
                        productSummaries = monthlyProductSummaries
                    )
                }
            }

            ReportPeriod.ANNUALLY -> {
                // Annual Year Selector Bar
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.previousYear() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Year")
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "របាយការណ៍ប្រចាំឆ្នាំ (Annual Report)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "ឆ្នាំ $selectedAnnualYear (Year $selectedAnnualYear)",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.nextYear() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Year")
                                }
                            }

                            if (allAvailableYears.size > 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(allAvailableYears) { yr ->
                                        val isYrSelected = yr == selectedAnnualYear
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isYrSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectAnnualYear(yr) }
                                        ) {
                                            Text(
                                                text = "$yr",
                                                color = if (isYrSelected) Color.White else Color(0xFF334155),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Annual Summary KPI Cards
                item {
                    val avgMonthly = if (annualRevenue > 0L) annualRevenue / 12 else 0L
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiCard(
                            title = "ចំណូលសរុបប្រចាំឆ្នាំ $selectedAnnualYear (Annual Revenue)",
                            value = Formatters.formatRiel(annualRevenue),
                            subtitle = Formatters.formatUsd(annualRevenue),
                            icon = Icons.Default.MonetizationOn,
                            backgroundColor = Color(0xFF0F172A),
                            contentColor = Color(0xFFFFD54F),
                            titleColor = Color(0xFF94A3B8)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "មធ្យម/ខែ (Avg Monthly)",
                                    value = Formatters.formatRiel(avgMonthly),
                                    icon = Icons.Default.TrendingUp,
                                    backgroundColor = Color(0xFFFAF5FF),
                                    contentColor = Color(0xFF9333EA),
                                    titleColor = Color(0xFF6B21A8)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ទំនិញលក់សរុប",
                                    value = "$annualItemsCount កែវ",
                                    icon = Icons.Default.ShoppingBag,
                                    backgroundColor = MaterialTheme.colorScheme.surface,
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "សាច់ប្រាក់ (Cash)",
                                    value = Formatters.formatRiel(annualCashRevenue),
                                    emoji = "💵",
                                    backgroundColor = Color(0xFFF0FDF4),
                                    contentColor = Color(0xFF059669),
                                    titleColor = Color(0xFF166534)
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                KpiCard(
                                    title = "ABA Pay",
                                    value = Formatters.formatRiel(annualAbaRevenue),
                                    emoji = "📲",
                                    backgroundColor = Color(0xFFF0F9FF),
                                    contentColor = Color(0xFF0284C7),
                                    titleColor = Color(0xFF075985)
                                )
                            }
                        }
                    }
                }

                // 12-Month Breakdown Card
                item {
                    AnnualMonthlyBreakdownCard(monthlyBreakdown = annualMonthlyBreakdown)
                }

                // Annual Top Products Card
                item {
                    ProductBreakdownCard(
                        title = "ទំនិញលក់ដាច់ប្រចាំឆ្នាំ $selectedAnnualYear (Annual Top Sellers)",
                        productSummaries = annualProductSummaries
                    )
                }
            }
        }

        // Shared Export, Telegram & Google Drive Sync Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "☁️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ចែករំលែក & Google Drive Sync",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        if (cloudConfig.lastSyncTimestamp > 0L) {
                            Text(
                                text = "Sync: ${Formatters.formatTimestampToTime(cloudConfig.lastSyncTimestamp)}",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Text(
                        text = "ចែករំលែករបាយការណ៍ ${currentPeriod.titleKh} ទៅ Telegram ឬធ្វើសមកាលកម្មទិន្នន័យការលក់ជាមួយ Google Drive Database។",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Google Drive 2-Way Sync Button
                        Button(
                            onClick = { viewModel.syncWithGoogleDrive(silent = false) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("sync_google_drive_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F172A),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (syncState == SyncState.SYNCING) Icons.Default.CloudSync else Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (syncState == SyncState.SYNCING) "Syncing..." else "Sync Cloud",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        // Telegram Share Button (Dispatches currently active period report!)
                        Button(
                            onClick = { viewModel.triggerTelegramShare(context) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("share_telegram_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF229ED9),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Telegram",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Direct Telegram Bot Send if configured
                    if (cloudConfig.telegramBotToken.isNotBlank() && cloudConfig.telegramChatId.isNotBlank()) {
                        OutlinedButton(
                            onClick = { viewModel.sendTelegramBotReportDirect() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("direct_bot_send_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0284C7)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ផ្ញើរបាយការណ៍ ${currentPeriod.titleKh} ទៅ Telegram Bot",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Ending Day (Daily Closure)
    if (showCloseDayDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDayDialog = false },
            title = {
                Text(
                    text = "បញ្ចប់ការលក់ថ្ងៃនេះ?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "តើអ្នកពិតជាចង់បិទបញ្ជីលក់សម្រាប់ថ្ងៃនេះ (${Formatters.formatDateToKhmer(selectedDate)}) មែនទេ?\n\nចំណូលសរុប: ${Formatters.formatRiel(dailyTotalRevenue)}\nចំនួនទំនិញ: $dailyTotalItems មុខ\n\nទិន្នន័យទាំងអស់ត្រូវបានរក្សាទុកជាប់ថេរ។",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeCurrentDay()
                        showCloseDayDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F172A),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_end_day_button")
                ) {
                    Text("យល់ព្រមបិទបញ្ជី")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDayDialog = false }) {
                    Text("ថយក្រោយ")
                }
            }
        )
    }
}

@Composable
fun ProductBreakdownCard(
    title: String,
    productSummaries: List<ProductSaleSummary>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Summarize,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Text(
                    text = "${productSummaries.size} មុខ",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (productSummaries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "មិនទាន់មានការលក់ក្នុងចន្លោះពេលនេះនៅឡើយទេ",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ឈ្មោះផលិតផល",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = "ចំនួន",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = "សរុបរង",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                productSummaries.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1.8f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.product.iconEmoji,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.product.nameKh,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${item.totalQuantity} កែវ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0284C7),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(0.8f)
                        )

                        Text(
                            text = Formatters.formatRiel(item.totalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF059669),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1.4f)
                        )
                    }

                    if (index < productSummaries.size - 1) {
                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyBreakdownCard(
    dailyBreakdown: List<DaySalesStat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ចំណូលតាមថ្ងៃក្នុងសប្ដាហ៍ (Daily Breakdown)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val maxRevenue = dailyBreakdown.maxOfOrNull { it.revenue }?.coerceAtLeast(1L) ?: 1L

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                dailyBreakdown.forEach { dayStat ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (dayStat.isToday) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (dayStat.isToday) Color(0xFF93C5FD) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dayStat.dayNameKhmer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (dayStat.isToday) Color(0xFF1D4ED8) else Color(0xFF1E293B)
                                    )
                                    if (dayStat.isToday) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF2563EB)
                                        ) {
                                            Text(
                                                text = "ថ្ងៃនេះ",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = Formatters.formatRiel(dayStat.revenue),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (dayStat.revenue > 0L) Color(0xFF059669) else Color(0xFF94A3B8)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Visual progress bar of revenue
                            val progress = (dayStat.revenue.toFloat() / maxRevenue.toFloat()).coerceIn(0.04f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (dayStat.revenue > 0L) progress else 0f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (dayStat.isToday) Color(0xFF2563EB) else Color(0xFF10B981))
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${dayStat.dateIso} (${dayStat.transactionsCount} លើក)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "លក់បាន ${dayStat.itemsCount} កែវ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyDayBreakdownCard(
    dailyBreakdown: List<DaySalesStat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ចំណូលតាមថ្ងៃក្នុងខែ (Daily Sales in Month)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (dailyBreakdown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "មិនទាន់មានការលក់ក្នុងខែនេះនៅឡើយទេ",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val maxRevenue = dailyBreakdown.maxOfOrNull { it.revenue }?.coerceAtLeast(1L) ?: 1L

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dailyBreakdown.forEach { dayStat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (dayStat.isToday) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (dayStat.isToday) Color(0xFF93C5FD) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dayStat.dateIso,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (dayStat.isToday) Color(0xFF1D4ED8) else Color(0xFF1E293B)
                                        )
                                        if (dayStat.isToday) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF2563EB)
                                            ) {
                                                Text(
                                                    text = "ថ្ងៃនេះ",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = Formatters.formatRiel(dayStat.revenue),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = if (dayStat.revenue > 0L) Color(0xFF059669) else Color(0xFF94A3B8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val progress = (dayStat.revenue.toFloat() / maxRevenue.toFloat()).coerceIn(0.04f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (dayStat.revenue > 0L) progress else 0f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (dayStat.isToday) Color(0xFF2563EB) else Color(0xFF10B981))
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ចំនួន ${dayStat.transactionsCount} លើក",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text(
                                        text = "លក់បាន ${dayStat.itemsCount} កែវ",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnualMonthlyBreakdownCard(
    monthlyBreakdown: List<MonthSalesStat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ចំណូលតាមខែនីមួយៗ (12-Month Breakdown)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                monthlyBreakdown.forEach { monthStat ->
                    val hasSales = monthStat.revenue > 0L
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (hasSales) Color(0xFFF8FAFC) else Color(0xFFFAFAFA),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasSales) Color(0xFFCBD5E1) else Color(0xFFF1F5F9)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = monthStat.monthNameKhmer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (hasSales) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                                Text(
                                    text = if (hasSales) "${monthStat.itemsCount} កែវ (${monthStat.transactionsCount} លើក)" else "គ្មានការលក់",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatRiel(monthStat.revenue),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (hasSales) Color(0xFF059669) else Color(0xFF94A3B8)
                                )
                                if (hasSales) {
                                    Text(
                                        text = Formatters.formatUsd(monthStat.revenue),
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    emoji: String? = null,
    backgroundColor: Color,
    contentColor: Color,
    titleColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                if (emoji != null) {
                    Text(text = emoji, fontSize = 18.sp)
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "≈ $subtitle",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}
