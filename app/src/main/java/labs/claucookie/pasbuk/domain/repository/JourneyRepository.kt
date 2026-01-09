package labs.claucookie.pasbuk.domain.repository

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.ActivitySuggestion
import labs.claucookie.pasbuk.domain.model.Journey

/**
 * Repository interface for managing Journey data operations.
 *
 * Provides methods for creating, retrieving, and deleting journeys,
 * which are named collections of passes organized chronologically.
 */
interface JourneyRepository {
    /**
     * Creates a new journey with the given name and passes.
     *
     * Associates the specified passes with the journey and sorts them
     * chronologically by their relevant date.
     *
     * @param name The name of the journey (must be unique)
     * @param passIds List of pass IDs to include in the journey
     * @return The created Journey object with associated passes
     * @throws DuplicateJourneyNameException if a journey with the same name already exists
     * @throws IllegalArgumentException if the name is blank or pass list is empty
     */
    suspend fun createJourney(name: String, passIds: List<String>): Journey

    /**
     * Retrieves a journey by its unique identifier, including all associated passes.
     *
     * @param id The unique identifier of the journey
     * @return The Journey object with passes if found, null otherwise
     */
    suspend fun getJourneyById(id: Long): Journey?

    /**
     * Returns a Flow of all journeys with their associated passes.
     *
     * Journeys are ordered by creation date (most recent first).
     * Passes within each journey are sorted chronologically.
     * The Flow emits a new list whenever the underlying data changes.
     *
     * @return Flow emitting lists of journeys
     */
    fun getAllJourneys(): Flow<List<Journey>>

    /**
     * Deletes a journey and its pass associations.
     *
     * Note: This does not delete the passes themselves, only the journey
     * and its relationships. Passes remain in the timeline.
     *
     * @param id The unique identifier of the journey to delete
     */
    suspend fun deleteJourney(id: Long)

    /**
     * Updates suggestions for an existing journey.
     *
     * Replaces the existing suggestions with the new list.
     *
     * @param journeyId The ID of the journey to update
     * @param suggestions New list of suggestions (replaces existing)
     * @throws IllegalArgumentException if journey not found
     */
    suspend fun updateSuggestions(journeyId: Long, suggestions: List<ActivitySuggestion>)
}
