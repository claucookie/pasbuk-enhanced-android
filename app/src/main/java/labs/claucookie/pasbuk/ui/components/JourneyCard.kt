package labs.claucookie.pasbuk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import labs.claucookie.pasbuk.domain.model.Journey
import java.io.File
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Card component displaying journey summary information with visual thumbnail.
 *
 * Shows journey name, date range, pass count with icons, and thumbnail image.
 */
@Composable
fun JourneyCard(
    journey: Journey,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .testTag("journey_card")
            .semantics { role = Role.Button }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3646)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Blue accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF4A9EFF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Thumbnail image
            JourneyThumbnail(
                journey = journey,
                modifier = Modifier.size(136.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Journey details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Journey name
                Text(
                    text = journey.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Date range with icon
                journey.dateRange?.let { range ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF4A9EFF),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = formatDateRange(range),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFB0B8C3)
                        )
                    }
                }

                // Pass count with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = Color(0xFF4A9EFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (journey.passCount == 1) "1 Pass" else "${journey.passCount} Passes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFB0B8C3)
                    )
                }
            }

            // Right arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View journey",
                tint = Color(0xFF7B8794),
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp)
            )
        }
    }
}

@Composable
private fun JourneyThumbnail(
    journey: Journey,
    modifier: Modifier = Modifier
) {
    // Try to use the first pass's strip or background image
    val thumbnailPath = journey.passes.firstOrNull()?.let { pass ->
        pass.stripImagePath?.takeIf { File(it).exists() }
            ?: pass.backgroundImagePath?.takeIf { File(it).exists() }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        getJourneyGradientColor(journey.name, 0),
                        getJourneyGradientColor(journey.name, 1)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailPath != null) {
            AsyncImage(
                model = File(thumbnailPath),
                contentDescription = journey.name,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder with journey initial
            Text(
                text = journey.name.take(1).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

private fun formatDateRange(range: ClosedRange<java.time.Instant>): String {
    val startDate = range.start.atZone(ZoneId.systemDefault())
    val endDate = range.endInclusive.atZone(ZoneId.systemDefault())

    val startMonth = startDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val endMonth = endDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    return if (startDate.toLocalDate() == endDate.toLocalDate()) {
        "$startMonth ${startDate.dayOfMonth}"
    } else if (startDate.month == endDate.month) {
        "$startMonth ${startDate.dayOfMonth} - ${endDate.dayOfMonth}"
    } else {
        "$startMonth ${startDate.dayOfMonth} - $endMonth ${endDate.dayOfMonth}"
    }
}

private fun getJourneyGradientColor(name: String, index: Int): Color {
    val hash = name.hashCode()
    val colorSets = listOf(
        listOf(Color(0xFF667EEA), Color(0xFF764BA2)), // Purple-blue
        listOf(Color(0xFFF093FB), Color(0xFFF5576C)), // Pink-red
        listOf(Color(0xFF4FACFE), Color(0xFF00F2FE)), // Blue-cyan
        listOf(Color(0xFF43E97B), Color(0xFF38F9D7)), // Green-turquoise
        listOf(Color(0xFFFA709A), Color(0xFFFEE140)), // Pink-yellow
        listOf(Color(0xFF30CFD0), Color(0xFF330867)), // Cyan-purple
        listOf(Color(0xFFA8EDEA), Color(0xFFFED6E3)), // Mint-pink
        listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF))  // Coral-pink
    )

    val colorSet = colorSets[hash.mod(colorSets.size)]
    return colorSet[index]
}
