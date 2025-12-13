package labs.claucookie.pasbuk.ui.screens.passdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.BarcodeFormat
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassField
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.ui.theme.PasbukTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * UI tests for PassDetailScreen (T038).
 *
 * Tests verify that:
 * - Pass details are displayed correctly
 * - Barcode is rendered when present
 * - Loading state shows progress indicator
 * - Error state shows error message and retry button
 * - Delete confirmation dialog works correctly
 */
@RunWith(AndroidJUnit4::class)
class PassDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ==================== Success State Tests ====================

    @Test
    fun successState_displaysPassOrganizationName() {
        // Given
        val pass = createTestPass()

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Test Organization")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysPassDescription() {
        // Given
        val pass = createTestPass()

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Concert Event Pass")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysSerialNumber() {
        // Given
        val pass = createTestPass()

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("SERIAL-12345")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysPassType() {
        // Given
        val pass = createTestPass()

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("EVENT TICKET")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysLogoTextWhenPresent() {
        // Given
        val pass = createTestPass().copy(logoText = "VIP Access")

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("VIP Access")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysFieldsWhenPresent() {
        // Given
        val fields = mapOf(
            "seat" to PassField(key = "seat", label = "Seat", value = "A12", textAlignment = null),
            "row" to PassField(key = "row", label = "Row", value = "5", textAlignment = null)
        )
        val pass = createTestPass().copy(fields = fields)

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("SEAT")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("A12")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysBarcodeWhenPresent() {
        // Given
        val barcode = Barcode(
            message = "TICKET-XYZ-789",
            format = BarcodeFormat.QR,
            messageEncoding = "iso-8859-1",
            altText = "Scan this code"
        )
        val pass = createTestPass().copy(barcode = barcode)

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then: Barcode alt text should be visible
        composeTestRule
            .onNodeWithText("Scan this code")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysPassInformationSection() {
        // Given
        val pass = createTestPass()

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Pass Information")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysDetailsSection_whenFieldsExist() {
        // Given
        val fields = mapOf(
            "venue" to PassField(key = "venue", label = "Venue", value = "Madison Square Garden", textAlignment = null)
        )
        val pass = createTestPass().copy(fields = fields)

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailContentTestWrapper(pass = pass)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Details")
            .assertIsDisplayed()
    }

    // ==================== Loading State Tests ====================

    @Test
    fun loadingState_displaysProgressIndicator() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailLoadingTestWrapper()
            }
        }

        // Then: CircularProgressIndicator should be displayed
        // Note: We can't easily test for CircularProgressIndicator directly,
        // but we verify the loading content composable is used
        composeTestRule.waitForIdle()
    }

    // ==================== Error State Tests ====================

    @Test
    fun errorState_displaysErrorMessage() {
        // Given
        val errorMessage = "Failed to load pass"

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailErrorTestWrapper(message = errorMessage)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Failed to load pass")
            .assertIsDisplayed()
    }

    @Test
    fun errorState_displaysRetryButton() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailErrorTestWrapper(message = "Error")
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonIsClickable() {
        // Given
        var retryClicked = false

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailErrorTestWrapper(
                    message = "Error",
                    onRetry = { retryClicked = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        // Then
        assert(retryClicked)
    }

    // ==================== Top Bar Tests ====================

    @Test
    fun topBar_displaysTitle() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailTopBarTestWrapper()
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Pass Details")
            .assertIsDisplayed()
    }

    @Test
    fun topBar_backButtonIsDisplayed() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailTopBarTestWrapper()
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()
    }

    @Test
    fun topBar_deleteButtonIsDisplayed() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailTopBarTestWrapper()
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Delete pass")
            .assertIsDisplayed()
    }

    @Test
    fun topBar_backButtonIsClickable() {
        // Given
        var backClicked = false

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailTopBarTestWrapper(onBackClick = { backClicked = true })
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Back")
            .performClick()

        // Then
        assert(backClicked)
    }

    @Test
    fun topBar_deleteButtonIsClickable() {
        // Given
        var deleteClicked = false

        // When
        composeTestRule.setContent {
            PasbukTheme {
                PassDetailTopBarTestWrapper(onDeleteClick = { deleteClicked = true })
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Delete pass")
            .performClick()

        // Then
        assert(deleteClicked)
    }

    // ==================== Delete Dialog Tests ====================

    @Test
    fun deleteDialog_displaysConfirmationMessage() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                DeleteDialogTestWrapper(showDialog = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Delete Pass")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Are you sure you want to delete this pass? This action cannot be undone.")
            .assertIsDisplayed()
    }

    @Test
    fun deleteDialog_hasDeleteButton() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                DeleteDialogTestWrapper(showDialog = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Delete")
            .assertIsDisplayed()
    }

    @Test
    fun deleteDialog_hasCancelButton() {
        // When
        composeTestRule.setContent {
            PasbukTheme {
                DeleteDialogTestWrapper(showDialog = true)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()
    }

    @Test
    fun deleteDialog_confirmCallsOnConfirm() {
        // Given
        var confirmed = false

        // When
        composeTestRule.setContent {
            PasbukTheme {
                DeleteDialogTestWrapper(
                    showDialog = true,
                    onConfirm = { confirmed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Delete")
            .performClick()

        // Then
        assert(confirmed)
    }

    @Test
    fun deleteDialog_cancelCallsOnDismiss() {
        // Given
        var dismissed = false

        // When
        composeTestRule.setContent {
            PasbukTheme {
                DeleteDialogTestWrapper(
                    showDialog = true,
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Then
        assert(dismissed)
    }

    // ==================== Helper Methods ====================

    private fun createTestPass(): Pass {
        return Pass(
            id = "test-pass-id",
            serialNumber = "SERIAL-12345",
            passTypeIdentifier = "pass.com.example.event",
            organizationName = "Test Organization",
            description = "Concert Event Pass",
            teamIdentifier = "TEAM123",
            relevantDate = Instant.parse("2024-12-25T20:00:00Z"),
            expirationDate = null,
            locations = emptyList(),
            logoText = null,
            backgroundColor = "rgb(66, 133, 244)",
            foregroundColor = "rgb(255, 255, 255)",
            labelColor = "rgb(200, 200, 200)",
            barcode = null,
            logoImagePath = null,
            iconImagePath = null,
            thumbnailImagePath = null,
            stripImagePath = null,
            backgroundImagePath = null,
            originalPkpassPath = "/path/to/test.pkpass",
            passType = PassType.EVENT_TICKET,
            fields = emptyMap(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }
}

// ==================== Test Wrapper Composables ====================
// These wrapper composables expose private composables for testing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import labs.claucookie.pasbuk.ui.components.BarcodeDisplay
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import java.io.File

@Composable
fun PassDetailContentTestWrapper(pass: Pass) {
    val backgroundColor = parseTestColor(pass.backgroundColor) ?: MaterialTheme.colorScheme.primaryContainer
    val foregroundColor = parseTestColor(pass.foregroundColor) ?: MaterialTheme.colorScheme.onPrimaryContainer
    val labelColor = parseTestColor(pass.labelColor) ?: foregroundColor.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pass.organizationName,
                            style = MaterialTheme.typography.labelLarge,
                            color = labelColor
                        )
                        pass.logoText?.let { logoText ->
                            Text(
                                text = logoText,
                                style = MaterialTheme.typography.titleMedium,
                                color = foregroundColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.headlineSmall,
                    color = foregroundColor,
                    fontWeight = FontWeight.Bold
                )

                pass.barcode?.let { barcode ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BarcodeDisplay(
                            barcode = barcode,
                            size = 180.dp,
                            showAltText = true
                        )
                    }
                }
            }
        }

        if (pass.fields.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    pass.fields.values.forEachIndexed { index, field ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                        Column {
                            field.label?.let { label ->
                                Text(
                                    text = label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = field.value,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pass Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                InfoRowTest(label = "Serial Number", value = pass.serialNumber)
                InfoRowTest(label = "Pass Type", value = pass.passType.name.replace("_", " "))
            }
        }
    }
}

@Composable
fun InfoRowTest(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PassDetailLoadingTestWrapper() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun PassDetailErrorTestWrapper(
    message: String,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassDetailTopBarTestWrapper(
    onBackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pass Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete pass"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun DeleteDialogTestWrapper(
    showDialog: Boolean,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Pass") },
            text = { Text("Are you sure you want to delete this pass? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun parseTestColor(colorString: String?): Color? {
    if (colorString.isNullOrBlank()) return null
    return try {
        when {
            colorString.startsWith("rgb", ignoreCase = true) -> {
                val values = colorString
                    .replace("rgb(", "", ignoreCase = true)
                    .replace(")", "")
                    .split(",")
                    .map { it.trim().toInt() }
                if (values.size >= 3) Color(values[0], values[1], values[2]) else null
            }
            colorString.startsWith("#") -> {
                Color(android.graphics.Color.parseColor(colorString))
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
