# Repository Interface Contracts

**Feature**: 001-passbook-journeys
**Layer**: Domain ↔ Data
**Purpose**: Define contracts between domain layer (use cases) and data layer (repositories)

## Overview

These interfaces define the contract between the domain and data layers following the Repository pattern. Use cases depend on these interfaces, while repository implementations in the data layer provide concrete implementations.

---

## PassRepository

Manages Pass entities - importing, retrieving, updating, and deleting passes.

```kotlin
package labs.claucookie.pasbuk.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Pass

interface PassRepository {

    /**
     * Import a .pkpass file from the given URI.
     *
     * Parses the file, extracts images, and stores everything locally.
     *
     * @param uri URI pointing to the .pkpass file (from file picker)
     * @return Result containing the imported Pass or an error
     * @throws InvalidPassException if file is corrupted or invalid format
     * @throws DuplicatePassException if pass with same serialNumber already exists
     */
    suspend fun importPass(uri: Uri): Result<Pass>

    /**
     * Get a single pass by its ID.
     *
     * @param passId Unique identifier of the pass
     * @return Pass if found, null otherwise
     */
    suspend fun getPassById(passId: String): Pass?

    /**
     * Get all passes sorted chronologically by relevantDate.
     *
     * Returns a Flow that emits whenever the pass list changes.
     * Passes without relevantDate are sorted to the end.
     *
     * @return Flow of list of passes sorted by date (descending - newest first)
     */
    fun getAllPassesSortedByDate(): Flow<List<Pass>>

    /**
     * Get passes filtered by date range.
     *
     * @param startDate Start of date range (epoch millis)
     * @param endDate End of date range (epoch millis)
     * @return Flow of filtered passes
     */
    fun getPassesInDateRange(startDate: Long, endDate: Long): Flow<List<Pass>>

    /**
     * Search passes by text query.
     *
     * Searches in: organizationName, description, logoText
     *
     * @param query Search text
     * @return Flow of matching passes
     */
    fun searchPasses(query: String): Flow<List<Pass>>

    /**
     * Delete a pass and its associated files.
     *
     * This will:
     * - Remove pass from database
     * - Delete original .pkpass file
     * - Delete extracted image files
     * - Remove from any journeys (cascade handled by database)
     *
     * @param passId ID of pass to delete
     * @return Result indicating success or failure
     */
    suspend fun deletePass(passId: String): Result<Unit>

    /**
     * Get multiple passes by their IDs.
     *
     * @param passIds List of pass IDs to retrieve
     * @return List of found passes (may be fewer than requested if some don't exist)
     */
    suspend fun getPassesByIds(passIds: List<String>): List<Pass>
}
```

---

## JourneyRepository

Manages Journey entities - creating, updating, and deleting journey collections.

```kotlin
package labs.claucookie.pasbuk.domain.repository

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Journey

interface JourneyRepository {

    /**
     * Create a new journey with the given name and passes.
     *
     * @param name User-provided journey name
     * @param passIds List of pass IDs to include in journey
     * @return Result containing the created Journey or an error
     * @throws DuplicateJourneyNameException if journey with same name exists
     */
    suspend fun createJourney(name: String, passIds: List<String>): Result<Journey>

    /**
     * Get a single journey by its ID, including all associated passes.
     *
     * Passes are sorted chronologically by relevantDate.
     *
     * @param journeyId Unique identifier of the journey
     * @return Journey with passes if found, null otherwise
     */
    suspend fun getJourneyById(journeyId: Long): Journey?

    /**
     * Get all journeys.
     *
     * Returns a Flow that emits whenever journeys are created/updated/deleted.
     * Sorted by most recently created first.
     *
     * @return Flow of list of journeys
     */
    fun getAllJourneys(): Flow<List<Journey>>

    /**
     * Update a journey's name.
     *
     * @param journeyId ID of journey to update
     * @param newName New journey name
     * @return Result indicating success or failure
     * @throws DuplicateJourneyNameException if another journey has this name
     */
    suspend fun updateJourneyName(journeyId: Long, newName: String): Result<Unit>

    /**
     * Add passes to an existing journey.
     *
     * @param journeyId ID of journey to update
     * @param passIds List of pass IDs to add
     * @return Result indicating success or failure
     */
    suspend fun addPassesToJourney(journeyId: Long, passIds: List<String>): Result<Unit>

    /**
     * Remove passes from a journey.
     *
     * Note: This does NOT delete the passes themselves, only removes them from the journey.
     *
     * @param journeyId ID of journey to update
     * @param passIds List of pass IDs to remove
     * @return Result indicating success or failure
     */
    suspend fun removePassesFromJourney(journeyId: Long, passIds: List<String>): Result<Unit>

    /**
     * Delete a journey.
     *
     * Note: This does NOT delete the passes themselves, only the journey grouping.
     *
     * @param journeyId ID of journey to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteJourney(journeyId: Long): Result<Unit>

    /**
     * Get all journeys that contain a specific pass.
     *
     * @param passId ID of the pass
     * @return Flow of journeys containing this pass
     */
    fun getJourneysContainingPass(passId: String): Flow<List<Journey>>
}
```

---

## Exception Types

Custom exceptions for domain-specific errors.

```kotlin
package labs.claucookie.pasbuk.domain.repository

/**
 * Thrown when attempting to import an invalid .pkpass file.
 */
class InvalidPassException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Thrown when attempting to import a pass that already exists (by serialNumber).
 */
class DuplicatePassException(val serialNumber: String) : Exception("Pass with serialNumber $serialNumber already exists")

/**
 * Thrown when attempting to create a journey with a name that already exists.
 */
class DuplicateJourneyNameException(val name: String) : Exception("Journey with name '$name' already exists")
```

---

## Contract Guarantees

### Thread Safety
- All repository methods are `suspend` functions or return `Flow`
- Safe to call from any coroutine dispatcher
- Repository implementations handle switching to appropriate dispatcher (IO for database/file operations)

### Error Handling
- Use `Result<T>` for operations that can fail with expected errors (import, create, delete)
- Throw exceptions for unexpected errors (database corruption, I/O errors)
- Custom exceptions for domain-specific failures

### Data Consistency
- Pass deletion cascades to journey associations (enforced by Room foreign keys)
- Journey deletion does NOT delete passes (only removes associations)
- Database operations are transactional where needed

### Reactive Updates
- `Flow` emissions are triggered by database changes
- UI automatically updates when data changes
- No manual refresh needed

---

## Usage Examples

### Importing a Pass

```kotlin
class ImportPassUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    suspend operator fun invoke(uri: Uri): Result<Pass> {
        return passRepository.importPass(uri)
    }
}

// In ViewModel
viewModelScope.launch {
    importPassUseCase(selectedUri)
        .onSuccess { pass ->
            _uiState.value = UiState.Success(pass)
        }
        .onFailure { error ->
            when (error) {
                is InvalidPassException -> _uiState.value = UiState.Error("Invalid pass file")
                is DuplicatePassException -> _uiState.value = UiState.Error("Pass already imported")
                else -> _uiState.value = UiState.Error("Import failed")
            }
        }
}
```

### Observing Timeline

```kotlin
class GetTimelineUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    operator fun invoke(): Flow<List<Pass>> {
        return passRepository.getAllPassesSortedByDate()
    }
}

// In ViewModel
val timeline: StateFlow<List<Pass>> = getTimelineUseCase()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

### Creating a Journey

```kotlin
class CreateJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    suspend operator fun invoke(name: String, passIds: List<String>): Result<Journey> {
        return journeyRepository.createJourney(name, passIds)
    }
}

// In ViewModel
viewModelScope.launch {
    createJourneyUseCase(journeyName, selectedPassIds)
        .onSuccess { journey ->
            _uiState.value = UiState.JourneyCreated(journey)
        }
        .onFailure { error ->
            when (error) {
                is DuplicateJourneyNameException -> _uiState.value = UiState.Error("Journey name already exists")
                else -> _uiState.value = UiState.Error("Failed to create journey")
            }
        }
}
```

---

## Testing Contracts

### Repository Testing Strategy
- Unit test repository implementations with in-memory Room database
- Mock repositories in use case tests
- Use Test Doubles (fakes) for integration tests

### Example Mock

```kotlin
class FakePassRepository : PassRepository {
    private val passes = mutableListOf<Pass>()
    private val passesFlow = MutableStateFlow<List<Pass>>(emptyList())

    override suspend fun importPass(uri: Uri): Result<Pass> {
        val pass = createMockPass(uri) // Test helper
        passes.add(pass)
        passesFlow.value = passes.sortedByDescending { it.relevantDate }
        return Result.success(pass)
    }

    override fun getAllPassesSortedByDate(): Flow<List<Pass>> = passesFlow

    // ... other methods
}
```

---

## Design Rationale

### Why Separate Interface from Implementation?
- **Testability**: Use cases can be tested with fake repositories
- **Flexibility**: Can swap implementations (e.g., add cloud sync later)
- **Dependency Inversion**: Domain layer doesn't depend on data layer details

### Why Use Flow Instead of suspend fun?
- **Reactive**: UI automatically updates when data changes
- **Efficient**: Only active collectors receive updates
- **Lifecycle-aware**: Compose can collect safely with `collectAsStateWithLifecycle()`

### Why Result<T> Instead of Exceptions?
- **Explicit**: Caller knows operation can fail
- **Type-safe**: Compiler forces error handling
- **Kotlin-idiomatic**: Follows Kotlin's Result conventions
