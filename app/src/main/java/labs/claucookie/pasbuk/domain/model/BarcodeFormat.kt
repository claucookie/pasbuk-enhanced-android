package labs.claucookie.pasbuk.domain.model

/**
 * Supported barcode formats in Apple Wallet passes.
 */
enum class BarcodeFormat {
    QR,
    PDF417,
    AZTEC,
    CODE128
}
