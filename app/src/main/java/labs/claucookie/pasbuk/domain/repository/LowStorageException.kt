package labs.claucookie.pasbuk.domain.repository

class LowStorageException(
    val availableBytes: Long,
    val requiredBytes: Long,
    message: String = "Insufficient storage: ${availableBytes / (1024 * 1024)}MB available, ${requiredBytes / (1024 * 1024)}MB required"
) : Exception(message)
