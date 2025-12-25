package labs.claucookie.pasbuk.data.repository

import android.content.Context
import android.net.Uri
import android.os.StatFs
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.data.local.dao.PassDao
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.data.parser.PkpassParser
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.DuplicatePassException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Instant

/**
 * Unit tests for PassRepositoryImpl (T036).
 *
 * Tests verify that:
 * - Import flow works correctly (parse, check duplicate, save)
 * - Duplicate detection works by serial number
 * - Get and delete operations work correctly
 * - Data flows from DAO are properly transformed
 */
class PassRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var passDao: PassDao
    private lateinit var pkpassParser: PkpassParser
    private lateinit var moshi: Moshi
    private lateinit var repository: PassRepositoryImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        passDao = mockk(relaxed = true)
        pkpassParser = mockk()
        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        // Mock storage check - provide sufficient storage by default
        every { context.filesDir } returns File("/mock/path")
        mockkConstructor(StatFs::class)
        every { anyConstructed<StatFs>().availableBytes } returns 100L * 1024 * 1024 // 100MB

        repository = PassRepositoryImpl(context, passDao, pkpassParser, moshi)
    }

    @After
    fun tearDown() {
        unmockkConstructor(StatFs::class)
    }

    // ==================== Import Tests ====================

    @Test
    fun `importPass parses file and saves to database when no duplicate exists`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val parsedPass = createTestPass("new-pass-123")

        every { pkpassParser.parse(uri) } returns parsedPass
        coEvery { passDao.getBySerialNumber(parsedPass.serialNumber) } returns null
        coEvery { passDao.insert(any()) } just Runs

        // When
        val result = repository.importPass(uri)

        // Then
        assertEquals(parsedPass.id, result.id)
        assertEquals(parsedPass.serialNumber, result.serialNumber)
        verify { pkpassParser.parse(uri) }
        coVerify { passDao.getBySerialNumber(parsedPass.serialNumber) }
        coVerify { passDao.insert(any()) }
    }

    @Test(expected = DuplicatePassException::class)
    fun `importPass throws DuplicatePassException when serial number exists`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val parsedPass = createTestPass("duplicate-pass")
        val existingEntity = createTestPassEntity("existing-id", parsedPass.serialNumber)

        every { pkpassParser.parse(uri) } returns parsedPass
        coEvery { passDao.getBySerialNumber(parsedPass.serialNumber) } returns existingEntity
        every { pkpassParser.deletePassFiles(parsedPass.id) } just Runs

        // When
        repository.importPass(uri)

        // Then: DuplicatePassException is thrown
    }

    @Test
    fun `importPass cleans up parsed files when duplicate is detected`() = runTest {
        // Given
        val uri = mockk<Uri>()
        val parsedPass = createTestPass("cleanup-test-pass")
        val existingEntity = createTestPassEntity("existing-id", parsedPass.serialNumber)

        every { pkpassParser.parse(uri) } returns parsedPass
        coEvery { passDao.getBySerialNumber(parsedPass.serialNumber) } returns existingEntity
        every { pkpassParser.deletePassFiles(parsedPass.id) } just Runs

        // When
        try {
            repository.importPass(uri)
        } catch (e: DuplicatePassException) {
            // Expected
        }

        // Then: Files are cleaned up
        verify { pkpassParser.deletePassFiles(parsedPass.id) }
    }

    // ==================== Get By ID Tests ====================

    @Test
    fun `getPassById returns pass when it exists`() = runTest {
        // Given
        val passId = "existing-pass-id"
        val passEntity = createTestPassEntity(passId, "SERIAL-$passId")
        coEvery { passDao.getById(passId) } returns passEntity

        // When
        val result = repository.getPassById(passId)

        // Then
        assertNotNull(result)
        assertEquals(passId, result?.id)
        coVerify { passDao.getById(passId) }
    }

    @Test
    fun `getPassById returns null when pass does not exist`() = runTest {
        // Given
        val passId = "non-existent-pass"
        coEvery { passDao.getById(passId) } returns null

        // When
        val result = repository.getPassById(passId)

        // Then
        assertNull(result)
    }

    // ==================== Get All Sorted Tests ====================

    @Test
    fun `getAllPassesSortedByDate returns flow of passes from dao`() = runTest {
        // Given
        val entities = listOf(
            createTestPassEntity("pass-1", "SERIAL-1"),
            createTestPassEntity("pass-2", "SERIAL-2"),
            createTestPassEntity("pass-3", "SERIAL-3")
        )
        every { passDao.getAllSortedByDate() } returns flowOf(entities)

        // When
        val result = repository.getAllPassesSortedByDate().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("pass-1", result[0].id)
        assertEquals("pass-2", result[1].id)
        assertEquals("pass-3", result[2].id)
    }

    @Test
    fun `getAllPassesSortedByDate returns empty list when no passes exist`() = runTest {
        // Given
        every { passDao.getAllSortedByDate() } returns flowOf(emptyList())

        // When
        val result = repository.getAllPassesSortedByDate().first()

        // Then
        assertTrue(result.isEmpty())
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deletePass removes from database and deletes files`() = runTest {
        // Given
        val passId = "pass-to-delete"
        val passEntity = createTestPassEntity(passId, "SERIAL-DELETE")
        coEvery { passDao.getById(passId) } returns passEntity
        coEvery { passDao.delete(passEntity) } just Runs
        every { pkpassParser.deletePassFiles(passId) } just Runs

        // When
        repository.deletePass(passId)

        // Then
        coVerify { passDao.delete(passEntity) }
        verify { pkpassParser.deletePassFiles(passId) }
    }

    @Test
    fun `deletePass does nothing when pass does not exist`() = runTest {
        // Given
        val passId = "non-existent-pass"
        coEvery { passDao.getById(passId) } returns null

        // When
        repository.deletePass(passId)

        // Then
        coVerify(exactly = 0) { passDao.delete(any()) }
        verify(exactly = 0) { pkpassParser.deletePassFiles(any()) }
    }

    // ==================== Helper Methods ====================

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

    private fun createTestPassEntity(id: String, serialNumber: String): PassEntity {
        val now = System.currentTimeMillis()
        return PassEntity(
            id = id,
            serialNumber = serialNumber,
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass",
            teamIdentifier = "TEAM123",
            relevantDate = now,
            expirationDate = null,
            locationsJson = "[]",
            barcodeJson = null,
            fieldsJson = "{}",
            logoText = null,
            backgroundColor = null,
            foregroundColor = null,
            labelColor = null,
            logoImagePath = null,
            iconImagePath = null,
            thumbnailImagePath = null,
            stripImagePath = null,
            backgroundImagePath = null,
            originalPkpassPath = "/path/to/$id.pkpass",
            passType = "GENERIC",
            createdAt = now,
            modifiedAt = now
        )
    }
}
