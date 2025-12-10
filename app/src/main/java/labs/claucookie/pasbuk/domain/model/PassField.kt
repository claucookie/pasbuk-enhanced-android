package labs.claucookie.pasbuk.domain.model

/**
 * A field displayed on a pass (header, primary, secondary, auxiliary, or back fields).
 *
 * @property key Unique identifier for the field
 * @property label Field label (optional)
 * @property value Field value (can be string, number, or date)
 * @property textAlignment Text alignment for the value
 */
data class PassField(
    val key: String,
    val label: String?,
    val value: String,
    val textAlignment: TextAlignment?
)
