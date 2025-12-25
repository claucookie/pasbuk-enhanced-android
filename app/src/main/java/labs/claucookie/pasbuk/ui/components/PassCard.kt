package labs.claucookie.pasbuk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Card composable displaying a pass summary.
 *
 * @param pass The pass to display
 * @param modifier Modifier for the composable (caller should apply clickable/combinedClickable)
 * @param isSelected Whether this card is currently selected (for multi-select mode)
 */
@Composable
fun PassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val backgroundColor = parsePassColor(pass.backgroundColor) ?: MaterialTheme.colorScheme.primaryContainer
    val foregroundColor = parsePassColor(pass.foregroundColor) ?: MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Button
                stateDescription = if (isSelected) "Selected" else "Not selected"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo or Icon
            PassLogo(
                logoPath = pass.logoImagePath ?: pass.iconImagePath,
                passType = pass.passType,
                tintColor = foregroundColor
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Pass details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Organization name
                Text(
                    text = pass.organizationName,
                    style = MaterialTheme.typography.labelMedium,
                    color = foregroundColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description (title)
                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = foregroundColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Logo text if available
                pass.logoText?.let { logoText ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = logoText,
                        style = MaterialTheme.typography.bodySmall,
                        color = foregroundColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Date if available
                pass.relevantDate?.let { date ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = foregroundColor.copy(alpha = 0.9f)
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PassLogo(
    logoPath: String?,
    passType: PassType,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tintColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        if (logoPath != null && File(logoPath).exists()) {
            AsyncImage(
                model = File(logoPath),
                contentDescription = "Pass logo",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = getPassTypeIcon(passType),
                contentDescription = passType.name,
                tint = tintColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

private fun getPassTypeIcon(passType: PassType): ImageVector {
    return when (passType) {
        PassType.BOARDING_PASS -> Icons.Default.AirplaneTicket
        PassType.EVENT_TICKET -> Icons.Default.ConfirmationNumber
        PassType.COUPON -> Icons.Default.LocalOffer
        PassType.STORE_CARD -> Icons.Default.CreditCard
        PassType.GENERIC -> Icons.Default.Receipt
    }
}

private fun parsePassColor(colorString: String?): Color? {
    if (colorString.isNullOrBlank()) return null

    return try {
        // Handle "rgb(r, g, b)" format
        if (colorString.startsWith("rgb", ignoreCase = true)) {
            val values = colorString
                .replace("rgb(", "", ignoreCase = true)
                .replace(")", "")
                .split(",")
                .map { it.trim().toInt() }

            if (values.size >= 3) {
                Color(values[0], values[1], values[2])
            } else null
        }
        // Handle "#RRGGBB" format
        else if (colorString.startsWith("#")) {
            Color(android.graphics.Color.parseColor(colorString))
        }
        else null
    } catch (e: Exception) {
        null
    }
}

private fun formatDate(instant: Instant): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

@Preview(showBackground = true)
@Composable
private fun PassCardPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PassCard(
                pass = Pass(
                    id = "1",
                    serialNumber = "ABC123",
                    passTypeIdentifier = "pass.com.example.ticket",
                    organizationName = "Concert Venue",
                    description = "Summer Music Festival 2024",
                    teamIdentifier = "TEAM123",
                    relevantDate = Instant.now(),
                    expirationDate = null,
                    locations = emptyList(),
                    logoText = "VIP Access",
                    backgroundColor = "rgb(88, 86, 214)",
                    foregroundColor = "rgb(255, 255, 255)",
                    labelColor = null,
                    barcode = null,
                    logoImagePath = null,
                    iconImagePath = null,
                    thumbnailImagePath = null,
                    stripImagePath = null,
                    backgroundImagePath = null,
                    originalPkpassPath = "/path/to/pass.pkpass",
                    passType = PassType.EVENT_TICKET,
                    fields = emptyMap(),
                    createdAt = Instant.now(),
                    modifiedAt = Instant.now()
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PassCardBoardingPassPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            PassCard(
                pass = Pass(
                    id = "2",
                    serialNumber = "FLT456",
                    passTypeIdentifier = "pass.com.airline.boarding",
                    organizationName = "Delta Airlines",
                    description = "Flight DL1234 - JFK to LAX",
                    teamIdentifier = "TEAM456",
                    relevantDate = Instant.now().plusSeconds(86400 * 3),
                    expirationDate = null,
                    locations = emptyList(),
                    logoText = null,
                    backgroundColor = "rgb(0, 49, 83)",
                    foregroundColor = "rgb(255, 255, 255)",
                    labelColor = null,
                    barcode = null,
                    logoImagePath = null,
                    iconImagePath = null,
                    thumbnailImagePath = null,
                    stripImagePath = null,
                    backgroundImagePath = null,
                    originalPkpassPath = "/path/to/pass.pkpass",
                    passType = PassType.BOARDING_PASS,
                    fields = emptyMap(),
                    createdAt = Instant.now(),
                    modifiedAt = Instant.now()
                ),
                isSelected = true
            )
        }
    }
}
