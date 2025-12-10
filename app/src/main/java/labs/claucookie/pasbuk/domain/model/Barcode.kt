package labs.claucookie.pasbuk.domain.model

/**
 * Barcode data for a pass.
 *
 * @property message The barcode data to encode
 * @property format The barcode format (QR, PDF417, etc.)
 * @property messageEncoding Character encoding (usually "iso-8859-1")
 * @property altText Alternative text displayed below barcode
 */
data class Barcode(
    val message: String,
    val format: BarcodeFormat,
    val messageEncoding: String,
    val altText: String?
)
