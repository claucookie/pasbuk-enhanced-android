package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when attempting to import a pass that already exists in the database.
 *
 * Duplicate detection is based on the pass serial number, which should be unique
 * across all passes.
 *
 * @property serialNumber The serial number of the duplicate pass
 * @param message Custom error message (defaults to a standard duplicate message)
 */
class DuplicatePassException(
    val serialNumber: String,
    message: String = "Pass with serial number '$serialNumber' already exists"
) : Exception(message)
