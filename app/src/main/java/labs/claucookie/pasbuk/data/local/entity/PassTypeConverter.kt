package labs.claucookie.pasbuk.data.local.entity

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.Location
import labs.claucookie.pasbuk.domain.model.PassField

class PassTypeConverter {
    private val moshi = Moshi.Builder().build()

    @TypeConverter
    fun fromLocationsJson(value: String?): List<Location>? {
        if (value == null) return null
        val type = Types.newParameterizedType(List::class.java, Location::class.java)
        val adapter = moshi.adapter<List<Location>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toLocationsJson(locations: List<Location>?): String? {
        if (locations == null) return null
        val type = Types.newParameterizedType(List::class.java, Location::class.java)
        val adapter = moshi.adapter<List<Location>>(type)
        return adapter.toJson(locations)
    }

    @TypeConverter
    fun fromBarcodeJson(value: String?): Barcode? {
        if (value == null) return null
        val adapter = moshi.adapter(Barcode::class.java)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toBarcodeJson(barcode: Barcode?): String? {
        if (barcode == null) return null
        val adapter = moshi.adapter(Barcode::class.java)
        return adapter.toJson(barcode)
    }

    @TypeConverter
    fun fromFieldsJson(value: String?): Map<String, PassField>? {
        if (value == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            PassField::class.java
        )
        val adapter = moshi.adapter<Map<String, PassField>>(type)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun toFieldsJson(fields: Map<String, PassField>?): String? {
        if (fields == null) return null
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            PassField::class.java
        )
        val adapter = moshi.adapter<Map<String, PassField>>(type)
        return adapter.toJson(fields)
    }
}
