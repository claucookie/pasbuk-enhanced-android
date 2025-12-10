package labs.claucookie.pasbuk.domain.model

import java.time.Instant

/**
 * Domain model representing a single imported .pkpass file.
 *
 * @property id Unique identifier (UUID or serial number)
 * @property serialNumber Serial number from pass.json
 * @property passTypeIdentifier Pass type identifier (e.g., "pass.com.airline.boardingpass")
 * @property organizationName Issuer organization name
 * @property description Pass title/description
 * @property teamIdentifier Apple Developer Team ID
 * @property relevantDate Primary event date/time (optional)
 * @property expirationDate When the pass expires (optional)
 * @property locations Associated geographic locations
 * @property logoText Text displayed near the logo (optional)
 * @property backgroundColor RGB hex color (optional)
 * @property foregroundColor RGB hex color (optional)
 * @property labelColor RGB hex color (optional)
 * @property barcode Primary barcode (optional)
 * @property logoImagePath Path to logo image in internal storage (optional)
 * @property iconImagePath Path to icon image in internal storage (optional)
 * @property thumbnailImagePath Path to thumbnail image in internal storage (optional)
 * @property stripImagePath Path to strip image in internal storage (optional)
 * @property backgroundImagePath Path to background image in internal storage (optional)
 * @property originalPkpassPath Path to original .pkpass file in internal storage
 * @property passType Type of pass (boarding, event, coupon, etc.)
 * @property fields All fields from pass.json sections (header, primary, secondary, auxiliary, back)
 * @property createdAt Timestamp when pass was imported
 * @property modifiedAt Timestamp when pass was last modified
 */
data class Pass(
    val id: String,
    val serialNumber: String,
    val passTypeIdentifier: String,
    val organizationName: String,
    val description: String,
    val teamIdentifier: String,
    val relevantDate: Instant?,
    val expirationDate: Instant?,
    val locations: List<Location>,
    val logoText: String?,
    val backgroundColor: String?,
    val foregroundColor: String?,
    val labelColor: String?,
    val barcode: Barcode?,
    val logoImagePath: String?,
    val iconImagePath: String?,
    val thumbnailImagePath: String?,
    val stripImagePath: String?,
    val backgroundImagePath: String?,
    val originalPkpassPath: String,
    val passType: PassType,
    val fields: Map<String, PassField>,
    val createdAt: Instant,
    val modifiedAt: Instant
)
