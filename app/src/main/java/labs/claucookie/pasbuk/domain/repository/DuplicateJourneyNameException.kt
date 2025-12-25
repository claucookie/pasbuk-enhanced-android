package labs.claucookie.pasbuk.domain.repository

/**
 * Exception thrown when attempting to create a journey with a name that already exists.
 *
 * Journey names must be unique to allow users to easily distinguish between
 * different trips or collections.
 *
 * @property journeyName The name of the journey that already exists
 * @param message Custom error message (defaults to a standard duplicate message)
 */
class DuplicateJourneyNameException(
    val journeyName: String,
    message: String = "Journey with name '$journeyName' already exists"
) : Exception(message)
