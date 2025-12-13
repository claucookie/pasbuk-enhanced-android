package labs.claucookie.pasbuk.domain.usecase

import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.DuplicatePassException
import labs.claucookie.pasbuk.domain.repository.InvalidPassException
import labs.claucookie.pasbuk.domain.repository.PassRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for ImportPassUseCase (T034).
 *
 * Tests verify that:
 * - Successful imports return Result.success with the pass
 * - Repository exceptions are wrapped in Result.failure
 * - Different exception types are properly propagated
 */
class ImportPassUseCaseTest {

    private lateinit var passRepository: PassRepository
    private lateinit var importPassUseCase: ImportPassUseCase

    @Before
    fun setup() {
        passRepository = mockk()
        importPassUseCase = ImportPassUseCase(passRepository)
    }

    @Test
    fun `invoke returns success with pass when import succeeds`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val expectedPass = createTestPass("pass-123")
        coEvery { passRepository.importPass(uri) } returns expectedPass

        // When
        val result = importPassUseCase(uri)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedPass, result.getOrNull())
        coVerify(exactly = 1) { passRepository.importPass(uri) }
    }

    @Test
    fun `invoke returns failure when repository throws InvalidPassException`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val exception = InvalidPassException("Invalid pass file")
        coEvery { passRepository.importPass(uri) } throws exception

        // When
        val result = importPassUseCase(uri)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidPassException)
        assertEquals("Invalid pass file", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke returns failure when repository throws DuplicatePassException`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val exception = DuplicatePassException(serialNumber = "TEST-SERIAL-123")
        coEvery { passRepository.importPass(uri) } throws exception

        // When
        val result = importPassUseCase(uri)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DuplicatePassException)
        assertTrue(result.exceptionOrNull()?.message?.contains("TEST-SERIAL-123") == true)
    }

    @Test
    fun `invoke returns failure when repository throws generic exception`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val exception = RuntimeException("Unexpected error")
        coEvery { passRepository.importPass(uri) } throws exception

        // When
        val result = importPassUseCase(uri)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Unexpected error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke calls repository with correct URI`() = runTest {
        // Given
        val uri = mockk<Uri>()
        coEvery { passRepository.importPass(uri) } returns createTestPass("pass-456")

        // When
        importPassUseCase(uri)

        // Then
        coVerify { passRepository.importPass(uri) }
    }

    private fun createTestPass(id: String): Pass {
        return Pass(
            id = id,
            serialNumber = "SERIAL-$id",
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass",
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
            originalPkpassPath = "/path/to/$id.pkpass",
            passType = PassType.GENERIC,
            fields = emptyMap(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }
}
