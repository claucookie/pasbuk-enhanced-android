package labs.claucookie.pasbuk.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import labs.claucookie.pasbuk.domain.model.ActivitySuggestion
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Timeline view for journey passes grouped by day with AI suggestions
 */
@Composable
fun JourneyTimeline(
    passes: List<Pass>,
    suggestions: List<ActivitySuggestion> = emptyList(),
    onPassClick: (String) -> Unit,
    onSuggestionDismiss: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val passesByDay = passes.groupByDay()

    Column(modifier = modifier) {
        passesByDay.forEachIndexed { dayIndex, (dayLabel, dayPasses) ->
            // Day header
            DayHeader(
                dayLabel = dayLabel,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = if (dayIndex == 0) 8.dp else 24.dp, bottom = 16.dp)
            )

            // Passes and suggestions for this day
            dayPasses.forEachIndexed { passIndex, pass ->
                // Calculate global pass index (across all days)
                val globalPassIndex = passesByDay
                    .take(dayIndex)
                    .sumOf { it.second.size } + passIndex

                // Render suggestion BEFORE this pass (if any)
                val suggestionBefore = suggestions.find { it.positionIndex == globalPassIndex + 1 }
                if (suggestionBefore != null) {
                    TimelineSuggestionItem(
                        suggestion = suggestionBefore,
                        isFirst = false,
                        isLast = false,
                        onDismiss = onSuggestionDismiss
                    )
                }

                // Render pass
                TimelinePassItem(
                    pass = pass,
                    isFirst = dayIndex == 0 && passIndex == 0,
                    isLast = dayIndex == passesByDay.lastIndex && passIndex == dayPasses.lastIndex,
                    onClick = { onPassClick(pass.id) },
                    modifier = Modifier.padding(bottom = if (passIndex < dayPasses.lastIndex) 0.dp else 0.dp)
                )
            }

            // Render suggestion AFTER last pass in this day (if this is the last day)
            if (dayIndex == passesByDay.lastIndex) {
                val globalEndIndex = passesByDay.sumOf { it.second.size }
                val suggestionAfter = suggestions.find { it.positionIndex == globalEndIndex + 1 }
                if (suggestionAfter != null) {
                    TimelineSuggestionItem(
                        suggestion = suggestionAfter,
                        isFirst = false,
                        isLast = true,
                        onDismiss = onSuggestionDismiss
                    )
                }
            }
        }

        // End of journey marker
        if (passesByDay.isNotEmpty()) {
            EndOfJourneyMarker(
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }
    }
}

/**
 * Day header showing day number and date
 */
@Composable
private fun DayHeader(
    dayLabel: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = dayLabel,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Single timeline item with icon, connector, and pass card
 */
@Composable
private fun TimelinePassItem(
    pass: Pass,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline connector column (icon + line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(72.dp)
        ) {
            // Top connector line
            if (!isFirst) {
                TimelineConnector(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Icon
            TimelineIcon(
                passType = pass.passType,
                accentColor = getAccentColorForPass(pass)
            )

            // Bottom connector line
            if (!isLast) {
                TimelineConnector(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                )
            }
        }

        // Pass card
        TimelinePassCard(
            pass = pass,
            accentColor = getAccentColorForPass(pass),
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp, bottom = 16.dp)
        )
    }
}

/**
 * Vertical line connector
 */
@Composable
private fun TimelineConnector(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Color(0xFF3D4756),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

/**
 * Circular icon for pass type
 */
@Composable
private fun TimelineIcon(
    passType: PassType,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = Color(0xFF2C3646),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getIconForPassType(passType),
            contentDescription = null,
            tint = accentColor.copy(alpha = 0.9f),
            modifier = Modifier.size(28.dp)
        )
    }
}

/**
 * Pass card with timeline styling
 */
@Composable
private fun TimelinePassCard(
    pass: Pass,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3646)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            // Accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor)
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Time
                pass.relevantDate?.let { date ->
                    val time = formatTime(date)
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFB0B8C3),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Title
                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = pass.organizationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B8C3)
                )

                // Additional info for boarding passes
                if (pass.passType == PassType.BOARDING_PASS) {
                    val fields = pass.fields
                    if (fields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fields.values.take(2).forEach { field ->
                                Text(
                                    text = field.label + " " + field.value,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7B8794),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * End of journey marker
 */
@Composable
private fun EndOfJourneyMarker(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "End of Journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

/**
 * Get icon for pass type
 */
private fun getIconForPassType(passType: PassType): ImageVector {
    return when (passType) {
        PassType.BOARDING_PASS -> Icons.Default.Flight
        PassType.EVENT_TICKET -> Icons.Default.ConfirmationNumber
        PassType.COUPON -> Icons.Default.ShoppingBag
        PassType.STORE_CARD -> Icons.Default.ShoppingBag
        PassType.GENERIC -> Icons.Default.ConfirmationNumber
    }
}

/**
 * Get accent color for pass based on type
 */
private fun getAccentColorForPass(pass: Pass): Color {
    // Try to parse backgroundColor from pass
    pass.backgroundColor?.let { bgColor ->
        try {
            val color = android.graphics.Color.parseColor(bgColor)
            return Color(color)
        } catch (e: Exception) {
            // Fall through to default color
        }
    }

    // Default colors based on pass type
    return when (pass.passType) {
        PassType.BOARDING_PASS -> Color(0xFFE74856) // Red
        PassType.EVENT_TICKET -> Color(0xFF4A9EFF) // Blue
        PassType.COUPON -> Color(0xFF9B7653) // Brown/Beige
        PassType.STORE_CARD -> Color(0xFF50C878) // Green
        PassType.GENERIC -> Color(0xFF9B7653) // Beige
    }
}

/**
 * Format instant as time string
 */
private fun formatTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant).uppercase()
}

/**
 * Group passes by day
 */
private fun List<Pass>.groupByDay(): List<Pair<String, List<Pass>>> {
    if (isEmpty()) return emptyList()

    val grouped = mutableListOf<Pair<String, List<Pass>>>()
    var currentDay: Int? = null
    var currentDayPasses = mutableListOf<Pass>()
    var dayNumber = 1

    // Get the first date as reference
    val firstDate = firstOrNull()?.relevantDate
        ?.atZone(ZoneId.systemDefault())
        ?.toLocalDate()

    forEach { pass ->
        val passDate = pass.relevantDate
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()

        val dayOfYear = passDate?.dayOfYear

        if (currentDay == null) {
            currentDay = dayOfYear
        }

        if (dayOfYear != currentDay) {
            // Save previous day
            if (currentDayPasses.isNotEmpty()) {
                val dayLabel = formatDayLabel(dayNumber, currentDayPasses.first())
                grouped.add(dayLabel to currentDayPasses.toList())
                dayNumber++
            }

            // Start new day
            currentDay = dayOfYear
            currentDayPasses = mutableListOf(pass)
        } else {
            currentDayPasses.add(pass)
        }
    }

    // Add last day
    if (currentDayPasses.isNotEmpty()) {
        val dayLabel = formatDayLabel(dayNumber, currentDayPasses.first())
        grouped.add(dayLabel to currentDayPasses.toList())
    }

    return grouped
}

/**
 * Format day label (e.g., "DAY 1 • OCT 12")
 */
private fun formatDayLabel(dayNumber: Int, firstPass: Pass): String {
    firstPass.relevantDate?.let { date ->
        val zonedDate = date.atZone(ZoneId.systemDefault())
        val month = zonedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
        val day = zonedDate.dayOfMonth
        return "DAY $dayNumber • $month $day"
    }
    return "DAY $dayNumber"
}
