package labs.claucookie.pasbuk.ui.screens.timeline

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.ui.components.PassCard

@Composable
fun TimelineScreen(
    onNavigateToPassDetail: (String) -> Unit,
    onNavigateToJourneys: () -> Unit,
    onNavigateToJourneyDetail: (Long) -> Unit = {},
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TimelineTopBar(
                uiState = uiState,
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
                    LoadingContent()
                }
                is TimelineUiState.Success -> {
                    if (state.isEmpty) {
                        EmptyContent()
                    } else {
                        TimelineContent(
                            passes = state.passes,
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
                ImportLoadingOverlay()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineTopBar(
    uiState: TimelineUiState,
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                onClick = { onPassClick(pass.id) },
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

@Composable
private fun ImportLoadingOverlay() {
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
                text = "Importing pass...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
