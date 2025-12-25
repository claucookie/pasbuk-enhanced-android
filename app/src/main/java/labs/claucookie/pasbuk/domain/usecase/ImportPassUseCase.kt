package labs.claucookie.pasbuk.domain.usecase

import android.net.Uri
import kotlinx.coroutines.delay
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.repository.DuplicatePassException
import labs.claucookie.pasbuk.domain.repository.InvalidPassException
import labs.claucookie.pasbuk.domain.repository.LowStorageException
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Inject

/**
 * Use case for importing a .pkpass file.
 *
 * Handles the business logic of importing a pass from a file URI,
 * including validation, duplicate detection, and retry logic with exponential backoff.
 */
class ImportPassUseCase @Inject constructor(
    private val passRepository: PassRepository
) {
    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY_MS = 500L
        private const val RETRY_DELAY_MULTIPLIER = 2
    }

    /**
     * Imports a .pkpass file from the given URI with retry logic.
     *
     * Retries are applied with exponential backoff for transient errors.
     * Non-retryable errors (duplicates, invalid files) fail immediately.
     *
     * @param uri URI to the .pkpass file (typically from a file picker)
     * @param onRetry Callback invoked before each retry attempt with the attempt number
     * @return Result containing the imported Pass on success, or an exception on failure
     */
    suspend operator fun invoke(
        uri: Uri,
        onRetry: (suspend (attempt: Int) -> Unit)? = null
    ): Result<Pass> {
        var lastException: Exception? = null
        var retryDelay = INITIAL_RETRY_DELAY_MS

        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                val pass = passRepository.importPass(uri)
                return Result.success(pass)
            } catch (e: Exception) {
                lastException = e

                // Don't retry for non-transient errors
                if (!isRetryableError(e)) {
                    return Result.failure(e)
                }

                // If not the last attempt, wait and retry
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    onRetry?.invoke(attempt + 2) // +2 because we're about to start the next attempt
                    delay(retryDelay)
                    retryDelay *= RETRY_DELAY_MULTIPLIER
                }
            }
        }

        // All retries exhausted
        return Result.failure(
            lastException ?: Exception("Failed to import pass after $MAX_RETRY_ATTEMPTS attempts")
        )
    }

    /**
     * Determines if an error is retryable.
     *
     * Non-retryable errors:
     * - DuplicatePassException (pass already exists)
     * - InvalidPassException (file is corrupted or not a valid .pkpass)
     * - LowStorageException (insufficient storage space)
     *
     * Retryable errors:
     * - IO exceptions (file system issues)
     * - Network errors (if downloading from network)
     * - Other transient errors
     */
    private fun isRetryableError(exception: Exception): Boolean {
        return when (exception) {
            is DuplicatePassException,
            is InvalidPassException,
            is LowStorageException -> false
            else -> true
        }
    }
}
