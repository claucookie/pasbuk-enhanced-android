package labs.claucookie.pasbuk.domain.model

import java.time.Instant

/**
 * AI-generated activity suggestion for a gap in journey timeline.
 *
 * @property id Unique identifier for this suggestion
 * @property positionIndex Gap index where suggestion appears (0 = before first pass, 1 = between pass 0 and 1, etc.)
 * @property title Action-oriented title (e.g., "Book restaurant near venue")
 * @property description Detailed suggestion text
 * @property reasoning Optional explanation of why this suggestion was made
 * @property isDismissed User dismissed this suggestion
 * @property generatedAt Timestamp when AI generated this suggestion
 */
data class ActivitySuggestion(
    val id: String,
    val positionIndex: Int,
    val title: String,
    val description: String,
    val reasoning: String? = null,
    val isDismissed: Boolean = false,
    val generatedAt: Instant
)
