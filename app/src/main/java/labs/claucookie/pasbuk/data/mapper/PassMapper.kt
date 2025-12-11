package labs.claucookie.pasbuk.data.mapper

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import labs.claucookie.pasbuk.data.local.entity.PassEntity
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.Location
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassField
import labs.claucookie.pasbuk.domain.model.PassType
import java.time.Instant

/**
 * Extension function to convert PassEntity to Pass domain model.
 */
fun PassEntity.toDomain(moshi: Moshi): Pass {
    return Pass(
        id = id,
        serialNumber = serialNumber,
        passTypeIdentifier = passTypeIdentifier,
        organizationName = organizationName,
        description = description,
        teamIdentifier = teamIdentifier,
        relevantDate = relevantDate?.let { Instant.ofEpochMilli(it) },
        expirationDate = expirationDate?.let { Instant.ofEpochMilli(it) },
        locations = parseLocations(locationsJson, moshi),
        logoText = logoText,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        labelColor = labelColor,
        barcode = parseBarcode(barcodeJson, moshi),
        logoImagePath = logoImagePath,
        iconImagePath = iconImagePath,
        thumbnailImagePath = thumbnailImagePath,
        stripImagePath = stripImagePath,
        backgroundImagePath = backgroundImagePath,
        originalPkpassPath = originalPkpassPath,
        passType = PassType.valueOf(passType),
        fields = parseFields(fieldsJson, moshi),
        createdAt = Instant.ofEpochMilli(createdAt),
        modifiedAt = Instant.ofEpochMilli(modifiedAt)
    )
}

/**
 * Extension function to convert Pass domain model to PassEntity.
 */
fun Pass.toEntity(moshi: Moshi): PassEntity {
    return PassEntity(
        id = id,
        serialNumber = serialNumber,
        passTypeIdentifier = passTypeIdentifier,
        organizationName = organizationName,
        description = description,
        teamIdentifier = teamIdentifier,
        relevantDate = relevantDate?.toEpochMilli(),
        expirationDate = expirationDate?.toEpochMilli(),
        locationsJson = serializeLocations(locations, moshi),
        barcodeJson = serializeBarcode(barcode, moshi),
        fieldsJson = serializeFields(fields, moshi),
        logoText = logoText,
        backgroundColor = backgroundColor,
        foregroundColor = foregroundColor,
        labelColor = labelColor,
        logoImagePath = logoImagePath,
        iconImagePath = iconImagePath,
        thumbnailImagePath = thumbnailImagePath,
        stripImagePath = stripImagePath,
        backgroundImagePath = backgroundImagePath,
        originalPkpassPath = originalPkpassPath,
        passType = passType.name,
        createdAt = createdAt.toEpochMilli(),
        modifiedAt = modifiedAt.toEpochMilli()
    )
}

private fun parseLocations(json: String?, moshi: Moshi): List<Location> {
    if (json.isNullOrBlank()) return emptyList()
    val type = Types.newParameterizedType(List::class.java, Location::class.java)
    val adapter = moshi.adapter<List<Location>>(type)
    return adapter.fromJson(json) ?: emptyList()
}

private fun parseBarcode(json: String?, moshi: Moshi): Barcode? {
    if (json.isNullOrBlank()) return null
    val adapter = moshi.adapter(Barcode::class.java)
    return adapter.fromJson(json)
}

private fun parseFields(json: String?, moshi: Moshi): Map<String, PassField> {
    if (json.isNullOrBlank()) return emptyMap()
    val type = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        PassField::class.java
    )
    val adapter = moshi.adapter<Map<String, PassField>>(type)
    return adapter.fromJson(json) ?: emptyMap()
}

private fun serializeLocations(locations: List<Location>, moshi: Moshi): String? {
    if (locations.isEmpty()) return null
    val type = Types.newParameterizedType(List::class.java, Location::class.java)
    val adapter = moshi.adapter<List<Location>>(type)
    return adapter.toJson(locations)
}

private fun serializeBarcode(barcode: Barcode?, moshi: Moshi): String? {
    if (barcode == null) return null
    val adapter = moshi.adapter(Barcode::class.java)
    return adapter.toJson(barcode)
}

private fun serializeFields(fields: Map<String, PassField>, moshi: Moshi): String? {
    if (fields.isEmpty()) return null
    val type = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        PassField::class.java
    )
    val adapter = moshi.adapter<Map<String, PassField>>(type)
    return adapter.toJson(fields)
}
