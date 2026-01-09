package labs.claucookie.pasbuk.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import labs.claucookie.pasbuk.domain.model.ActivitySuggestion

/**
 * Timeline item for AI-generated activity suggestion.
 *
 * Displays between pass items with expandable/collapsible behavior.
 */
@Composable
fun TimelineSuggestionItem(
    suggestion: ActivitySuggestion,
    isFirst: Boolean,
    isLast: Boolean,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

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
                    isDashed = true,
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Icon
            SuggestionIcon()

            // Bottom connector line
            if (!isLast) {
                TimelineConnector(
                    isDashed = true,
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                )
            }
        }

        // Suggestion card
        SuggestionCard(
            suggestion = suggestion,
            isExpanded = isExpanded,
            onToggleExpanded = { isExpanded = !isExpanded },
            onDismiss = { onDismiss(suggestion.id) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun SuggestionIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = Color(0xFF3D4756),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = "AI Suggestion",
            tint = Color(0xFFFFC107),  // Amber/yellow for lightbulb
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun SuggestionCard(
    suggestion: ActivitySuggestion,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onToggleExpanded),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C3646).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column {
            // Accent bar (amber for suggestions)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFFFFC107))
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // AI badge
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFC107).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Expand/collapse button
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color(0xFFB0B8C3),
                        modifier = Modifier.size(20.dp)
                    )

                    // Dismiss button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss suggestion",
                            tint = Color(0xFFB0B8C3),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                // Expandable content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        // Description
                        Text(
                            text = suggestion.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB0B8C3)
                        )

                        // Reasoning (if available)
                        suggestion.reasoning?.let { reasoning ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Why: $reasoning",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7B8794),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineConnector(
    isDashed: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (isDashed) {
            // Draw dashed line for suggestions
            val dashWidth = 4.dp.toPx()
            val dashGap = 4.dp.toPx()
            var currentY = 0f

            while (currentY < size.height) {
                drawLine(
                    color = Color(0xFF3D4756),
                    start = Offset(size.width / 2, currentY),
                    end = Offset(size.width / 2, minOf(currentY + dashWidth, size.height)),
                    strokeWidth = 2.dp.toPx()
                )
                currentY += dashWidth + dashGap
            }
        } else {
            // Solid line for passes
            drawLine(
                color = Color(0xFF3D4756),
                start = Offset(size.width / 2, 0f),
                end = Offset(size.width / 2, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
