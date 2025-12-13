package labs.claucookie.pasbuk.domain.usecase

import kotlinx.coroutines.flow.Flow
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Use case for retrieving all passes sorted by relevant date.
 *
 * Returns a Flow that emits the list of passes whenever the data changes,
 * sorted by relevantDate in descending order (most recent first).
 * Passes without a relevantDate appear at the end.
 */
class GetTimelineUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * Retrieves all passes sorted chronologically.
     *
     * @return Flow emitting list of passes sorted by date (most recent first)
     */
    operator fun invoke(): Flow<List<Pass>> {
        return passRepository.getAllPassesSortedByDate()
    }
}
