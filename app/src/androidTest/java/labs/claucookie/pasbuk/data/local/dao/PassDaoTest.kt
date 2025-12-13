package labs.claucookie.pasbuk.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.data.local.AppDatabase
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for PassDao (T037).
 *
 * Uses an in-memory Room database to test:
 * - CRUD operations (insert, query, update, delete)
 * - Queries by ID and serial number
 * - Sorting by relevant date
 * - Unique constraint on serial number
 */
@RunWith(AndroidJUnit4::class)
class PassDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var passDao: PassDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        passDao = database.passDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== Insert Tests ====================

    @Test
    fun insert_savesPassToDatabase() = runTest {
        // Given
        val pass = createTestPassEntity("pass-1", "SERIAL-1")

        // When
        passDao.insert(pass)

        // Then
        val retrieved = passDao.getById("pass-1")
        assertNotNull(retrieved)
        assertEquals("pass-1", retrieved?.id)
        assertEquals("SERIAL-1", retrieved?.serialNumber)
    }

    @Test
    fun insert_multiplePassesAreAllSaved() = runTest {
        // Given
        val pass1 = createTestPassEntity("pass-1", "SERIAL-1")
        val pass2 = createTestPassEntity("pass-2", "SERIAL-2")
        val pass3 = createTestPassEntity("pass-3", "SERIAL-3")

        // When
        passDao.insert(pass1)
        passDao.insert(pass2)
        passDao.insert(pass3)

        // Then
        val allPasses = passDao.getAll().first()
        assertEquals(3, allPasses.size)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun insert_duplicateSerialNumberThrowsException() = runTest {
        // Given
        val pass1 = createTestPassEntity("pass-1", "SAME-SERIAL")
        val pass2 = createTestPassEntity("pass-2", "SAME-SERIAL")

        // When
        passDao.insert(pass1)
        passDao.insert(pass2) // Should throw due to unique constraint
    }

    // ==================== Query Tests ====================

    @Test
    fun getById_returnsCorrectPass() = runTest {
        // Given
        val pass = createTestPassEntity("target-pass", "SERIAL-TARGET")
        passDao.insert(pass)
        passDao.insert(createTestPassEntity("other-pass", "SERIAL-OTHER"))

        // When
        val result = passDao.getById("target-pass")

        // Then
        assertNotNull(result)
        assertEquals("target-pass", result?.id)
        assertEquals("SERIAL-TARGET", result?.serialNumber)
    }

    @Test
    fun getById_returnsNullForNonExistentPass() = runTest {
        // Given
        passDao.insert(createTestPassEntity("existing-pass", "SERIAL-1"))

        // When
        val result = passDao.getById("non-existent-pass")

        // Then
        assertNull(result)
    }

    @Test
    fun getBySerialNumber_returnsCorrectPass() = runTest {
        // Given
        val pass = createTestPassEntity("pass-1", "UNIQUE-SERIAL-123")
        passDao.insert(pass)

        // When
        val result = passDao.getBySerialNumber("UNIQUE-SERIAL-123")

        // Then
        assertNotNull(result)
        assertEquals("pass-1", result?.id)
    }

    @Test
    fun getBySerialNumber_returnsNullForNonExistentSerial() = runTest {
        // Given
        passDao.insert(createTestPassEntity("pass-1", "SERIAL-1"))

        // When
        val result = passDao.getBySerialNumber("NON-EXISTENT-SERIAL")

        // Then
        assertNull(result)
    }

    // ==================== Sorting Tests ====================

    @Test
    fun getAllSortedByDate_returnsPassesSortedByRelevantDateDescending() = runTest {
        // Given: Passes with different relevant dates
        val oldestDate = System.currentTimeMillis() - 3000000
        val middleDate = System.currentTimeMillis() - 2000000
        val newestDate = System.currentTimeMillis() - 1000000

        passDao.insert(createTestPassEntity("oldest", "SERIAL-OLD", relevantDate = oldestDate))
        passDao.insert(createTestPassEntity("newest", "SERIAL-NEW", relevantDate = newestDate))
        passDao.insert(createTestPassEntity("middle", "SERIAL-MID", relevantDate = middleDate))

        // When
        val result = passDao.getAllSortedByDate().first()

        // Then: Sorted descending by date (newest first)
        assertEquals(3, result.size)
        assertEquals("newest", result[0].id)
        assertEquals("middle", result[1].id)
        assertEquals("oldest", result[2].id)
    }

    @Test
    fun getAllSortedByDate_nullDatesAreLastInSorting() = runTest {
        // Given: Mix of passes with and without dates
        val withDate = System.currentTimeMillis()

        passDao.insert(createTestPassEntity("with-date", "SERIAL-1", relevantDate = withDate))
        passDao.insert(createTestPassEntity("no-date", "SERIAL-2", relevantDate = null))

        // When
        val result = passDao.getAllSortedByDate().first()

        // Then: Passes with dates come first
        assertEquals(2, result.size)
        assertEquals("with-date", result[0].id)
        assertEquals("no-date", result[1].id)
    }

    @Test
    fun getAllSortedByDate_emptyDatabaseReturnsEmptyList() = runTest {
        // When
        val result = passDao.getAllSortedByDate().first()

        // Then
        assertTrue(result.isEmpty())
    }

    // ==================== Update Tests ====================

    @Test
    fun update_modifiesExistingPass() = runTest {
        // Given
        val original = createTestPassEntity("pass-1", "SERIAL-1")
        passDao.insert(original)

        // When
        val updated = original.copy(
            description = "Updated Description",
            modifiedAt = System.currentTimeMillis()
        )
        passDao.update(updated)

        // Then
        val result = passDao.getById("pass-1")
        assertEquals("Updated Description", result?.description)
    }

    // ==================== Delete Tests ====================

    @Test
    fun delete_removesPassFromDatabase() = runTest {
        // Given
        val pass = createTestPassEntity("pass-to-delete", "SERIAL-DELETE")
        passDao.insert(pass)
        assertNotNull(passDao.getById("pass-to-delete"))

        // When
        passDao.delete(pass)

        // Then
        assertNull(passDao.getById("pass-to-delete"))
    }

    @Test
    fun deleteAll_removesAllPasses() = runTest {
        // Given
        passDao.insert(createTestPassEntity("pass-1", "SERIAL-1"))
        passDao.insert(createTestPassEntity("pass-2", "SERIAL-2"))
        passDao.insert(createTestPassEntity("pass-3", "SERIAL-3"))
        assertEquals(3, passDao.getAll().first().size)

        // When
        passDao.deleteAll()

        // Then
        assertTrue(passDao.getAll().first().isEmpty())
    }

    // ==================== Flow Tests ====================

    @Test
    fun getAllSortedByDate_flowEmitsUpdatesOnInsert() = runTest {
        // Given
        passDao.insert(createTestPassEntity("pass-1", "SERIAL-1"))
        var count = passDao.getAllSortedByDate().first().size
        assertEquals(1, count)

        // When
        passDao.insert(createTestPassEntity("pass-2", "SERIAL-2"))

        // Then
        count = passDao.getAllSortedByDate().first().size
        assertEquals(2, count)
    }

    // ==================== Helper Methods ====================

    private fun createTestPassEntity(
        id: String,
        serialNumber: String,
        relevantDate: Long? = System.currentTimeMillis()
    ): PassEntity {
        val now = System.currentTimeMillis()
        return PassEntity(
            id = id,
            serialNumber = serialNumber,
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass Description",
            teamIdentifier = "TEAM123",
            relevantDate = relevantDate,
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
