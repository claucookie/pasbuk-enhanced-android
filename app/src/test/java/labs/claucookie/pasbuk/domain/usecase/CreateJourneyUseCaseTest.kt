package labs.claucookie.pasbuk.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.DuplicateJourneyNameException
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for CreateJourneyUseCase (T073).
 *
 * Tests verify that:
 * - Journey name validation works correctly
 * - Valid journey creation succeeds
 * - Empty name throws exception
 * - Blank name throws exception
 * - Duplicate name throws exception
 * - Empty pass list throws exception
 */
class CreateJourneyUseCaseTest {

    private lateinit var journeyRepository: JourneyRepository
    private lateinit var createJourneyUseCase: CreateJourneyUseCase

    @Before
    fun setup() {
        journeyRepository = mockk()
        createJourneyUseCase = CreateJourneyUseCase(journeyRepository)
    }

    @Test
    fun `invoke with valid name and passes succeeds`() = runTest {
        // Given
        val name = "Summer Vacation"
        val passIds = listOf("pass-1", "pass-2", "pass-3")
        val expectedJourney = createTestJourney(1, name, passIds)
        coEvery { journeyRepository.createJourney(name, passIds) } returns expectedJourney

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { journey ->
                assertEquals(name, journey.name)
                assertEquals(3, journey.passCount)
                coVerify { journeyRepository.createJourney(name, passIds) }
            },
            onFailure = { throw AssertionError("Expected success but got failure: $it") }
        )
    }

    @Test
    fun `invoke with empty name returns failure`() = runTest {
        // Given
        val name = ""
        val passIds = listOf("pass-1")

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { throw AssertionError("Expected failure but got success") },
            onFailure = { exception ->
                assertTrue(exception is IllegalArgumentException)
                assertEquals("Journey name cannot be empty", exception.message)
            }
        )
        coVerify(exactly = 0) { journeyRepository.createJourney(any(), any()) }
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        // Given
        val name = "   "
        val passIds = listOf("pass-1")

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { throw AssertionError("Expected failure but got success") },
            onFailure = { exception ->
                assertTrue(exception is IllegalArgumentException)
                assertEquals("Journey name cannot be empty", exception.message)
            }
        )
        coVerify(exactly = 0) { journeyRepository.createJourney(any(), any()) }
    }

    @Test
    fun `invoke with empty pass list returns failure`() = runTest {
        // Given
        val name = "Empty Journey"
        val passIds = emptyList<String>()

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { throw AssertionError("Expected failure but got success") },
            onFailure = { exception ->
                assertTrue(exception is IllegalArgumentException)
                assertEquals("Journey must contain at least one pass", exception.message)
            }
        )
        coVerify(exactly = 0) { journeyRepository.createJourney(any(), any()) }
    }

    @Test
    fun `invoke with duplicate name returns failure`() = runTest {
        // Given
        val name = "Existing Journey"
        val passIds = listOf("pass-1")
        coEvery { journeyRepository.createJourney(name, passIds) } throws DuplicateJourneyNameException(name)

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { throw AssertionError("Expected failure but got success") },
            onFailure = { exception ->
                assertTrue(exception is DuplicateJourneyNameException)
                coVerify { journeyRepository.createJourney(name, passIds) }
            }
        )
    }

    @Test
    fun `invoke trims whitespace from name`() = runTest {
        // Given
        val name = "  Trip to Paris  "
        val trimmedName = "Trip to Paris"
        val passIds = listOf("pass-1")
        val expectedJourney = createTestJourney(1, trimmedName, passIds)
        coEvery { journeyRepository.createJourney(trimmedName, passIds) } returns expectedJourney

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { journey ->
                assertEquals(trimmedName, journey.name)
                coVerify { journeyRepository.createJourney(trimmedName, passIds) }
            },
            onFailure = { throw AssertionError("Expected success but got failure: $it") }
        )
    }

    @Test
    fun `invoke with repository error returns failure`() = runTest {
        // Given
        val name = "Test Journey"
        val passIds = listOf("pass-1")
        val exception = RuntimeException("Database error")
        coEvery { journeyRepository.createJourney(name, passIds) } throws exception

        // When
        val result = createJourneyUseCase(name, passIds)

        // Then
        result.fold(
            onSuccess = { throw AssertionError("Expected failure but got success") },
            onFailure = { error ->
                assertEquals(exception, error)
                coVerify { journeyRepository.createJourney(name, passIds) }
            }
        )
    }

    private fun createTestJourney(id: Long, name: String, passIds: List<String>): Journey {
        val passes = passIds.map { passId ->
            labs.claucookie.pasbuk.domain.model.Pass(
                id = passId,
                serialNumber = "SERIAL-$passId",
                passTypeIdentifier = "pass.com.example",
                organizationName = "Test Organization",
                description = "Test Pass $passId",
                teamIdentifier = "TEAM123",
                relevantDate = Instant.now(),
                expirationDate = null,
                locations = emptyList(),
                logoText = null,
                backgroundColor = null,
                foregroundColor = null,
                labelColor = null,
                barcode = null,
                logoImagePath = null,
                iconImagePath = null,
                thumbnailImagePath = null,
                stripImagePath = null,
                backgroundImagePath = null,
                originalPkpassPath = "/path/to/$passId.pkpass",
                passType = PassType.GENERIC,
                fields = emptyMap(),
                createdAt = Instant.now(),
                modifiedAt = Instant.now()
            )
        }

        return Journey(
            id = id,
            name = name,
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }
}
