package labs.claucookie.pasbuk.domain.usecase

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import labs.claucookie.pasbuk.di.ApplicationScope
import labs.claucookie.pasbuk.domain.model.Journey
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import javax.inject.Inject

/**
 * Use case for creating a new journey from selected passes.
 *
 * Validates that:
 * - Journey name is not empty
 * - At least one pass is provided
 *
 * After creating the journey, triggers async AI suggestion generation.
 *
 * @throws DuplicateJourneyNameException if journey name already exists
 * @throws IllegalArgumentException if validation fails
 */
class CreateJourneyUseCase @Inject constructor(
    private val journeyRepository: JourneyRepository,
    private val generateSuggestionsUseCase: GenerateJourneySuggestionsUseCase,
    @ApplicationScope private val coroutineScope: CoroutineScope
) {
    /**
     * Creates a new journey with the given name and passes.
     *
     * @param name Journey name (will be trimmed)
     * @param passIds List of pass IDs to include in the journey
     * @return Result containing the created Journey or an exception
     */
    suspend operator fun invoke(name: String, passIds: List<String>): Result<Journey> {
        return try {
            // Validate name
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) {
                return Result.failure(IllegalArgumentException("Journey name cannot be empty"))
            }

            // Validate pass list
            if (passIds.isEmpty()) {
                return Result.failure(IllegalArgumentException("Journey must contain at least one pass"))
            }

            // Create journey
            val journey = journeyRepository.createJourney(trimmedName, passIds)

            // Trigger async suggestion generation (fire-and-forget)
            coroutineScope.launch {
                generateSuggestionsUseCase(journey.id)
                    .onFailure { error ->
                        // Log error but don't fail journey creation
                        Log.w("CreateJourneyUseCase", "Failed to generate suggestions", error)
                    }
            }

            Result.success(journey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
