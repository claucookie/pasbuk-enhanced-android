package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when a .pkpass file is corrupted, malformed, or cannot be parsed.
 *
 * This can occur when:
 * - The file is not a valid ZIP archive
 * - Required pass.json is missing or corrupted
 * - JSON parsing fails due to invalid format
 * - Required fields are missing from the pass data
 *
 * @param message Description of what made the pass invalid
 * @param cause The underlying exception that caused the parse failure, if any
 */
class InvalidPassException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
