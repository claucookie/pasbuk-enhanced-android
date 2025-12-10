package labs.claucookie.pasbuk.domain.repository

class DuplicateJourneyNameException(
    val journeyName: String,
    message: String = "Journey with name '$journeyName' already exists"
) : Exception(message)
