package labs.claucookie.pasbuk.data.parser

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Root structure of pass.json from a .pkpass file.
 * Based on Apple's Wallet Pass Format Reference.
 */
@JsonClass(generateAdapter = true)
data class PassJson(
    // Standard keys (required)
    @Json(name = "formatVersion") val formatVersion: Int,
    @Json(name = "passTypeIdentifier") val passTypeIdentifier: String,
    @Json(name = "serialNumber") val serialNumber: String,
    @Json(name = "teamIdentifier") val teamIdentifier: String,
    @Json(name = "organizationName") val organizationName: String,
    @Json(name = "description") val description: String,

    // Visual appearance (optional)
    @Json(name = "backgroundColor") val backgroundColor: String? = null,
    @Json(name = "foregroundColor") val foregroundColor: String? = null,
    @Json(name = "labelColor") val labelColor: String? = null,
    @Json(name = "logoText") val logoText: String? = null,

    // Relevance keys (optional)
    @Json(name = "relevantDate") val relevantDate: String? = null,
    @Json(name = "expirationDate") val expirationDate: String? = null,
    @Json(name = "locations") val locations: List<LocationJson>? = null,
    @Json(name = "maxDistance") val maxDistance: Double? = null,

    // Barcode keys (optional)
    @Json(name = "barcode") val barcode: BarcodeJson? = null,
    @Json(name = "barcodes") val barcodes: List<BarcodeJson>? = null,

    // Pass type specific structures (only one should be present)
    @Json(name = "boardingPass") val boardingPass: PassStructureJson? = null,
    @Json(name = "eventTicket") val eventTicket: PassStructureJson? = null,
    @Json(name = "coupon") val coupon: PassStructureJson? = null,
    @Json(name = "storeCard") val storeCard: PassStructureJson? = null,
    @Json(name = "generic") val generic: PassStructureJson? = null,

    // Web service keys (optional)
    @Json(name = "webServiceURL") val webServiceURL: String? = null,
    @Json(name = "authenticationToken") val authenticationToken: String? = null,

    // Associated app keys (optional)
    @Json(name = "associatedStoreIdentifiers") val associatedStoreIdentifiers: List<Long>? = null,
    @Json(name = "appLaunchURL") val appLaunchURL: String? = null,

    // NFC keys (optional)
    @Json(name = "nfc") val nfc: NfcJson? = null,

    // Sharing keys (optional)
    @Json(name = "sharingProhibited") val sharingProhibited: Boolean? = null,

    // Semantic tags (iOS 12+, optional)
    @Json(name = "semantics") val semantics: Map<String, Any>? = null,

    // User info (optional)
    @Json(name = "userInfo") val userInfo: Map<String, Any>? = null
) {
    /**
     * Returns the active pass structure based on pass type.
     */
    fun getPassStructure(): PassStructureJson? {
        return boardingPass ?: eventTicket ?: coupon ?: storeCard ?: generic
    }

    /**
     * Returns the pass type based on which structure is present.
     */
    fun getPassType(): String {
        return when {
            boardingPass != null -> "BOARDING_PASS"
            eventTicket != null -> "EVENT_TICKET"
            coupon != null -> "COUPON"
            storeCard != null -> "STORE_CARD"
            generic != null -> "GENERIC"
            else -> "GENERIC"
        }
    }

    /**
     * Returns the primary barcode (prefers barcodes array over legacy barcode field).
     */
    fun getPrimaryBarcode(): BarcodeJson? {
        return barcodes?.firstOrNull() ?: barcode
    }
}

/**
 * Pass structure containing field groups.
 * Used for all pass types (boardingPass, eventTicket, coupon, storeCard, generic).
 */
@JsonClass(generateAdapter = true)
data class PassStructureJson(
    @Json(name = "headerFields") val headerFields: List<FieldJson>? = null,
    @Json(name = "primaryFields") val primaryFields: List<FieldJson>? = null,
    @Json(name = "secondaryFields") val secondaryFields: List<FieldJson>? = null,
    @Json(name = "auxiliaryFields") val auxiliaryFields: List<FieldJson>? = null,
    @Json(name = "backFields") val backFields: List<FieldJson>? = null,

    // Boarding pass specific
    @Json(name = "transitType") val transitType: String? = null
) {
    /**
     * Returns all fields from all sections as a flat map keyed by field key.
     */
    fun getAllFields(): Map<String, FieldJson> {
        val allFields = mutableMapOf<String, FieldJson>()

        headerFields?.forEach { allFields[it.key] = it }
        primaryFields?.forEach { allFields[it.key] = it }
        secondaryFields?.forEach { allFields[it.key] = it }
        auxiliaryFields?.forEach { allFields[it.key] = it }
        backFields?.forEach { allFields[it.key] = it }

        return allFields
    }
}

/**
 * A single field on a pass.
 */
@JsonClass(generateAdapter = true)
data class FieldJson(
    @Json(name = "key") val key: String,
    @Json(name = "label") val label: String? = null,
    @Json(name = "value") val value: Any, // Can be String, Number, or Date string
    @Json(name = "textAlignment") val textAlignment: String? = null,

    // Attributed value (HTML-like formatting)
    @Json(name = "attributedValue") val attributedValue: String? = null,

    // Change message for updates
    @Json(name = "changeMessage") val changeMessage: String? = null,

    // Date/time formatting
    @Json(name = "dateStyle") val dateStyle: String? = null,
    @Json(name = "timeStyle") val timeStyle: String? = null,
    @Json(name = "isRelative") val isRelative: Boolean? = null,
    @Json(name = "ignoresTimeZone") val ignoresTimeZone: Boolean? = null,

    // Number formatting
    @Json(name = "numberStyle") val numberStyle: String? = null,
    @Json(name = "currencyCode") val currencyCode: String? = null,

    // Data detector types
    @Json(name = "dataDetectorTypes") val dataDetectorTypes: List<String>? = null,

    // Row configuration
    @Json(name = "row") val row: Int? = null
) {
    /**
     * Returns the value as a string, handling different types.
     */
    fun getValueAsString(): String {
        return when (value) {
            is String -> value
            is Number -> value.toString()
            else -> value.toString()
        }
    }
}

/**
 * Barcode information.
 */
@JsonClass(generateAdapter = true)
data class BarcodeJson(
    @Json(name = "message") val message: String,
    @Json(name = "format") val format: String,
    @Json(name = "messageEncoding") val messageEncoding: String? = "iso-8859-1",
    @Json(name = "altText") val altText: String? = null
) {
    companion object {
        const val FORMAT_QR = "PKBarcodeFormatQR"
        const val FORMAT_PDF417 = "PKBarcodeFormatPDF417"
        const val FORMAT_AZTEC = "PKBarcodeFormatAztec"
        const val FORMAT_CODE128 = "PKBarcodeFormatCode128"
    }

    /**
     * Converts Apple's barcode format string to our enum name.
     */
    fun toBarcodeFormatName(): String {
        return when (format) {
            FORMAT_QR -> "QR"
            FORMAT_PDF417 -> "PDF417"
            FORMAT_AZTEC -> "AZTEC"
            FORMAT_CODE128 -> "CODE128"
            else -> "QR" // Default fallback
        }
    }
}

/**
 * Location information for relevance.
 */
@JsonClass(generateAdapter = true)
data class LocationJson(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "altitude") val altitude: Double? = null,
    @Json(name = "relevantText") val relevantText: String? = null
)

/**
 * NFC information for contactless passes.
 */
@JsonClass(generateAdapter = true)
data class NfcJson(
    @Json(name = "message") val message: String,
    @Json(name = "encryptionPublicKey") val encryptionPublicKey: String? = null,
    @Json(name = "requiresAuthentication") val requiresAuthentication: Boolean? = null
)
