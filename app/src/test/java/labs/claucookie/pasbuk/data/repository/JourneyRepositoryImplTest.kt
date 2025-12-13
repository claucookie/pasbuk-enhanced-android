package labs.claucookie.pasbuk.data.repository

import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import labs.claucookie.pasbuk.data.local.dao.JourneyDao
import labs.claucookie.pasbuk.data.local.dao.PassDao
import labs.claucookie.pasbuk.data.local.entity.JourneyEntity
import labs.claucookie.pasbuk.data.local.entity.JourneyPassCrossRef
import labs.claucookie.pasbuk.data.local.entity.JourneyWithPasses
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.DuplicateJourneyNameException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for JourneyRepositoryImpl (T076).
 *
 * Tests verify that:
 * - Journey creation works correctly
 * - Duplicate journey names throw exception
 * - Getting journey by ID works
 * - Getting all journeys works
 * - Deleting journey works
 * - Cross-references are created correctly
 */
class JourneyRepositoryImplTest {

    private lateinit var journeyDao: JourneyDao
    private lateinit var passDao: PassDao
    private lateinit var moshi: Moshi
    private lateinit var repository: JourneyRepositoryImpl

    @Before
    fun setup() {
        journeyDao = mockk(relaxed = true)
        passDao = mockk(relaxed = true)
        moshi = Moshi.Builder().build()
        repository = JourneyRepositoryImpl(journeyDao, passDao, moshi)
    }

    @Test
    fun `createJourney successfully creates journey and cross references`() = runTest {
        // Given
        val name = "Summer Trip"
        val passIds = listOf("pass-1", "pass-2", "pass-3")
        val journeyId = 1L
        val passes = passIds.map { createTestPassEntity(it) }

        coEvery { journeyDao.getByName(name) } returns null
        coEvery { journeyDao.insert(any()) } returns journeyId
        passes.forEach { pass ->
            coEvery { passDao.getById(pass.id) } returns pass
        }
        coEvery { journeyDao.getJourneyWithPasses(journeyId) } returns JourneyWithPasses(
            journey = JourneyEntity(journeyId, name, Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
            passes = passes
        )

        // When
        val result = repository.createJourney(name, passIds)

        // Then
        assertNotNull(result)
        assertEquals(name, result.name)
        assertEquals(3, result.passCount)
        coVerify { journeyDao.getByName(name) }
        coVerify { journeyDao.insert(any()) }
        coVerify { journeyDao.insertJourneyPassCrossRefs(any()) }
    }

    @Test(expected = DuplicateJourneyNameException::class)
    fun `createJourney throws exception when name already exists`() = runTest {
        // Given
        val name = "Existing Journey"
        val passIds = listOf("pass-1")
        val existingJourney = JourneyEntity(1L, name, Instant.now().toEpochMilli(), Instant.now().toEpochMilli())

        coEvery { journeyDao.getByName(name) } returns existingJourney

        // When
        repository.createJourney(name, passIds)

        // Then: Exception thrown
    }

    @Test
    fun `createJourney creates correct cross references`() = runTest {
        // Given
        val name = "Test Journey"
        val passIds = listOf("pass-1", "pass-2")
        val journeyId = 1L
        val passes = passIds.map { createTestPassEntity(it) }

        coEvery { journeyDao.getByName(name) } returns null
        coEvery { journeyDao.insert(any()) } returns journeyId
        passes.forEach { pass ->
            coEvery { passDao.getById(pass.id) } returns pass
        }
        coEvery { journeyDao.getJourneyWithPasses(journeyId) } returns JourneyWithPasses(
            journey = JourneyEntity(journeyId, name, Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
            passes = passes
        )

        // When
        repository.createJourney(name, passIds)

        // Then
        coVerify {
            journeyDao.insertJourneyPassCrossRefs(
                match { crossRefs ->
                    crossRefs.size == 2 &&
                    crossRefs.all { it.journeyId == journeyId } &&
                    crossRefs.map { it.passId }.containsAll(passIds)
                }
            )
        }
    }

    @Test
    fun `getJourneyById returns journey when found`() = runTest {
        // Given
        val journeyId = 1L
        val name = "Test Journey"
        val passes = listOf(createTestPassEntity("pass-1"), createTestPassEntity("pass-2"))
        val journeyWithPasses = JourneyWithPasses(
            journey = JourneyEntity(journeyId, name, Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
            passes = passes
        )

        coEvery { journeyDao.getJourneyWithPasses(journeyId) } returns journeyWithPasses

        // When
        val result = repository.getJourneyById(journeyId)

        // Then
        assertNotNull(result)
        assertEquals(name, result?.name)
        assertEquals(2, result?.passCount)
        coVerify { journeyDao.getJourneyWithPasses(journeyId) }
    }

    @Test
    fun `getJourneyById returns null when not found`() = runTest {
        // Given
        val journeyId = 999L
        coEvery { journeyDao.getJourneyWithPasses(journeyId) } returns null

        // When
        val result = repository.getJourneyById(journeyId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getAllJourneys returns flow of journeys`() = runTest {
        // Given
        val journeysWithPasses = listOf(
            JourneyWithPasses(
                journey = JourneyEntity(1L, "Journey 1", Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
                passes = listOf(createTestPassEntity("pass-1"))
            ),
            JourneyWithPasses(
                journey = JourneyEntity(2L, "Journey 2", Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
                passes = listOf(createTestPassEntity("pass-2"), createTestPassEntity("pass-3"))
            )
        )

        every { journeyDao.getAllJourneysWithPasses() } returns flowOf(journeysWithPasses)

        // When
        val result = repository.getAllJourneys().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Journey 1", result[0].name)
        assertEquals(1, result[0].passCount)
        assertEquals("Journey 2", result[1].name)
        assertEquals(2, result[1].passCount)
    }

    @Test
    fun `getAllJourneys returns empty list when no journeys exist`() = runTest {
        // Given
        every { journeyDao.getAllJourneysWithPasses() } returns flowOf(emptyList())

        // When
        val result = repository.getAllJourneys().first()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteJourney deletes journey and cross references`() = runTest {
        // Given
        val journeyId = 1L
        val journeyEntity = JourneyEntity(journeyId, "Test", Instant.now().toEpochMilli(), Instant.now().toEpochMilli())

        coEvery { journeyDao.getById(journeyId) } returns journeyEntity

        // When
        repository.deleteJourney(journeyId)

        // Then
        coVerify { journeyDao.deleteAllPassesFromJourney(journeyId) }
        coVerify { journeyDao.delete(journeyEntity) }
    }

    @Test
    fun `getAllJourneys returns journeys with sorted passes`() = runTest {
        // Given
        val now = Instant.now()
        val oldPass = createTestPassEntity("pass-1", now.minusSeconds(60 * 60 * 24))
        val newPass = createTestPassEntity("pass-2", now.minusSeconds(60 * 60))

        val journeyWithPasses = JourneyWithPasses(
            journey = JourneyEntity(1L, "Sorted Journey", Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
            passes = listOf(newPass, oldPass) // Unsorted in DB
        )

        every { journeyDao.getAllJourneysWithPasses() } returns flowOf(listOf(journeyWithPasses))

        // When
        val result = repository.getAllJourneys().first()

        // Then
        assertEquals(1, result.size)
        val journey = result[0]
        assertEquals(2, journey.passCount)
        // Passes should be sorted by relevantDate (ascending)
        assertEquals("pass-1", journey.passes[0].id) // Oldest first
        assertEquals("pass-2", journey.passes[1].id)
    }

    private fun createTestPassEntity(
        id: String,
        relevantDate: Instant = Instant.now()
    ): PassEntity {
        return PassEntity(
            id = id,
            serialNumber = "SERIAL-$id",
            passTypeIdentifier = "pass.com.example",
            organizationName = "Test Organization",
            description = "Test Pass $id",
            teamIdentifier = "TEAM123",
            relevantDate = relevantDate.toEpochMilli(),
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
