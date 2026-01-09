package labs.claucookie.pasbuk.domain.service

import labs.claucookie.pasbuk.domain.model.ActivitySuggestion
import labs.claucookie.pasbuk.domain.model.Journey

/**
 * Service interface for AI-powered journey suggestions using Gemini.
 */
interface GeminiSuggestionService {
    /**
     * Generates 2-3 intelligent activity suggestions for gaps in the journey.
     *
     * Uses Gemini AI to analyze the journey and identify the most important
     * gaps where the user might need to take action (e.g., meals, lodging, transportation).
     *
     * @param journey Journey to analyze
     * @return Result containing list of suggested activities (2-3 items, prioritized by AI)
     *         or an exception if generation fails
     */
    suspend fun generateSuggestions(journey: Journey): Result<List<ActivitySuggestion>>
}

/**
 * Exception thrown when Gemini API call fails.
 */
class GeminiApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when Gemini API rate limit is exceeded.
 */
class GeminiRateLimitException(message: String) : Exception(message)
