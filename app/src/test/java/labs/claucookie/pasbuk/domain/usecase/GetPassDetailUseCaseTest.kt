package labs.claucookie.pasbuk.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.BarcodeFormat
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.PassRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for GetPassDetailUseCase (T035).
 *
 * Tests verify that:
 * - Existing passes are returned correctly
 * - Non-existent passes return null
 * - Repository is called with correct pass ID
 */
class GetPassDetailUseCaseTest {

    private lateinit var passRepository: PassRepository
    private lateinit var getPassDetailUseCase: GetPassDetailUseCase

    @Before
    fun setup() {
        passRepository = mockk()
        getPassDetailUseCase = GetPassDetailUseCase(passRepository)
    }

    @Test
    fun `invoke returns pass when it exists`() = runTest {
        // Given
        val passId = "test-pass-123"
        val expectedPass = createTestPass(passId)
        coEvery { passRepository.getPassById(passId) } returns expectedPass

        // When
        val result = getPassDetailUseCase(passId)

        // Then
        assertNotNull(result)
        assertEquals(expectedPass, result)
        assertEquals(passId, result?.id)
    }

    @Test
    fun `invoke returns null when pass does not exist`() = runTest {
        // Given
        val passId = "non-existent-pass"
        coEvery { passRepository.getPassById(passId) } returns null

        // When
        val result = getPassDetailUseCase(passId)

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke calls repository with correct pass ID`() = runTest {
        // Given
        val passId = "test-pass-456"
        coEvery { passRepository.getPassById(passId) } returns createTestPass(passId)

        // When
        getPassDetailUseCase(passId)

        // Then
        coVerify(exactly = 1) { passRepository.getPassById(passId) }
    }

    @Test
    fun `invoke returns pass with all fields populated`() = runTest {
        // Given
        val passId = "full-pass-789"
        val expectedPass = createFullPass(passId)
        coEvery { passRepository.getPassById(passId) } returns expectedPass

        // When
        val result = getPassDetailUseCase(passId)

        // Then
        assertNotNull(result)
        assertEquals("Full Pass Organization", result?.organizationName)
        assertEquals("Full Pass Description", result?.description)
        assertNotNull(result?.barcode)
        assertEquals(BarcodeFormat.QR, result?.barcode?.format)
        assertEquals("BARCODE-MESSAGE", result?.barcode?.message)
        assertEquals(PassType.EVENT_TICKET, result?.passType)
        assertNotNull(result?.relevantDate)
    }

    @Test
    fun `invoke returns pass with minimal fields`() = runTest {
        // Given
        val passId = "minimal-pass"
        val minimalPass = createMinimalPass(passId)
        coEvery { passRepository.getPassById(passId) } returns minimalPass

        // When
        val result = getPassDetailUseCase(passId)

        // Then
        assertNotNull(result)
        assertNull(result?.barcode)
        assertNull(result?.relevantDate)
        assertNull(result?.logoImagePath)
    }

    @Test
    fun `invoke handles multiple sequential calls`() = runTest {
        // Given
        val passId1 = "pass-1"
        val passId2 = "pass-2"
        val pass1 = createTestPass(passId1)
        val pass2 = createTestPass(passId2)
        coEvery { passRepository.getPassById(passId1) } returns pass1
        coEvery { passRepository.getPassById(passId2) } returns pass2

        // When
        val result1 = getPassDetailUseCase(passId1)
        val result2 = getPassDetailUseCase(passId2)

        // Then
        assertEquals(passId1, result1?.id)
        assertEquals(passId2, result2?.id)
        coVerify(exactly = 1) { passRepository.getPassById(passId1) }
        coVerify(exactly = 1) { passRepository.getPassById(passId2) }
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

    private fun createFullPass(id: String): Pass {
        return Pass(
            id = id,
            serialNumber = "FULL-SERIAL-$id",
            passTypeIdentifier = "pass.com.full.example",
            organizationName = "Full Pass Organization",
            description = "Full Pass Description",
            teamIdentifier = "TEAM-FULL",
            relevantDate = Instant.parse("2024-12-25T10:30:00Z"),
            expirationDate = Instant.parse("2025-12-31T23:59:59Z"),
            locations = emptyList(),
            logoText = "LOGO TEXT",
            backgroundColor = "rgb(255, 0, 0)",
            foregroundColor = "rgb(255, 255, 255)",
            labelColor = "rgb(200, 200, 200)",
            barcode = Barcode(
                message = "BARCODE-MESSAGE",
                format = BarcodeFormat.QR,
                messageEncoding = "iso-8859-1",
                altText = "Alt Text"
            ),
            logoImagePath = "/path/to/logo.png",
            iconImagePath = "/path/to/icon.png",
            thumbnailImagePath = "/path/to/thumbnail.png",
            stripImagePath = "/path/to/strip.png",
            backgroundImagePath = "/path/to/background.png",
            originalPkpassPath = "/path/to/$id.pkpass",
            passType = PassType.EVENT_TICKET,
            fields = mapOf(
                "event" to labs.claucookie.pasbuk.domain.model.PassField(
                    key = "event",
                    label = "Event",
                    value = "Concert",
                    textAlignment = null
                )
            ),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }

    private fun createMinimalPass(id: String): Pass {
        return Pass(
            id = id,
            serialNumber = "MINIMAL-$id",
            passTypeIdentifier = "pass.com.minimal",
            organizationName = "Minimal Org",
            description = "Minimal Pass",
            teamIdentifier = "TEAM-MIN",
            relevantDate = null,
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
