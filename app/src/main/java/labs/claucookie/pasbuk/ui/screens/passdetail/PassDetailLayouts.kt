package labs.claucookie.pasbuk.ui.screens.passdetail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassField
import labs.claucookie.pasbuk.ui.components.BarcodeDisplay
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Boarding Pass specific layout with route display and departure info
 */
@Composable
fun BoardingPassLayout(pass: Pass, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Header with airline and flight info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = pass.organizationName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF7B8794)
                )
                Text(
                    text = "FLIGHT ${pass.fields["flightNumber"]?.value ?: ""}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4A9EFF)
                )
            }
        }

        // Route display
        BoardingPassRoute(pass)

        Spacer(modifier = Modifier.height(24.dp))

        // Terminal, Gate, Seat info
        BoardingPassInfo(pass)

        Spacer(modifier = Modifier.height(16.dp))

        // Passenger info
        PassengerInfo(pass)

        Spacer(modifier = Modifier.height(24.dp))

        // Barcode
        pass.barcode?.let { barcode ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BarcodeDisplay(
                    barcode = barcode,
                    size = 200.dp,
                    showAltText = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add to Wallet button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add to Apple Wallet", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "● On Time",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BoardingPassRoute(pass: Pass) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Origin
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = pass.fields["origin"]?.value ?: "LHR",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "10:40 AM",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0B8C3)
            )
        }

        // Flight icon
        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = null,
            tint = Color(0xFF4A9EFF),
            modifier = Modifier.size(32.dp)
        )

        // Destination
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = pass.fields["destination"]?.value ?: "JFK",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "01:25 PM",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFB0B8C3)
            )
        }
    }
}

@Composable
private fun BoardingPassInfo(pass: Pass) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoBox(
            label = "TERMINAL",
            value = pass.fields["terminal"]?.value ?: "5",
            modifier = Modifier.weight(1f)
        )
        InfoBox(
            label = "GATE",
            value = pass.fields["gate"]?.value ?: "A10",
            modifier = Modifier.weight(1f),
            highlighted = true
        )
        InfoBox(
            label = "SEAT",
            value = pass.fields["seat"]?.value ?: "42A",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (highlighted) Color(0xFF4A9EFF) else Color(0xFF2C3646))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlighted) Color.White.copy(alpha = 0.8f) else Color(0xFF7B8794)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PassengerInfo(pass: Pass) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "PASSENGER",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7B8794)
            )
            Text(
                text = pass.fields["passenger"]?.value ?: "John Doe",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "GROUP",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7B8794)
            )
            Text(
                text = pass.fields["group"]?.value ?: "3",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

/**
 * Event Ticket layout with event image and venue info
 */
@Composable
fun EventTicketLayout(pass: Pass, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Event image
        pass.stripImagePath?.let { stripPath ->
            if (File(stripPath).exists()) {
                AsyncImage(
                    model = File(stripPath),
                    contentDescription = pass.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Event title
        Text(
            text = pass.description,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section, Row, Seat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoBox(
                label = "SECTION",
                value = pass.fields["section"]?.value ?: "120",
                modifier = Modifier.weight(1f)
            )
            InfoBox(
                label = "ROW",
                value = pass.fields["row"]?.value ?: "G",
                modifier = Modifier.weight(1f)
            )
            InfoBox(
                label = "SEAT",
                value = pass.fields["seat"]?.value ?: "14-15",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Date, Time, Venue
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EventInfoRow(icon = "📅", label = "DATE", value = formatEventDate(pass.relevantDate))
            EventInfoRow(icon = "🕐", label = "TIME", value = formatEventTime(pass.relevantDate))
            EventInfoRow(icon = "📍", label = "VENUE", value = pass.fields["venue"]?.value ?: pass.organizationName)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Barcode
        pass.barcode?.let { barcode ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BarcodeDisplay(
                    barcode = barcode,
                    size = 200.dp,
                    showAltText = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Get Directions button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A9EFF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Directions", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EventInfoRow(icon: String, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7B8794)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

private fun formatEventDate(instant: Instant?): String {
    if (instant == null) return ""
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

private fun formatEventTime(instant: Instant?): String {
    if (instant == null) return ""
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}

/**
 * Generic pass layout for memberships and general passes
 */
@Composable
fun GenericPassLayout(pass: Pass, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Organization badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GYM MEMBERSHIP",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF4A9EFF)
                )
                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Member info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C3646)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "MEMBER NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7B8794)
                    )
                    Text(
                        text = pass.fields["member"]?.value ?: "Jane Doe",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MEMBER ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7B8794)
                    )
                    Text(
                        text = pass.fields["memberId"]?.value ?: "123456789",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status and Expiry
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7B8794)
                )
                Text(
                    text = "● Active",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF4CAF50)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "EXPIRES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7B8794)
                )
                Text(
                    text = formatEventDate(pass.expirationDate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Barcode
        pass.barcode?.let { barcode ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                BarcodeDisplay(
                    barcode = barcode,
                    size = 200.dp,
                    showAltText = true
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add to Wallet button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A9EFF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add to Apple Wallet", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pass Details
        Text(
            text = "🔵 PASS DETAILS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        pass.fields.values.forEach { field ->
            GenericPassDetailRow(label = field.label ?: "", value = field.value)
        }
    }
}

@Composable
private fun GenericPassDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7B8794)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

/**
 * Store Card layout with balance and tier info
 */
@Composable
fun StoreCardLayout(pass: Pass, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4A9EFF)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pass.organizationName.take(1),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = pass.organizationName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Tier badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = pass.fields["tier"]?.value ?: "PLATINUM",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "REWARDS BALANCE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pass.fields["points"]?.value ?: "2,450"} pts",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Barcode Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                pass.barcode?.let { barcode ->
                    BarcodeDisplay(
                        barcode = barcode,
                        size = 160.dp,
                        showAltText = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan at checkout",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details section
        Text(
            text = "Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StoreCardDetailRow(icon = "📅", label = "Last Used", value = formatEventDate(pass.modifiedAt))
            StoreCardDetailRow(icon = "🏆", label = "Tier", value = pass.fields["tier"]?.value ?: "Platinum")
            StoreCardDetailRow(icon = "🏪", label = "Associated Store", value = pass.fields["store"]?.value ?: "${pass.organizationName} Downtown")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add to Wallet button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add to Wallet", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StoreCardDetailRow(icon: String, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7B8794)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

/**
 * Coupon layout with offer card and redemption
 */
@Composable
fun CouponLayout(pass: Pass, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Color(0xFFF5F5F5))) {
        // Offer Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF4A9EFF)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pass.organizationName.take(1),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${pass.organizationName} REWARDS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = pass.fields["offer"]?.value ?: "Your Next Order",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Expiry
                pass.expirationDate?.let { expDate ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Expires ${formatEventDate(expDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Barcode
        pass.barcode?.let { barcode ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BarcodeDisplay(
                        barcode = barcode,
                        size = 120.dp,
                        showAltText = false
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "COUPON CODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = barcode.message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Mark as Redeemed button
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4A9EFF)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("✓ Mark as Redeemed", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // How to use
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4A9EFF).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "ℹ️", style = MaterialTheme.typography.titleMedium)
                    }
                    Column {
                        Text(
                            text = "How to use",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        Text(
                            text = "Present this code at checkout",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
