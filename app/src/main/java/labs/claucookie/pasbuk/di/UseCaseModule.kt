package labs.claucookie.pasbuk.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing use case instances.
 *
 * Use case provisions will be added in later phases:
 * - T046: Provide ImportPassUseCase, GetPassDetailUseCase, DeletePassUseCase
 * - T065: Provide GetTimelineUseCase
 * - T087: Provide CreateJourneyUseCase, GetAllJourneysUseCase, GetJourneyDetailUseCase, DeleteJourneyUseCase
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // Use case provisions will be added here in future tasks
}
