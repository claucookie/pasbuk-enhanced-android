package labs.claucookie.pasbuk.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for GetAllJourneysUseCase (T074).
 *
 * Tests verify that:
 * - Use case returns Flow from repository
 * - Journeys are returned correctly
 * - Empty list is handled correctly
 */
class GetAllJourneysUseCaseTest {

    private lateinit var journeyRepository: JourneyRepository
    private lateinit var getAllJourneysUseCase: GetAllJourneysUseCase

    @Before
    fun setup() {
        journeyRepository = mockk()
        getAllJourneysUseCase = GetAllJourneysUseCase(journeyRepository)
    }

    @Test
    fun `invoke returns flow of journeys from repository`() = runTest {
        // Given
        val journeys = listOf(
            createTestJourney(1, "Summer Trip", listOf("pass-1", "pass-2")),
            createTestJourney(2, "Winter Holidays", listOf("pass-3")),
            createTestJourney(3, "Conference", listOf("pass-4", "pass-5", "pass-6"))
        )
        every { journeyRepository.getAllJourneys() } returns flowOf(journeys)

        // When
        val result = getAllJourneysUseCase().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("Summer Trip", result[0].name)
        assertEquals(2, result[0].passCount)
        assertEquals("Winter Holidays", result[1].name)
        assertEquals(1, result[1].passCount)
        assertEquals("Conference", result[2].name)
        assertEquals(3, result[2].passCount)
        verify { journeyRepository.getAllJourneys() }
    }

    @Test
    fun `invoke returns empty list when no journeys exist`() = runTest {
        // Given
        every { journeyRepository.getAllJourneys() } returns flowOf(emptyList())

        // When
        val result = getAllJourneysUseCase().first()

        // Then
        assertTrue(result.isEmpty())
        verify { journeyRepository.getAllJourneys() }
    }

    @Test
    fun `invoke calls repository exactly once`() = runTest {
        // Given
        every { journeyRepository.getAllJourneys() } returns flowOf(emptyList())

        // When
        getAllJourneysUseCase().first()

        // Then
        verify(exactly = 1) { journeyRepository.getAllJourneys() }
    }

    @Test
    fun `invoke returns journeys with correct pass counts`() = runTest {
        // Given
        val journeyWithOnePas = createTestJourney(1, "One Pass", listOf("pass-1"))
        val journeyWithManyPasses = createTestJourney(2, "Many Passes", listOf("pass-1", "pass-2", "pass-3", "pass-4", "pass-5"))

        every { journeyRepository.getAllJourneys() } returns flowOf(listOf(journeyWithOnePas, journeyWithManyPasses))

        // When
        val result = getAllJourneysUseCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(1, result[0].passCount)
        assertEquals(5, result[1].passCount)
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
