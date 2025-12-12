package labs.claucookie.pasbuk.data.parser

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.BarcodeFormat
import labs.claucookie.pasbuk.domain.model.Location
import labs.claucookie.pasbuk.domain.model.Pass
import labs.claucookie.pasbuk.domain.model.PassField
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.model.TextAlignment
import labs.claucookie.pasbuk.domain.repository.InvalidPassException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for .pkpass files.
 *
 * A .pkpass file is a ZIP archive containing:
 * - pass.json: All pass metadata
 * - manifest.json: File checksums
 * - signature: PKCS#7 signature
 * - Various image assets (logo.png, icon.png, etc.)
 */
@Singleton
class PkpassParser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    companion object {
        private const val PASS_JSON = "pass.json"
        private const val PASSES_DIR = "passes"
        private const val ORIGINAL_DIR = "original"
        private const val IMAGES_DIR = "images"

        // Image file names in .pkpass
        private val IMAGE_FILES = setOf(
            "logo.png", "logo@2x.png", "logo@3x.png",
            "icon.png", "icon@2x.png", "icon@3x.png",
            "thumbnail.png", "thumbnail@2x.png", "thumbnail@3x.png",
            "strip.png", "strip@2x.png", "strip@3x.png",
            "background.png", "background@2x.png", "background@3x.png",
            "footer.png", "footer@2x.png", "footer@3x.png"
        )
    }

    /**
     * Parses a .pkpass file from the given URI.
     *
     * @param uri URI to the .pkpass file (from file picker)
     * @return Parsed Pass domain model
     * @throws InvalidPassException if the file is invalid or cannot be parsed
     */
    fun parse(uri: Uri): Pass {
        val passId = UUID.randomUUID().toString()
        val passDir = getPassDirectory(passId)
        val imagesDir = File(passDir, IMAGES_DIR)

        try {
            // Create directories
            passDir.mkdirs()
            imagesDir.mkdirs()

            // Copy original .pkpass file
            val originalFile = copyOriginalFile(uri, passId)

            // Extract and parse
            val passJson = extractAndParse(uri, imagesDir)

            // Convert to domain model
            return convertToDomain(
                passId = passId,
                passJson = passJson,
                imagesDir = imagesDir,
                originalPath = originalFile.absolutePath
            )
        } catch (e: InvalidPassException) {
            // Clean up on failure
            passDir.deleteRecursively()
            throw e
        } catch (e: Exception) {
            // Clean up on failure
            passDir.deleteRecursively()
            throw InvalidPassException("Failed to parse .pkpass file: ${e.message}", e)
        }
    }

    /**
     * Deletes all files associated with a pass.
     */
    fun deletePassFiles(passId: String) {
        val passDir = getPassDirectory(passId)
        passDir.deleteRecursively()
    }

    private fun getPassDirectory(passId: String): File {
        return File(context.filesDir, "$PASSES_DIR/$passId")
    }

    private fun copyOriginalFile(uri: Uri, passId: String): File {
        val originalDir = File(context.filesDir, "$PASSES_DIR/$ORIGINAL_DIR")
        originalDir.mkdirs()
        val originalFile = File(originalDir, "$passId.pkpass")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(originalFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw InvalidPassException("Cannot read .pkpass file")

        return originalFile
    }

    private fun extractAndParse(uri: Uri, imagesDir: File): PassJson {
        var passJson: PassJson? = null

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zipInput ->
                var entry: ZipEntry? = zipInput.nextEntry

                while (entry != null) {
                    val fileName = entry.name

                    when {
                        fileName == PASS_JSON -> {
                            passJson = parsePassJson(zipInput)
                        }
                        isImageFile(fileName) -> {
                            extractImage(zipInput, imagesDir, fileName)
                        }
                    }

                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }
            }
        } ?: throw InvalidPassException("Cannot read .pkpass file")

        return passJson ?: throw InvalidPassException("pass.json not found in .pkpass file")
    }

    private fun parsePassJson(inputStream: InputStream): PassJson {
        val json = inputStream.bufferedReader().readText()
        val adapter = moshi.adapter(PassJson::class.java)

        return adapter.fromJson(json)
            ?: throw InvalidPassException("Failed to parse pass.json")
    }

    private fun isImageFile(fileName: String): Boolean {
        return IMAGE_FILES.any { fileName.endsWith(it, ignoreCase = true) }
    }

    private fun extractImage(zipInput: ZipInputStream, imagesDir: File, fileName: String) {
        val imageFile = File(imagesDir, fileName.replace("/", "_"))
        try {
            FileOutputStream(imageFile).use { output ->
                zipInput.copyTo(output)
            }
        } catch (e: IOException) {
            // Non-fatal: continue without this image
        }
    }

    private fun convertToDomain(
        passId: String,
        passJson: PassJson,
        imagesDir: File,
        originalPath: String
    ): Pass {
        val passStructure = passJson.getPassStructure()
        val primaryBarcode = passJson.getPrimaryBarcode()

        return Pass(
            id = passId,
            serialNumber = passJson.serialNumber,
            passTypeIdentifier = passJson.passTypeIdentifier,
            organizationName = passJson.organizationName,
            description = passJson.description,
            teamIdentifier = passJson.teamIdentifier,
            relevantDate = parseDate(passJson.relevantDate),
            expirationDate = parseDate(passJson.expirationDate),
            locations = passJson.locations?.map { it.toDomain() } ?: emptyList(),
            logoText = passJson.logoText,
            backgroundColor = passJson.backgroundColor,
            foregroundColor = passJson.foregroundColor,
            labelColor = passJson.labelColor,
            barcode = primaryBarcode?.toDomain(),
            logoImagePath = findBestImage(imagesDir, "logo"),
            iconImagePath = findBestImage(imagesDir, "icon"),
            thumbnailImagePath = findBestImage(imagesDir, "thumbnail"),
            stripImagePath = findBestImage(imagesDir, "strip"),
            backgroundImagePath = findBestImage(imagesDir, "background"),
            originalPkpassPath = originalPath,
            passType = PassType.valueOf(passJson.getPassType()),
            fields = passStructure?.getAllFields()?.mapValues { it.value.toDomain() } ?: emptyMap(),
            createdAt = Instant.now(),
            modifiedAt = Instant.now()
        )
    }

    private fun parseDate(dateString: String?): Instant? {
        if (dateString.isNullOrBlank()) return null

        return try {
            // Try ISO 8601 format first (with timezone)
            Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(dateString))
        } catch (e: DateTimeParseException) {
            try {
                // Try without timezone (assume UTC)
                Instant.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(dateString + "Z"))
            } catch (e2: DateTimeParseException) {
                try {
                    // Try date only format
                    val localDate = java.time.LocalDate.parse(dateString)
                    localDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                } catch (e3: DateTimeParseException) {
                    null
                }
            }
        }
    }

    private fun findBestImage(imagesDir: File, baseName: String): String? {
        // Prefer higher resolution images
        val candidates = listOf("${baseName}@3x.png", "${baseName}@2x.png", "${baseName}.png")

        for (candidate in candidates) {
            val file = File(imagesDir, candidate)
            if (file.exists()) {
                return file.absolutePath
            }
        }

        return null
    }
}

// Extension functions to convert JSON models to domain models

private fun LocationJson.toDomain(): Location {
    return Location(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        relevantText = relevantText
    )
}

private fun BarcodeJson.toDomain(): Barcode {
    return Barcode(
        message = message,
        format = BarcodeFormat.valueOf(toBarcodeFormatName()),
        messageEncoding = messageEncoding ?: "iso-8859-1",
        altText = altText
    )
}

private fun FieldJson.toDomain(): PassField {
    return PassField(
        key = key,
        label = label,
        value = getValueAsString(),
        textAlignment = textAlignment?.let { parseTextAlignment(it) }
    )
}

private fun parseTextAlignment(alignment: String): TextAlignment? {
    return when (alignment.uppercase()) {
        "PKTEXTALIGNMENTLEFT", "LEFT" -> TextAlignment.LEFT
        "PKTEXTALIGNMENTCENTER", "CENTER" -> TextAlignment.CENTER
        "PKTEXTALIGNMENTRIGHT", "RIGHT" -> TextAlignment.RIGHT
        "PKTEXTALIGNMENTNATURAL", "NATURAL" -> TextAlignment.NATURAL
        else -> null
    }
}
