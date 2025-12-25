package labs.claucookie.pasbuk.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing optimized Coil ImageLoader for efficient image loading.
 *
 * Configures:
 * - Memory cache: 25% of available memory
 * - Disk cache: 50MB for pass images (logos, icons, backgrounds)
 * - Cache policies for optimal performance with offline support
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageLoadingModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // Memory cache configuration
            .memoryCache {
                MemoryCache.Builder(context)
                    // Use 25% of the app's available memory for caching images
                    .maxSizePercent(0.25)
                    .build()
            }
            // Disk cache configuration
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    // 50MB cache size for pass images
                    .maxSizeBytes(50L * 1024 * 1024) // 50MB
                    .build()
            }
            // Cache policies
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Ignore HTTP cache headers for local files
            .build()
    }
}
