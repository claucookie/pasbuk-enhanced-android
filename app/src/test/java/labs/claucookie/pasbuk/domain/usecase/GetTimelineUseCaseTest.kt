package labs.claucookie.pasbuk.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.PassRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for GetTimelineUseCase (T059).
 *
 * Tests verify that:
 * - Use case returns Flow from repository
 * - Passes are sorted correctly (most recent first)
 * - Empty list is handled correctly
 */
class GetTimelineUseCaseTest {

    private lateinit var passRepository: PassRepository
    private lateinit var getTimelineUseCase: GetTimelineUseCase

    @Before
    fun setup() {
        passRepository = mockk()
        getTimelineUseCase = GetTimelineUseCase(passRepository)
    }

    @Test
    fun `invoke returns flow of passes from repository`() = runTest {
        // Given
        val passes = listOf(
            createTestPass("pass-1", daysAgo = 1),
            createTestPass("pass-2", daysAgo = 3),
            createTestPass("pass-3", daysAgo = 2)
        )
        every { passRepository.getAllPassesSortedByDate() } returns flowOf(passes)

        // When
        val result = getTimelineUseCase().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("pass-1", result[0].id)
        assertEquals("pass-2", result[1].id)
        assertEquals("pass-3", result[2].id)
        verify { passRepository.getAllPassesSortedByDate() }
    }

    @Test
    fun `invoke returns empty list when no passes exist`() = runTest {
        // Given
        every { passRepository.getAllPassesSortedByDate() } returns flowOf(emptyList())

        // When
        val result = getTimelineUseCase().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke returns passes sorted by date descending`() = runTest {
        // Given: Passes already sorted by repository (most recent first)
        val oldestPass = createTestPass("old-pass", daysAgo = 10)
        val middlePass = createTestPass("middle-pass", daysAgo = 5)
        val newestPass = createTestPass("new-pass", daysAgo = 1)

        val sortedPasses = listOf(newestPass, middlePass, oldestPass)
        every { passRepository.getAllPassesSortedByDate() } returns flowOf(sortedPasses)

        // When
        val result = getTimelineUseCase().first()

        // Then: Verify order is maintained (most recent first)
        assertEquals(3, result.size)
        assertEquals("new-pass", result[0].id)
        assertEquals("middle-pass", result[1].id)
        assertEquals("old-pass", result[2].id)

        // Verify dates are in descending order
        assertTrue(result[0].relevantDate!! > result[1].relevantDate!!)
        assertTrue(result[1].relevantDate!! > result[2].relevantDate!!)
    }

    @Test
    fun `invoke handles passes with null relevantDate`() = runTest {
        // Given: Mix of passes with and without dates
        val passWithDate = createTestPass("with-date", daysAgo = 5)
        val passWithoutDate = createTestPass("no-date", daysAgo = null)

        // Repository returns passes with null dates last
        val sortedPasses = listOf(passWithDate, passWithoutDate)
        every { passRepository.getAllPassesSortedByDate() } returns flowOf(sortedPasses)

        // When
        val result = getTimelineUseCase().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("with-date", result[0].id)
        assertEquals("no-date", result[1].id)
    }

    @Test
    fun `invoke calls repository exactly once`() = runTest {
        // Given
        every { passRepository.getAllPassesSortedByDate() } returns flowOf(emptyList())

        // When
        getTimelineUseCase().first()

        // Then
        verify(exactly = 1) { passRepository.getAllPassesSortedByDate() }
    }

    private fun createTestPass(id: String, daysAgo: Int?): Pass {
        val relevantDate = daysAgo?.let {
            Instant.now().minusSeconds((it * 24 * 60 * 60).toLong())
        }

        return Pass(
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
