package labs.claucookie.pasbuk.domain.repository

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Journey

interface JourneyRepository {
    suspend fun createJourney(name: String, passIds: List<String>): Journey
    suspend fun getJourneyById(id: Long): Journey?
    fun getAllJourneys(): Flow<List<Journey>>
    suspend fun deleteJourney(id: Long)
}
