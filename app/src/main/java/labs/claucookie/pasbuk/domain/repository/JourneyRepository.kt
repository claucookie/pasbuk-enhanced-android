package labs.claucookie.pasbuk.domain.repository

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Journey

/**
 * Repository interface for managing Journey entities.
 * Provides methods for creating, retrieving, and deleting journeys.
 */
interface JourneyRepository {
    /**
     * Create a new journey with the given name and pass IDs.
     *
     * @param name The journey name
     * @param passIds List of pass IDs to include in the journey
     * @return The created Journey domain model
     * @throws DuplicateJourneyNameException if a journey with this name already exists
     */
    suspend fun createJourney(name: String, passIds: List<String>): Journey

    /**
     * Get a journey by its unique identifier, including all associated passes.
     *
     * @param id The journey identifier
     * @return The Journey with passes if found, null otherwise
     */
    suspend fun getJourneyById(id: Long): Journey?

    /**
     * Get all journeys with their associated passes.
     *
     * @return Flow emitting the list of all journeys
     */
    fun getAllJourneys(): Flow<List<Journey>>

    /**
     * Delete a journey by its unique identifier.
     * This only deletes the journey, not the passes within it.
     *
     * @param id The journey identifier to delete
     */
    suspend fun deleteJourney(id: Long)
}
