package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when there is insufficient storage space to import a pass.
 *
 * Before importing a .pkpass file, the app checks if there is at least 10MB
 * of free storage available. If not, this exception is thrown to prevent
 * potential corruption or incomplete imports.
 *
 * @property availableBytes The amount of free storage space in bytes
 * @property requiredBytes The minimum required storage space in bytes
 * @param message Custom error message with formatted byte sizes
 */
class LowStorageException(
    val availableBytes: Long,
    val requiredBytes: Long,
    message: String = "Insufficient storage: ${availableBytes / (1024 * 1024)}MB available, ${requiredBytes / (1024 * 1024)}MB required"
) : Exception(message)
