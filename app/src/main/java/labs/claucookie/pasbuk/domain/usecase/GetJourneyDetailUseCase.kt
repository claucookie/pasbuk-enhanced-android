package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Use case for retrieving a journey by its ID with all passes.
 *
 * Returns the journey with passes sorted by relevantDate in ascending order
 * (oldest first).
 */
class GetJourneyDetailUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * Retrieves a journey by ID.
     *
     * @param journeyId The ID of the journey to retrieve
     * @return The journey with passes, or null if not found
     */
    suspend operator fun invoke(journeyId: Long): Journey? {
        return journeyRepository.getJourneyById(journeyId)
    }
}
