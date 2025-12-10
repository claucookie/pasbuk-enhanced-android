package labs.claucookie.pasbuk.domain.repository

class DuplicatePassException(
    val serialNumber: String,
    message: String = "Pass with serial number '$serialNumber' already exists"
) : Exception(message)
