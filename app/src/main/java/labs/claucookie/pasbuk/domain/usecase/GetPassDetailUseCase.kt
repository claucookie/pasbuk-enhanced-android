package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Use case for retrieving pass details by ID.
 */
class GetPassDetailUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * Retrieves the details of a pass by its ID.
     *
     * @param passId The unique identifier of the pass
     * @return The Pass if found, null otherwise
     */
    suspend operator fun invoke(passId: String): Pass? {
        return passRepository.getPassById(passId)
    }
}
