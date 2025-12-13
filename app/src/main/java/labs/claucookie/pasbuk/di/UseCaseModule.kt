package labs.claucookie.pasbuk.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing use case instances.
 *
 * Note: Use cases are currently injected via @Inject constructor and don't require
 * explicit @Provides methods. This module exists as a placeholder for future
 * use case provisions that may require custom instantiation logic.
 *
 * Implemented use cases (via @Inject):
 * - T046: ImportPassUseCase, GetPassDetailUseCase, DeletePassUseCase
 * - T065: GetTimelineUseCase
 * - T087: CreateJourneyUseCase, GetAllJourneysUseCase, GetJourneyDetailUseCase, DeleteJourneyUseCase
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Use cases are injected via @Inject constructor
    // Custom @Provides methods will be added here if needed
}
