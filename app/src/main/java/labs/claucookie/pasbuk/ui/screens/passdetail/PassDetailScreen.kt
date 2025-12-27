package labs.claucookie.pasbuk.ui.screens.passdetail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassField
import labs.claucookie.pasbuk.ui.components.BarcodeDisplay
import labs.claucookie.pasbuk.ui.components.ConfirmationDialog
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PassDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PassDetailEvent.NavigateBack -> onNavigateBack()
                is PassDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is PassDetailEvent.PassDeleted -> onNavigateBack()
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Pass",
            message = "Are you sure you want to delete this pass? This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.onDeleteClick()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PassDetailTopBar(
                scrollBehavior = scrollBehavior,
                onBackClick = onNavigateBack,
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PassDetailUiState.Loading -> {
                    LoadingContent()
                }

                is PassDetailUiState.Success -> {
                    PassDetailContent(pass = state.pass)
                }

                is PassDetailUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = viewModel::retry
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassDetailTopBar(
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
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

@Composable
private fun PassDetailContent(pass: Pass) {
    val backgroundColor = parsePassColor(pass.backgroundColor) ?: MaterialTheme.colorScheme.primaryContainer
    val foregroundColor = parsePassColor(pass.foregroundColor) ?: MaterialTheme.colorScheme.onPrimaryContainer
    val labelColor = parsePassColor(pass.labelColor) ?: foregroundColor.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Pass Header Card
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
                // Header with logo and organization
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pass.logoImagePath?.let { logoPath ->
                        if (File(logoPath).exists()) {
                            AsyncImage(
                                model = File(logoPath),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                    }
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

                // Strip image if present
                pass.stripImagePath?.let { stripPath ->
                    if (File(stripPath).exists()) {
                        AsyncImage(
                            model = File(stripPath),
                            contentDescription = "Strip image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Description
                Text(
                    text = pass.description,
                    style = MaterialTheme.typography.headlineSmall,
                    color = foregroundColor,
                    fontWeight = FontWeight.Bold
                )

                // Relevant date
                pass.relevantDate?.let { date ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatDateTime(date),
                        style = MaterialTheme.typography.bodyLarge,
                        color = foregroundColor.copy(alpha = 0.9f)
                    )
                }

                // Barcode
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

        // Pass Fields
        if (pass.fields.isNotEmpty()) {
            PassFieldsSection(
                fields = pass.fields,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                valueColor = MaterialTheme.colorScheme.onSurface
            )
        }

        // Pass Info Section
        PassInfoSection(pass = pass)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PassFieldsSection(
    fields: Map<String, PassField>,
    labelColor: Color,
    valueColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            fields.values.forEachIndexed { index, field ->
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
                            color = labelColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = field.value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = valueColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PassInfoSection(pass: Pass) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pass Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Serial Number", value = pass.serialNumber)
            InfoRow(label = "Pass Type", value = pass.passType.name.replace("_", " "))
            InfoRow(label = "Imported", value = formatDateTime(pass.createdAt))

            pass.expirationDate?.let { expDate ->
                InfoRow(label = "Expires", value = formatDateTime(expDate))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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

private fun parsePassColor(colorString: String?): Color? {
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

private fun formatDateTime(instant: Instant): String {
    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withZone(ZoneId.systemDefault())
    return formatter.format(instant)
}
