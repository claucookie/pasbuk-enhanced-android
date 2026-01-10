package labs.claucookie.pasbuk.data.local.converter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

/**
 * Moshi adapter for converting java.time.Instant to/from Long (epoch milliseconds).
 *
 * Used for JSON serialization of suggestions containing Instant fields.
 */
class InstantAdapter {
    @ToJson
    fun toJson(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @FromJson
    fun fromJson(epochMillis: Long): Instant {
        return Instant.ofEpochMilli(epochMillis)
    }
}
