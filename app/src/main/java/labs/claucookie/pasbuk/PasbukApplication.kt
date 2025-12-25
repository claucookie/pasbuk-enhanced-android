package labs.claucookie.pasbuk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import labs.claucookie.pasbuk.util.GlobalExceptionHandler

/**
 * Application class for Pasbuk Enhanced.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class PasbukApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize global exception handler
        GlobalExceptionHandler.initialize(this)
    }
}
