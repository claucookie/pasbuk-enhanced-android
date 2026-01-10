package labs.claucookie.pasbuk.domain.usecase

import android.util.Log
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import labs.claucookie.pasbuk.domain.service.GeminiSuggestionService
import javax.inject.Inject

/**
 * Use case for generating AI-powered activity suggestions for a journey.
 *
 * This use case:
 * 1. Fetches the journey with passes
 * 2. Calls Gemini API to generate 2-3 suggestions
 * 3. Updates the journey with suggestions in database
 *
 * NOTE: This is an async operation and may fail due to network issues.
 * Failures are logged but don't block journey creation/update.
 */
class GenerateJourneySuggestionsUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val geminiService: GeminiSuggestionService
) {
    /**
     * Generates and saves suggestions for a journey.
     *
     * @param journeyId ID of the journey to generate suggestions for
     * @return Result indicating success/failure
     */
    suspend operator fun invoke(journeyId: Long): Result<Unit> {
        return try {
            Log.d(TAG, "Starting suggestion generation for journey ID: $journeyId")
            // Fetch journey
            val journey = journeyRepository.getJourneyById(journeyId)
                ?: return Result.failure(IllegalArgumentException("Journey not found"))

            // Skip if journey has fewer than 2 passes
            if (journey.passes.size < 2) {
                return Result.success(Unit)
            }

            // Generate suggestions via Gemini
            val suggestionsResult = geminiService.generateSuggestions(journey)

            suggestionsResult.fold(
                onSuccess = { suggestions ->
                    // Update journey with suggestions
                    journeyRepository.updateSuggestions(journeyId, suggestions)
                    Result.success(Unit)
                },
                onFailure = { error ->
                    // Return failure but don't crash the app
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
