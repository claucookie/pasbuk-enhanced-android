package labs.claucookie.pasbuk.domain.model

import java.time.Instant

/**
 * Domain model representing a user-created collection of passes.
 *
 * @property id Auto-generated unique identifier
 * @property name User-provided journey name
 * @property passes List of passes in this journey (sorted by relevantDate)
 * @property createdAt Timestamp when journey was created
 * @property modifiedAt Timestamp when journey was last modified
 */
data class Journey(
    val id: Long,
    val name: String,
    val passes: List<Pass>,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    /**
     * Number of passes in this journey.
     */
    val passCount: Int get() = passes.size

    /**
     * Date range spanning from earliest to latest pass relevant date.
     * Returns null if no passes have relevant dates.
     */
    val dateRange: ClosedRange<Instant>? get() {
        val dates = passes.mapNotNull { it.relevantDate }.sorted()
        return if (dates.isEmpty()) null else dates.first()..dates.last()
    }
}
