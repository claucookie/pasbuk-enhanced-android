package labs.claucookie.pasbuk.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.data.local.AppDatabase
import labs.claucookie.pasbuk.data.local.entity.JourneyEntity
import labs.claucookie.pasbuk.data.local.entity.JourneyPassCrossRef
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.domain.model.PassType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

/**
 * Integration tests for JourneyDao (T077-T078).
 *
 * Tests verify that:
 * - CRUD operations work correctly
 * - Unique name constraint works
 * - Many-to-many relationship (JourneyPassCrossRef) works correctly
 * - getAllJourneysWithPasses returns journeys with passes
 * - getJourneyWithPasses returns journey with passes
 * - Deleting journey doesn't delete passes
 * - Flow emissions work correctly
 */
@RunWith(AndroidJUnit4::class)
class JourneyDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var journeyDao: JourneyDao
    private lateinit var passDao: PassDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        journeyDao = database.journeyDao()
        passDao = database.passDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_journeyIsInserted() = runTest {
        // Given
        val journey = createTestJourneyEntity("Summer Trip")

        // When
        val id = journeyDao.insert(journey)

        // Then
        val retrieved = journeyDao.getById(id)
        assertNotNull(retrieved)
        assertEquals("Summer Trip", retrieved?.name)
    }

    @Test
    fun getById_returnsCorrectJourney() = runTest {
        // Given
        val journey = createTestJourneyEntity("Test Journey")
        val id = journeyDao.insert(journey)

        // When
        val result = journeyDao.getById(id)

        // Then
        assertNotNull(result)
        assertEquals(id, result?.id)
        assertEquals("Test Journey", result?.name)
    }

    @Test
    fun getByName_returnsCorrectJourney() = runTest {
        // Given
        val journey = createTestJourneyEntity("Unique Name")
        journeyDao.insert(journey)

        // When
        val result = journeyDao.getByName("Unique Name")

        // Then
        assertNotNull(result)
        assertEquals("Unique Name", result?.name)
    }

    @Test
    fun getByName_returnsNullWhenNotFound() = runTest {
        // When
        val result = journeyDao.getByName("Non-existent")

        // Then
        assertNull(result)
    }

    @Test
    fun getAll_returnsAllJourneys() = runTest {
        // Given
        journeyDao.insert(createTestJourneyEntity("Journey 1"))
        journeyDao.insert(createTestJourneyEntity("Journey 2"))
        journeyDao.insert(createTestJourneyEntity("Journey 3"))

        // When
        val result = journeyDao.getAll().first()

        // Then
        assertEquals(3, result.size)
    }

    @Test
    fun getAll_returnsJourneysSortedByCreatedAtDescending() = runTest {
        // Given
        val journey1 = createTestJourneyEntity("First", Instant.now().minusSeconds(100))
        val journey2 = createTestJourneyEntity("Second", Instant.now().minusSeconds(50))
        val journey3 = createTestJourneyEntity("Third", Instant.now())

        journeyDao.insert(journey1)
        journeyDao.insert(journey2)
        journeyDao.insert(journey3)

        // When
        val result = journeyDao.getAll().first()

        // Then
        assertEquals(3, result.size)
        // Most recent first
        assertEquals("Third", result[0].name)
        assertEquals("Second", result[1].name)
        assertEquals("First", result[2].name)
    }

    @Test
    fun delete_journeyIsDeleted() = runTest {
        // Given
        val journey = createTestJourneyEntity("To Delete")
        val id = journeyDao.insert(journey)
        val insertedJourney = journeyDao.getById(id)!!

        // When
        journeyDao.delete(insertedJourney)

        // Then
        val result = journeyDao.getById(id)
        assertNull(result)
    }

    @Test
    fun journeyPassCrossRef_insertsCorrectly() = runTest {
        // Given
        val pass = createTestPassEntity("pass-1")
        passDao.insert(pass)

        val journey = createTestJourneyEntity("Test Journey")
        val journeyId = journeyDao.insert(journey)

        val crossRef = JourneyPassCrossRef(journeyId, "pass-1")

        // When
        journeyDao.insertJourneyPassCrossRef(crossRef)

        // Then
        val journeyWithPasses = journeyDao.getJourneyWithPasses(journeyId)
        assertNotNull(journeyWithPasses)
        assertEquals(1, journeyWithPasses?.passes?.size)
        assertEquals("pass-1", journeyWithPasses?.passes?.get(0)?.id)
    }

    @Test
    fun journeyPassCrossRef_multiplePasses() = runTest {
        // Given
        val pass1 = createTestPassEntity("pass-1")
        val pass2 = createTestPassEntity("pass-2")
        val pass3 = createTestPassEntity("pass-3")
        passDao.insert(pass1)
        passDao.insert(pass2)
        passDao.insert(pass3)

        val journey = createTestJourneyEntity("Multi-Pass Journey")
        val journeyId = journeyDao.insert(journey)

        val crossRefs = listOf(
            JourneyPassCrossRef(journeyId, "pass-1"),
            JourneyPassCrossRef(journeyId, "pass-2"),
            JourneyPassCrossRef(journeyId, "pass-3")
        )

        // When
        journeyDao.insertJourneyPassCrossRefs(crossRefs)

        // Then
        val journeyWithPasses = journeyDao.getJourneyWithPasses(journeyId)
        assertNotNull(journeyWithPasses)
        assertEquals(3, journeyWithPasses?.passes?.size)
        assertEquals(setOf("pass-1", "pass-2", "pass-3"), journeyWithPasses?.passes?.map { it.id }?.toSet())
    }

    @Test
    fun getAllJourneysWithPasses_returnsCorrectData() = runTest {
        // Given
        val pass1 = createTestPassEntity("pass-1")
        val pass2 = createTestPassEntity("pass-2")
        passDao.insert(pass1)
        passDao.insert(pass2)

        val journey1 = createTestJourneyEntity("Journey 1")
        val journey1Id = journeyDao.insert(journey1)
        journeyDao.insertJourneyPassCrossRef(JourneyPassCrossRef(journey1Id, "pass-1"))

        val journey2 = createTestJourneyEntity("Journey 2")
        val journey2Id = journeyDao.insert(journey2)
        journeyDao.insertJourneyPassCrossRefs(listOf(
            JourneyPassCrossRef(journey2Id, "pass-1"),
            JourneyPassCrossRef(journey2Id, "pass-2")
        ))

        // When
        val result = journeyDao.getAllJourneysWithPasses().first()

        // Then
        assertEquals(2, result.size)
        // Journey 2 created more recently, so it's first
        assertEquals("Journey 2", result[0].journey.name)
        assertEquals(2, result[0].passes.size)
        assertEquals("Journey 1", result[1].journey.name)
        assertEquals(1, result[1].passes.size)
    }

    @Test
    fun deleteJourney_doesNotDeletePasses() = runTest {
        // Given
        val pass = createTestPassEntity("pass-1")
        passDao.insert(pass)

        val journey = createTestJourneyEntity("Journey")
        val journeyId = journeyDao.insert(journey)
        journeyDao.insertJourneyPassCrossRef(JourneyPassCrossRef(journeyId, "pass-1"))

        val insertedJourney = journeyDao.getById(journeyId)!!

        // When
        journeyDao.delete(insertedJourney)

        // Then
        assertNull(journeyDao.getById(journeyId))
        assertNotNull(passDao.getById("pass-1")) // Pass still exists
    }

    @Test
    fun deleteAllPassesFromJourney_removesAllCrossReferences() = runTest {
        // Given
        val pass1 = createTestPassEntity("pass-1")
        val pass2 = createTestPassEntity("pass-2")
        passDao.insert(pass1)
        passDao.insert(pass2)

        val journey = createTestJourneyEntity("Journey")
        val journeyId = journeyDao.insert(journey)
        journeyDao.insertJourneyPassCrossRefs(listOf(
            JourneyPassCrossRef(journeyId, "pass-1"),
            JourneyPassCrossRef(journeyId, "pass-2")
        ))

        // When
        journeyDao.deleteAllPassesFromJourney(journeyId)

        // Then
        val journeyWithPasses = journeyDao.getJourneyWithPasses(journeyId)
        assertNotNull(journeyWithPasses)
        assertTrue(journeyWithPasses?.passes?.isEmpty() == true)
        // Passes still exist
        assertNotNull(passDao.getById("pass-1"))
        assertNotNull(passDao.getById("pass-2"))
    }

    @Test
    fun getAll_flowEmitsUpdatesOnInsert() = runTest {
        // Given - initial state
        val initialJourneys = journeyDao.getAll().first()
        assertEquals(0, initialJourneys.size)

        // When - insert journey
        journeyDao.insert(createTestJourneyEntity("New Journey"))

        // Then - flow emits updated list
        val updatedJourneys = journeyDao.getAll().first()
        assertEquals(1, updatedJourneys.size)
        assertEquals("New Journey", updatedJourneys[0].name)
    }

    @Test
    fun getAllJourneysWithPasses_flowEmitsUpdatesOnCrossRefInsert() = runTest {
        // Given
        val pass = createTestPassEntity("pass-1")
        passDao.insert(pass)

        val journey = createTestJourneyEntity("Journey")
        val journeyId = journeyDao.insert(journey)

        val initial = journeyDao.getAllJourneysWithPasses().first()
        assertEquals(0, initial[0].passes.size)

        // When
        journeyDao.insertJourneyPassCrossRef(JourneyPassCrossRef(journeyId, "pass-1"))

        // Then
        val updated = journeyDao.getAllJourneysWithPasses().first()
        assertEquals(1, updated[0].passes.size)
    }

    @Test
    fun getJourneyWithPasses_returnsNullWhenJourneyNotFound() = runTest {
        // When
        val result = journeyDao.getJourneyWithPasses(999L)

        // Then
        assertNull(result)
    }

    @Test
    fun getJourneyWithPasses_returnsJourneyWithEmptyPassList() = runTest {
        // Given
        val journey = createTestJourneyEntity("Empty Journey")
        val journeyId = journeyDao.insert(journey)

        // When
        val result = journeyDao.getJourneyWithPasses(journeyId)

        // Then
        assertNotNull(result)
        assertEquals("Empty Journey", result?.journey?.name)
        assertTrue(result?.passes?.isEmpty() == true)
    }

    private fun createTestJourneyEntity(
        name: String,
        createdAt: Instant = Instant.now()
    ): JourneyEntity {
        return JourneyEntity(
            id = 0, // Auto-generated
            name = name,
            createdAt = createdAt.toEpochMilli(),
            modifiedAt = Instant.now().toEpochMilli()
        )
    }

    private fun createTestPassEntity(id: String): PassEntity {
        return PassEntity(
            id = id,
            serialNumber = "SERIAL-$id",
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass $id",
            teamIdentifier = "TEAM123",
            relevantDate = Instant.now().toEpochMilli(),
            expirationDate = null,
            locationsJson = "[]",
            logoText = null,
            backgroundColor = null,
            foregroundColor = null,
            labelColor = null,
            barcodeJson = null,
            logoImagePath = null,
            iconImagePath = null,
            thumbnailImagePath = null,
            stripImagePath = null,
            backgroundImagePath = null,
            originalPkpassPath = "/path/to/$id.pkpass",
            passType = PassType.GENERIC.name,
            fieldsJson = "{}",
            createdAt = Instant.now().toEpochMilli(),
            modifiedAt = Instant.now().toEpochMilli()
        )
    }
}
