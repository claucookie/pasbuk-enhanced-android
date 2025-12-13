package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Use case for deleting a journey.
 *
 * Note: Deleting a journey does not delete the passes themselves,
 * only removes the journey and its associations.
 */
class DeleteJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * Deletes a journey by ID.
     *
     * @param journeyId The ID of the journey to delete
     */
    suspend operator fun invoke(journeyId: Long) {
        journeyRepository.deleteJourney(journeyId)
    }
}
