package labs.claucookie.pasbuk.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for GetJourneyDetailUseCase (T075).
 *
 * Tests verify that:
 * - Use case returns journey from repository
 * - Returns null when journey not found
 * - Journey with passes is returned correctly
 * - Journey passes are sorted by relevantDate
 */
class GetJourneyDetailUseCaseTest {

    private lateinit var journeyRepository: JourneyRepository
    private lateinit var getJourneyDetailUseCase: GetJourneyDetailUseCase

    @Before
    fun setup() {
        journeyRepository = mockk()
        getJourneyDetailUseCase = GetJourneyDetailUseCase(journeyRepository)
    }

    @Test
    fun `invoke returns journey from repository`() = runTest {
        // Given
        val journeyId = 1L
        val journey = createTestJourney(journeyId, "Test Journey", listOf("pass-1", "pass-2"))
        coEvery { journeyRepository.getJourneyById(journeyId) } returns journey

        // When
        val result = getJourneyDetailUseCase(journeyId)

        // Then
        assertEquals(journey, result)
        assertEquals("Test Journey", result?.name)
        assertEquals(2, result?.passCount)
        coVerify { journeyRepository.getJourneyById(journeyId) }
    }

    @Test
    fun `invoke returns null when journey not found`() = runTest {
        // Given
        val journeyId = 999L
        coEvery { journeyRepository.getJourneyById(journeyId) } returns null

        // When
        val result = getJourneyDetailUseCase(journeyId)

        // Then
        assertNull(result)
        coVerify { journeyRepository.getJourneyById(journeyId) }
    }

    @Test
    fun `invoke returns journey with empty passes list`() = runTest {
        // Given
        val journeyId = 1L
        val journey = createTestJourney(journeyId, "Empty Journey", emptyList())
        coEvery { journeyRepository.getJourneyById(journeyId) } returns journey

        // When
        val result = getJourneyDetailUseCase(journeyId)

        // Then
        assertEquals(journey, result)
        assertEquals(0, result?.passCount)
    }

    @Test
    fun `invoke returns journey with sorted passes`() = runTest {
        // Given
        val journeyId = 1L
        val now = Instant.now()
        val oldestPass = createTestPass("pass-3", now.minusSeconds(60 * 60 * 24 * 7)) // 7 days ago
        val middlePass = createTestPass("pass-2", now.minusSeconds(60 * 60 * 24)) // 1 day ago
        val newestPass = createTestPass("pass-1", now.minusSeconds(60 * 60)) // 1 hour ago

        val journey = Journey(
            id = journeyId,
            name = "Sorted Journey",
            passes = listOf(newestPass, middlePass, oldestPass), // Already sorted by repository
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        coEvery { journeyRepository.getJourneyById(journeyId) } returns journey

        // When
        val result = getJourneyDetailUseCase(journeyId)

        // Then
        assertEquals(3, result?.passCount)
        assertEquals("pass-1", result?.passes?.get(0)?.id)
        assertEquals("pass-2", result?.passes?.get(1)?.id)
        assertEquals("pass-3", result?.passes?.get(2)?.id)
    }

    @Test
    fun `invoke calls repository exactly once`() = runTest {
        // Given
        val journeyId = 1L
        coEvery { journeyRepository.getJourneyById(journeyId) } returns null

        // When
        getJourneyDetailUseCase(journeyId)

        // Then
        coVerify(exactly = 1) { journeyRepository.getJourneyById(journeyId) }
    }

    @Test
    fun `invoke returns journey with date range`() = runTest {
        // Given
        val journeyId = 1L
        val now = Instant.now()
        val passes = listOf(
            createTestPass("pass-1", now.minusSeconds(60 * 60 * 24 * 7)),
            createTestPass("pass-2", now.minusSeconds(60 * 60 * 24)),
            createTestPass("pass-3", now.minusSeconds(60 * 60))
        )
        val journey = Journey(
            id = journeyId,
            name = "Date Range Journey",
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
        coEvery { journeyRepository.getJourneyById(journeyId) } returns journey

        // When
        val result = getJourneyDetailUseCase(journeyId)

        // Then
        assertEquals(journey, result)
        assertEquals(3, result?.passCount)
        // dateRange should span from oldest to newest
        val dateRange = result?.dateRange
        assertEquals(passes[0].relevantDate, dateRange?.start)
        assertEquals(passes[2].relevantDate, dateRange?.endInclusive)
    }

    private fun createTestJourney(id: Long, name: String, passIds: List<String>): Journey {
        val passes = passIds.map { passId ->
            createTestPass(passId, Instant.now())
        }

        return Journey(
            id = id,
            name = name,
            passes = passes,
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }

    private fun createTestPass(id: String, relevantDate: Instant): labs.claucookie.pasbuk.domain.model.Pass {
        return labs.claucookie.pasbuk.domain.model.Pass(
            id = id,
            serialNumber = "SERIAL-$id",
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass $id",
            teamIdentifier = "TEAM123",
            relevantDate = relevantDate,
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
            originalPkpassPath = "/path/to/$id.pkpass",
            passType = PassType.GENERIC,
            fields = emptyMap(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }
}
