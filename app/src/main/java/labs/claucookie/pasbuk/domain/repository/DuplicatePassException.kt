package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when attempting to import a pass that already exists.
 * Duplicates are detected by serial number.
 *
 * @property serialNumber The serial number of the duplicate pass
 * @property message Description of the duplicate error
 */
class DuplicatePassException(
    val serialNumber: String,
    message: String = "Pass with serial number '$serialNumber' already exists"
) : Exception(message)
