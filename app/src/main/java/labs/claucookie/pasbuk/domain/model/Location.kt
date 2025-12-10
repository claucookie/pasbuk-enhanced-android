package labs.claucookie.pasbuk.domain.model

/**
 * Geographic location associated with a pass.
 *
 * @property latitude Latitude in degrees
 * @property longitude Longitude in degrees
 * @property altitude Altitude in meters (optional)
 * @property relevantText Text to display when near this location
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val relevantText: String?
)
