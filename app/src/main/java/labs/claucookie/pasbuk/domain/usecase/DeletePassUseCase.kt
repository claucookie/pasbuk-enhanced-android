package labs.claucookie.pasbuk.domain.usecase

import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Use case for deleting a pass.
 *
 * Handles the business logic of removing a pass from storage,
 * including cleaning up associated files.
 */
class DeletePassUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * Deletes a pass by its ID.
     *
     * @param passId The unique identifier of the pass to delete
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(passId: String): Result<Unit> {
        return try {
            passRepository.deletePass(passId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
