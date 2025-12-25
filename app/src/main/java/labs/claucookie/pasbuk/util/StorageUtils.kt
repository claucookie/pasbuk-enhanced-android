package labs.claucookie.pasbuk.util

import android.content.Context
import android.os.StatFs
import labs.claucookie.pasbuk.domain.repository.LowStorageException
import java.io.File

/**
 * Utility for checking storage availability.
 */
object StorageUtils {
    /**
     * Minimum free storage required for importing a pass (10MB).
     * A typical .pkpass file is usually less than 5MB.
     */
    private const val MIN_FREE_STORAGE_BYTES = 10L * 1024 * 1024

    /**
     * Checks if sufficient storage is available for importing a pass.
     *
     * @param context Application context
     * @param requiredBytes Minimum bytes required (defaults to MIN_FREE_STORAGE_BYTES)
     * @throws LowStorageException if available storage is below the required threshold
     */
    fun checkStorageAvailability(
        context: Context,
        requiredBytes: Long = MIN_FREE_STORAGE_BYTES
    ) {
        val availableBytes = getAvailableStorageBytes(context)
        if (availableBytes < requiredBytes) {
            throw LowStorageException(
                availableBytes = availableBytes,
                requiredBytes = requiredBytes
            )
        }
    }

    /**
     * Gets the available storage in bytes for the app's data directory.
     *
     * @param context Application context
     * @return Available storage in bytes
     */
    fun getAvailableStorageBytes(context: Context): Long {
        val dataDir = context.filesDir
        val stat = StatFs(dataDir.path)
        return stat.availableBytes
    }

    /**
     * Formats bytes to a human-readable string.
     *
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "5.2 MB")
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes bytes"
        }
    }
}
