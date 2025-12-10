package labs.claucookie.pasbuk.domain.repository

class InvalidPassException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
