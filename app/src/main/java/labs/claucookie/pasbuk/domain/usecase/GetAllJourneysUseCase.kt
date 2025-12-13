package labs.claucookie.pasbuk.domain.usecase

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Use case for retrieving all journeys.
 *
 * Returns a Flow that emits the list of journeys whenever the data changes,
 * sorted by creation date in descending order (most recent first).
 */
class GetAllJourneysUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository
) {
    /**
     * Retrieves all journeys.
     *
     * @return Flow emitting list of journeys sorted by date (most recent first)
     */
    operator fun invoke(): Flow<List<Journey>> {
        return journeyRepository.getAllJourneys()
    }
}
