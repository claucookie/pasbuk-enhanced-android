package labs.claucookie.pasbuk.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for providing repository implementations.
 *
 * Repository bindings will be added in later phases:
 * - T042: Bind PassRepository to PassRepositoryImpl
 * - T082: Bind JourneyRepository to JourneyRepositoryImpl
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // Repository bindings will be added here in future tasks
}
