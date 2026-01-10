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

            Log.d(TAG, "Journey fetched: ${journey.name} with ${journey.passes.size} passes")

            // Skip if journey has fewer than 2 passes
            if (journey.passes.size < 2) {
                Log.d(TAG, "Skipping suggestion generation - journey has < 2 passes")
                return Result.success(Unit)
            }

            // Generate suggestions via Gemini
            Log.d(TAG, "Calling Gemini API for suggestions...")
            val suggestionsResult = geminiService.generateSuggestions(journey)

            suggestionsResult.fold(
                onSuccess = { suggestions ->
                    Log.d(TAG, "Generated ${suggestions.size} suggestions")
                    // Update journey with suggestions
                    journeyRepository.updateSuggestions(journeyId, suggestions)
                    Log.d(TAG, "Suggestions saved to database")
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to generate suggestions", error)
                    // Return failure but don't crash the app
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during suggestion generation", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GenerateJourneySuggestions"
    }
}
