package labs.claucookie.pasbuk.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import labs.claucookie.pasbuk.data.service.GeminiSuggestionServiceImpl
import labs.claucookie.pasbuk.domain.service.GeminiSuggestionService
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module for domain layer dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindGeminiSuggestionService(
        impl: GeminiSuggestionServiceImpl
    ): GeminiSuggestionService
}

/**
 * Module for providing coroutine scopes.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

/**
 * Qualifier annotation for application-scoped coroutine scope.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
