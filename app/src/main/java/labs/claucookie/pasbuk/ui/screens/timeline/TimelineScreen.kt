package labs.claucookie.pasbuk.ui.screens.timeline

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.ui.components.PassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onNavigateToPassDetail: (String) -> Unit,
    onNavigateToJourneys: () -> Unit,
    onNavigateToJourneyDetail: (Long) -> Unit = {},
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val pagedPasses = viewModel.pagedPasses.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importPass(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TimelineEvent.NavigateToPassDetail -> {
                    viewModel.clearImportState()
                    onNavigateToPassDetail(event.passId)
                }
                is TimelineEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is TimelineEvent.NavigateToJourneyList -> {
                    onNavigateToJourneys()
                }
                is TimelineEvent.NavigateToJourneyDetail -> {
                    onNavigateToJourneyDetail(event.journeyId)
                }
            }
        }
    }

    var showJourneyDialog by remember { mutableStateOf(false) }

    if (showJourneyDialog) {
        labs.claucookie.pasbuk.ui.components.JourneyNameDialog(
            onDismiss = { showJourneyDialog = false },
            onConfirm = { journeyName ->
                showJourneyDialog = false
                viewModel.createJourney(journeyName)
            }
        )
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TimelineTopBar(
                uiState = uiState,
                scrollBehavior = scrollBehavior,
                onClearSelection = viewModel::clearSelection,
                onNavigateToJourneys = onNavigateToJourneys,
                onCreateJourney = { showJourneyDialog = true }
            )
        },
        floatingActionButton = {
            ImportFab(
                isImporting = importState is ImportState.Importing,
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/vnd.apple.pkpass", "*/*"))
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TimelineUiState.Loading -> {
                    // Show loading only on initial load
                    if (pagedPasses.itemCount == 0 && pagedPasses.loadState.refresh is LoadState.Loading) {
                        LoadingContent()
                    } else {
                        // Use paged content during refresh
                        PagedTimelineContent(
                            pagedPasses = pagedPasses,
                            selectedPassIds = emptySet(),
                            isSelectionMode = false,
                            onPassClick = viewModel::onPassClick,
                            onPassLongClick = viewModel::onPassLongClick
                        )
                    }
                }
                is TimelineUiState.Success -> {
                    if (state.isEmpty && pagedPasses.itemCount == 0) {
                        EmptyContent()
                    } else {
                        PagedTimelineContent(
                            pagedPasses = pagedPasses,
                            selectedPassIds = state.selectedPassIds,
                            isSelectionMode = state.isSelectionMode,
                            onPassClick = viewModel::onPassClick,
                            onPassLongClick = viewModel::onPassLongClick
                        )
                    }
                }
                is TimelineUiState.Error -> {
                    ErrorContent(message = state.message)
                }
            }

            // Import loading overlay
            AnimatedVisibility(
                visible = importState is ImportState.Importing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ImportLoadingOverlay(importState = importState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineTopBar(
    uiState: TimelineUiState,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onClearSelection: () -> Unit,
    onNavigateToJourneys: () -> Unit,
    onCreateJourney: () -> Unit
) {
    val isSelectionMode = uiState is TimelineUiState.Success && uiState.isSelectionMode
    val selectedCount = if (uiState is TimelineUiState.Success) uiState.selectedCount else 0

    TopAppBar(
        title = {
            if (isSelectionMode) {
                Text("$selectedCount selected")
            } else {
                Text("Pasbuk")
            }
        },
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear selection"
                    )
                }
            }
        },
        actions = {
            if (isSelectionMode) {
                IconButton(onClick = onCreateJourney) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Create journey"
                    )
                }
            } else {
                IconButton(onClick = onNavigateToJourneys) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Journeys"
                    )
                }
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
private fun ImportFab(
    isImporting: Boolean,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Add, contentDescription = "Import pass")
            }
        },
        text = { Text(if (isImporting) "Importing..." else "Import Pass") },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
private fun EmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "Empty state: No passes",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No passes yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the Import button to add your first pass",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineContent(
    passes: List<Pass>,
    selectedPassIds: Set<String>,
    isSelectionMode: Boolean,
    onPassClick: (String) -> Unit,
    onPassLongClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 88.dp // Extra space for FAB
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = passes,
            key = { it.id }
        ) { pass ->
            PassCard(
                pass = pass,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onPassClick(pass.id) },
                        onLongClick = { onPassLongClick(pass.id) }
                    ),
                isSelected = selectedPassIds.contains(pass.id)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagedTimelineContent(
    pagedPasses: LazyPagingItems<Pass>,
    selectedPassIds: Set<String>,
    isSelectionMode: Boolean,
    onPassClick: (String) -> Unit,
    onPassLongClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 88.dp // Extra space for FAB
        )
    ) {
        items(
            count = pagedPasses.itemCount,
            key = { index -> pagedPasses[index]?.id ?: index }
        ) { index ->
            val pass = pagedPasses[index]
            val previousPass = if (index > 0) pagedPasses[index - 1] else null

            if (pass != null) {
                // Show day header when date changes
                val showDayHeader = shouldShowDayHeader(pass, previousPass)

                if (showDayHeader) {
                    pass.relevantDate?.let { date ->
                        TimelineDayHeader(
                            date = date,
                            modifier = Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = if (index == 0) 8.dp else 24.dp,
                                bottom = 16.dp
                            )
                        )
                    }
                }

                TimelinePassItem(
                    pass = pass,
                    isFirst = index == 0 && !showDayHeader,
                    isLast = index == pagedPasses.itemCount - 1,
                    isSelected = selectedPassIds.contains(pass.id),
                    onClick = { onPassClick(pass.id) },
                    onLongClick = { onPassLongClick(pass.id) },
                    modifier = Modifier.padding(bottom = if (index < pagedPasses.itemCount - 1) 0.dp else 0.dp)
                )
            }
        }

        // Loading state for pagination
        when (pagedPasses.loadState.append) {
            is LoadState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
            is LoadState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error loading more passes",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

// Timeline helper functions and components

private fun shouldShowDayHeader(pass: Pass, previousPass: Pass?): Boolean {
    if (previousPass == null) return pass.relevantDate != null

    val passDate = pass.relevantDate?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
    val prevDate = previousPass.relevantDate?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()

    return passDate != null && passDate != prevDate
}

@Composable
private fun TimelineDayHeader(
    date: java.time.Instant,
    modifier: Modifier = Modifier
) {
    val zonedDate = date.atZone(java.time.ZoneId.systemDefault())
    val month = zonedDate.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).uppercase()
    val day = zonedDate.dayOfMonth
    val dayLabel = "$month $day"

    Text(
        text = dayLabel,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelinePassItem(
    pass: Pass,
    isFirst: Boolean,
    isLast: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
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
            isSelected = isSelected,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun TimelineConnector(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        drawLine(
            color = androidx.compose.ui.graphics.Color(0xFF3D4756),
            start = androidx.compose.ui.geometry.Offset(size.width / 2, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width / 2, size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun TimelineIcon(
    passType: PassType,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = androidx.compose.ui.graphics.Color(0xFF2C3646),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelinePassCard(
    pass: Pass,
    accentColor: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFF2C3646)
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
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
                        color = androidx.compose.ui.graphics.Color(0xFFB0B8C3),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Title
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pass.description,
                        style = MaterialTheme.typography.titleLarge,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )

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

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle
                Text(
                    text = pass.organizationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color(0xFFB0B8C3)
                )

                // Additional info for boarding passes
                if (pass.passType == PassType.BOARDING_PASS) {
                    val fields = pass.fields
                    if (fields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            fields.values.take(2).forEach { field ->
                                Text(
                                    text = field.label + " " + field.value,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = androidx.compose.ui.graphics.Color(0xFF7B8794),
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

private fun getIconForPassType(passType: PassType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (passType) {
        PassType.BOARDING_PASS -> Icons.Default.Flight
        PassType.EVENT_TICKET -> Icons.Default.ConfirmationNumber
        PassType.COUPON -> Icons.Default.ShoppingBag
        PassType.STORE_CARD -> Icons.Default.ShoppingBag
        PassType.GENERIC -> Icons.Default.ConfirmationNumber
    }
}

private fun getAccentColorForPass(pass: Pass): androidx.compose.ui.graphics.Color {
    // Try to parse backgroundColor from pass
    pass.backgroundColor?.let { bgColor ->
        try {
            val color = android.graphics.Color.parseColor(bgColor)
            return androidx.compose.ui.graphics.Color(color)
        } catch (e: Exception) {
            // Fall through to default color
        }
    }

    // Default colors based on pass type
    return when (pass.passType) {
        PassType.BOARDING_PASS -> androidx.compose.ui.graphics.Color(0xFFE74856) // Red
        PassType.EVENT_TICKET -> androidx.compose.ui.graphics.Color(0xFF4A9EFF) // Blue
        PassType.COUPON -> androidx.compose.ui.graphics.Color(0xFF9B7653) // Brown/Beige
        PassType.STORE_CARD -> androidx.compose.ui.graphics.Color(0xFF50C878) // Green
        PassType.GENERIC -> androidx.compose.ui.graphics.Color(0xFF9B7653) // Beige
    }
}

private fun formatTime(instant: java.time.Instant): String {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
        .withZone(java.time.ZoneId.systemDefault())
    return formatter.format(instant).uppercase()
}

@Composable
private fun ImportLoadingOverlay(importState: ImportState) {
    val message = when (importState) {
        is ImportState.Importing -> {
            if (importState.attempt > 1) {
                "Retrying import (${importState.attempt}/${importState.maxAttempts})..."
            } else {
                "Importing pass..."
            }
        }
        else -> "Importing pass..."
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
