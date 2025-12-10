package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when attempting to create a journey with a name that already exists.
 *
 * @property journeyName The duplicate journey name
 * @property message Description of the duplicate error
 */
class DuplicateJourneyNameException(
    val journeyName: String,
    message: String = "Journey with name '$journeyName' already exists"
) : Exception(message)
