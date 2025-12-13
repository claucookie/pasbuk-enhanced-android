package labs.claucookie.pasbuk.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import labs.claucookie.pasbuk.data.repository.JourneyRepositoryImpl
import labs.claucookie.pasbuk.data.repository.PassRepositoryImpl
import labs.claucookie.pasbuk.domain.repository.JourneyRepository
import labs.claucookie.pasbuk.domain.repository.PassRepository
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 *
 * Repository bindings:
 * - T042: Bind PassRepository to PassRepositoryImpl
 * - T082: Bind JourneyRepository to JourneyRepositoryImpl
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPassRepository(impl: PassRepositoryImpl): PassRepository

    @Binds
    @Singleton
    abstract fun bindJourneyRepository(impl: JourneyRepositoryImpl): JourneyRepository
}
