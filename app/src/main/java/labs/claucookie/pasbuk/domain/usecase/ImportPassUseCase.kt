package labs.claucookie.pasbuk.domain.usecase

import android.net.Uri
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Use case for importing a .pkpass file.
 *
 * Handles the business logic of importing a pass from a file URI,
 * including validation and duplicate detection.
 */
class ImportPassUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    /**
     * Imports a .pkpass file from the given URI.
     *
     * @param uri URI to the .pkpass file (typically from a file picker)
     * @return Result containing the imported Pass on success, or an exception on failure
     */
    suspend operator fun invoke(uri: Uri): Result<Pass> {
        return try {
            val pass = passRepository.importPass(uri)
            Result.success(pass)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
