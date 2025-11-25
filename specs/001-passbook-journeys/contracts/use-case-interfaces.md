# Use Case Contracts

**Feature**: 001-passbook-journeys
**Layer**: Domain
**Purpose**: Define business logic operations that ViewModels can invoke

## Overview

Use cases encapsulate single business operations and orchestrate repositories. They follow the Single Responsibility Principle - each use case does one thing. ViewModels invoke use cases rather than calling repositories directly.

---

## Pass Management Use Cases

### ImportPassUseCase

Import a .pkpass file and save it to local storage.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import android.net.Uri
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Import a .pkpass file from device storage.
 *
 * Business Rules:
 * - File must be valid .pkpass format
 * - Duplicate serial numbers are rejected
 * - All images are extracted and stored locally
 * - Original .pkpass file is preserved
 */
class ImportPassUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * @param uri URI from file picker pointing to .pkpass file
     * @return Result<Pass> with imported pass or error
     */
    suspend operator fun invoke(uri: Uri): Result<Pass> {
        return passRepository.importPass(uri)
    }
}
```

### GetPassDetailUseCase

Retrieve detailed information for a single pass.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Get full details of a specific pass by ID.
 *
 * Business Rules:
 * - Returns null if pass not found
 * - Includes all metadata, images, and barcode data
 */
class GetPassDetailUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * @param passId Unique identifier of the pass
     * @return Pass if found, null otherwise
     */
    suspend operator fun invoke(passId: String): Pass? {
        return passRepository.getPassById(passId)
    }
}
```

### DeletePassUseCase

Delete a pass and all its associated data.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Delete a pass permanently.
 *
 * Business Rules:
 * - Deletes pass from database
 * - Deletes original .pkpass file
 * - Deletes all extracted images
 * - Removes pass from any journeys (cascade)
 * - Cannot be undone
 */
class DeletePassUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * @param passId ID of pass to delete
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(passId: String): Result<Unit> {
        return passRepository.deletePass(passId)
    }
}
```

---

## Timeline Use Cases

### GetTimelineUseCase

Retrieve all passes sorted chronologically for timeline display.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Get all passes sorted by date for timeline view.
 *
 * Business Rules:
 * - Sorted by relevantDate descending (newest first)
 * - Passes without dates appear at end
 * - Reactive - updates automatically when passes change
 * - Empty list if no passes imported
 */
class GetTimelineUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * @return Flow<List<Pass>> emitting sorted pass list
     */
    operator fun invoke(): Flow<List<Pass>> {
        return passRepository.getAllPassesSortedByDate()
    }
}
```

### SearchPassesUseCase

Search passes by text query.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Search passes by organization name, description, or logo text.
 *
 * Business Rules:
 * - Case-insensitive search
 * - Matches partial text
 * - Searches: organizationName, description, logoText
 * - Returns empty list if no matches
 */
class SearchPassesUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * @param query Search text
     * @return Flow<List<Pass>> emitting matching passes
     */
    operator fun invoke(query: String): Flow<List<Pass>> {
        return passRepository.searchPasses(query)
    }
}
```

---

## Journey Management Use Cases

### CreateJourneyUseCase

Create a new journey from selected passes.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Create a new journey (grouped collection of passes).
 *
 * Business Rules:
 * - Journey name must be unique
 * - Name cannot be empty or blank
 * - Must contain at least one pass
 * - Passes are automatically sorted by date
 * - Name max length: 100 characters
 */
class CreateJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @param name User-provided journey name
     * @param passIds List of pass IDs to include
     * @return Result<Journey> with created journey or error
     */
    suspend operator fun invoke(name: String, passIds: List<String>): Result<Journey> {
        // Validation
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Journey name cannot be empty"))
        }
        if (trimmedName.length > 100) {
            return Result.failure(IllegalArgumentException("Journey name too long (max 100 characters)"))
        }
        if (passIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("Journey must contain at least one pass"))
        }

        return journeyRepository.createJourney(trimmedName, passIds)
    }
}
```

### GetAllJourneysUseCase

Retrieve all journeys for journey list screen.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Get all journeys.
 *
 * Business Rules:
 * - Sorted by creation date (newest first)
 * - Each journey includes its passes sorted by date
 * - Reactive - updates automatically
 * - Empty list if no journeys created
 */
class GetAllJourneysUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @return Flow<List<Journey>> emitting journey list
     */
    operator fun invoke(): Flow<List<Journey>> {
        return journeyRepository.getAllJourneys()
    }
}
```

### GetJourneyDetailUseCase

Get details of a specific journey including all passes.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Get full details of a journey.
 *
 * Business Rules:
 * - Includes all associated passes
 * - Passes sorted chronologically
 * - Returns null if journey not found
 */
class GetJourneyDetailUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @param journeyId ID of journey to retrieve
     * @return Journey if found, null otherwise
     */
    suspend operator fun invoke(journeyId: Long): Journey? {
        return journeyRepository.getJourneyById(journeyId)
    }
}
```

### UpdateJourneyUseCase

Update an existing journey (rename or modify passes).

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Update journey properties.
 *
 * Business Rules:
 * - New name must be unique
 * - Name validation same as CreateJourneyUseCase
 */
class UpdateJourneyNameUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @param journeyId ID of journey to update
     * @param newName New journey name
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(journeyId: Long, newName: String): Result<Unit> {
        val trimmedName = newName.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("Journey name cannot be empty"))
        }
        if (trimmedName.length > 100) {
            return Result.failure(IllegalArgumentException("Journey name too long (max 100 characters)"))
        }

        return journeyRepository.updateJourneyName(journeyId, trimmedName)
    }
}
```

### AddPassesToJourneyUseCase

Add passes to an existing journey.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Add passes to an existing journey.
 *
 * Business Rules:
 * - Duplicate passes ignored (no-op, not error)
 * - Passes automatically sorted by date after addition
 */
class AddPassesToJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @param journeyId ID of journey to update
     * @param passIds List of pass IDs to add
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(journeyId: Long, passIds: List<String>): Result<Unit> {
        if (passIds.isEmpty()) {
            return Result.success(Unit) // No-op
        }
        return journeyRepository.addPassesToJourney(journeyId, passIds)
    }
}
```

### RemovePassesFromJourneyUseCase

Remove passes from a journey.

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Remove passes from a journey.
 *
 * Business Rules:
 * - Does NOT delete passes themselves
 * - Only removes association with journey
 * - Non-existent passes ignored (no error)
 */
class RemovePassesFromJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @param journeyId ID of journey to update
     * @param passIds List of pass IDs to remove
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(journeyId: Long, passIds: List<String>): Result<Unit> {
        if (passIds.isEmpty()) {
            return Result.success(Unit) // No-op
        }
        return journeyRepository.removePassesFromJourney(journeyId, passIds)
    }
}
```

### DeleteJourneyUseCase

Delete a journey (but not its passes).

```kotlin
package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Delete a journey.
 *
 * Business Rules:
 * - Does NOT delete passes (only the journey grouping)
 * - Passes remain in timeline after journey deletion
 * - Cannot be undone
 */
class DeleteJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * @param journeyId ID of journey to delete
     * @return Result<Unit> indicating success or failure
     */
    suspend operator fun invoke(journeyId: Long): Result<Unit> {
        return journeyRepository.deleteJourney(journeyId)
    }
}
```

---

## Use Case Testing

### Testing Strategy
- Unit test each use case in isolation
- Mock repository dependencies with MockK
- Test business logic validation
- Verify repository methods called correctly

### Example Test

```kotlin
class CreateJourneyUseCaseTest {

    private lateinit var journeyRepository: JourneyRepository
    private lateinit var useCase: CreateJourneyUseCase

    @Before
    fun setup() {
        journeyRepository = mockk()
        useCase = CreateJourneyUseCase(journeyRepository)
    }

    @Test
    fun `creating journey with valid data returns success`() = runTest {
        // Given
        val name = "Summer Vacation 2025"
        val passIds = listOf("pass1", "pass2")
        val expectedJourney = mockk<Journey>()
        coEvery { journeyRepository.createJourney(name, passIds) } returns Result.success(expectedJourney)

        // When
        val result = useCase(name, passIds)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedJourney, result.getOrNull())
        coVerify { journeyRepository.createJourney(name, passIds) }
    }

    @Test
    fun `creating journey with empty name returns failure`() = runTest {
        // Given
        val name = "   " // Blank
        val passIds = listOf("pass1")

        // When
        val result = useCase(name, passIds)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        coVerify(exactly = 0) { journeyRepository.createJourney(any(), any()) }
    }

    @Test
    fun `creating journey with empty pass list returns failure`() = runTest {
        // Given
        val name = "Test Journey"
        val passIds = emptyList<String>()

        // When
        val result = useCase(name, passIds)

        // Then
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { journeyRepository.createJourney(any(), any()) }
    }

    @Test
    fun `creating journey trims whitespace from name`() = runTest {
        // Given
        val name = "  Trip 2025  "
        val passIds = listOf("pass1")
        val expectedJourney = mockk<Journey>()
        coEvery { journeyRepository.createJourney("Trip 2025", passIds) } returns Result.success(expectedJourney)

        // When
        val result = useCase(name, passIds)

        // Then
        assertTrue(result.isSuccess)
        coVerify { journeyRepository.createJourney("Trip 2025", passIds) }
    }
}
```

---

## Dependency Injection

Use cases are provided via Hilt modules.

```kotlin
package labs.claucookie.pasbuk.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import labs.claucookie.pasbuk.domain.repository.PassRepository
import labs.claucookie.pasbuk.domain.usecase.*

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    fun provideImportPassUseCase(
        passRepository: PassRepository
    ): ImportPassUseCase = ImportPassUseCase(passRepository)

    @Provides
    fun provideGetTimelineUseCase(
        passRepository: PassRepository
    ): GetTimelineUseCase = GetTimelineUseCase(passRepository)

    @Provides
    fun provideCreateJourneyUseCase(
        journeyRepository: JourneyRepository
    ): CreateJourneyUseCase = CreateJourneyUseCase(journeyRepository)

    // ... other use cases
}
```

---

## Design Principles

### Single Responsibility
Each use case does exactly one thing - no combining multiple operations.

### Validation at Boundaries
Use cases validate input before calling repositories. This keeps validation logic centralized and testable.

### Explicit Error Handling
Use `Result<T>` for expected failures (validation, duplicates). Throw exceptions for unexpected errors (database corruption, I/O).

### Thin Layer
Use cases should be thin - mostly delegating to repositories with some validation. Complex orchestration is a code smell.

### Testability
All dependencies injected, easy to mock. Pure functions where possible.
