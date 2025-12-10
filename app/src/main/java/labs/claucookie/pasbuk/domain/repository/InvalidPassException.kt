package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when attempting to import an invalid or corrupted .pkpass file.
 *
 * @property message Description of why the pass is invalid
 * @property cause Original exception that caused this error (optional)
 */
class InvalidPassException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
