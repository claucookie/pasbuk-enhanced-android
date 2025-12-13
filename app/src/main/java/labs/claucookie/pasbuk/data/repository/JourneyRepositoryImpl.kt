package labs.claucookie.pasbuk.data.repository

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import labs.claucookie.pasbuk.data.local.dao.JourneyDao
import labs.claucookie.pasbuk.data.local.dao.PassDao
import labs.claucookie.pasbuk.data.local.entity.JourneyPassCrossRef
import labs.claucookie.pasbuk.data.mapper.createJourneyEntity
import labs.claucookie.pasbuk.data.mapper.toDomain
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.DuplicateJourneyNameException
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of JourneyRepository using Room database.
 */
@Singleton
class JourneyRepositoryImpl @Inject constructor(
    private val journeyDao: JourneyDao,
    private val passDao: PassDao,
    private val moshi: Moshi
) : JourneyRepository {

    override suspend fun createJourney(name: String, passIds: List<String>): Journey {
        // Check if journey with same name already exists
        val existing = journeyDao.getByName(name)
        if (existing != null) {
            throw DuplicateJourneyNameException(name)
        }

        // Verify all passes exist
        passIds.forEach { passId ->
            passDao.getById(passId) ?: throw IllegalArgumentException("Pass with ID $passId not found")
        }

        // Create journey entity
        val journeyEntity = createJourneyEntity(name)
        val journeyId = journeyDao.insert(journeyEntity)

        // Create cross-references
        val crossRefs = passIds.map { passId ->
            JourneyPassCrossRef(journeyId, passId)
        }
        journeyDao.insertJourneyPassCrossRefs(crossRefs)

        // Return the created journey with passes
        val journeyWithPasses = journeyDao.getJourneyWithPasses(journeyId)
            ?: throw IllegalStateException("Failed to retrieve created journey")

        return journeyWithPasses.toDomain(moshi)
    }

    override suspend fun getJourneyById(id: Long): Journey? {
        return journeyDao.getJourneyWithPasses(id)?.toDomain(moshi)
    }

    override fun getAllJourneys(): Flow<List<Journey>> {
        return journeyDao.getAllJourneysWithPasses().map { journeysWithPasses ->
            journeysWithPasses.map { it.toDomain(moshi) }
        }
    }

    override suspend fun deleteJourney(id: Long) {
        val journeyEntity = journeyDao.getById(id) ?: return

        // Delete cross-references first
        journeyDao.deleteAllPassesFromJourney(id)

        // Delete journey
        journeyDao.delete(journeyEntity)
    }
}
