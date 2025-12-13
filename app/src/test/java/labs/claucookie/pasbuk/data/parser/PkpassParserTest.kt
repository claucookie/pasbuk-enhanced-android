package labs.claucookie.pasbuk.data.parser

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.every
import io.mockk.mockk
import labs.claucookie.pasbuk.domain.model.BarcodeFormat
import labs.claucookie.pasbuk.domain.model.PassType
import labs.claucookie.pasbuk.domain.repository.InvalidPassException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Unit tests for PkpassParser.
 *
 * Tests cover:
 * - T032: Parsing valid .pkpass files
 * - T033: Handling corrupted/invalid .pkpass files
 */
class PkpassParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var moshi: Moshi
    private lateinit var parser: PkpassParser
    private lateinit var filesDir: File

    @Before
    fun setup() {
        filesDir = tempFolder.newFolder("files")

        context = mockk(relaxed = true)
        contentResolver = mockk(relaxed = true)

        every { context.filesDir } returns filesDir
        every { context.contentResolver } returns contentResolver

        moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        parser = PkpassParser(context, moshi)
    }

    // ==================== T032: Test parsing valid .pkpass ====================

    @Test
    fun `parse valid boarding pass pkpass file`() {
        // Given: A valid .pkpass file with boarding pass structure
        val passJson = createValidBoardingPassJson()
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Pass is correctly parsed
        assertNotNull(pass.id)  // ID is UUID, just verify it exists
        assertEquals("ABC123456", pass.serialNumber)
        assertEquals("pass.com.airline.boarding", pass.passTypeIdentifier)
        assertEquals("Airline Inc.", pass.organizationName)
        assertEquals("Boarding Pass", pass.description)
        assertEquals("TEAM123", pass.teamIdentifier)
        assertEquals(PassType.BOARDING_PASS, pass.passType)
    }

    @Test
    fun `parse valid event ticket pkpass file`() {
        // Given: A valid .pkpass file with event ticket structure
        val passJson = createValidEventTicketJson()
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Pass is correctly parsed as event ticket
        assertEquals(PassType.EVENT_TICKET, pass.passType)
        assertEquals("Concert Venue", pass.organizationName)
        assertEquals("Concert Ticket", pass.description)
    }

    @Test
    fun `parse pkpass with barcode extracts barcode data`() {
        // Given: A .pkpass with QR code barcode
        val passJson = createPassJsonWithBarcode("PKBarcodeFormatQR", "TICKET123")
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Barcode is correctly extracted
        assertNotNull(pass.barcode)
        assertEquals("TICKET123", pass.barcode?.message)
        assertEquals(BarcodeFormat.QR, pass.barcode?.format)
    }

    @Test
    fun `parse pkpass with PDF417 barcode`() {
        // Given: A .pkpass with PDF417 barcode
        val passJson = createPassJsonWithBarcode("PKBarcodeFormatPDF417", "BOARDING-DATA")
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: PDF417 barcode is correctly parsed
        assertNotNull(pass.barcode)
        assertEquals("BOARDING-DATA", pass.barcode?.message)
        assertEquals(BarcodeFormat.PDF417, pass.barcode?.format)
    }

    @Test
    fun `parse pkpass with Aztec barcode`() {
        // Given: A .pkpass with Aztec barcode
        val passJson = createPassJsonWithBarcode("PKBarcodeFormatAztec", "AZTEC-DATA")
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Aztec barcode is correctly parsed
        assertNotNull(pass.barcode)
        assertEquals(BarcodeFormat.AZTEC, pass.barcode?.format)
    }

    @Test
    fun `parse pkpass with Code128 barcode`() {
        // Given: A .pkpass with Code128 barcode
        val passJson = createPassJsonWithBarcode("PKBarcodeFormatCode128", "CODE128-DATA")
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Code128 barcode is correctly parsed
        assertNotNull(pass.barcode)
        assertEquals(BarcodeFormat.CODE128, pass.barcode?.format)
    }

    @Test
    fun `parse pkpass with colors extracts color values`() {
        // Given: A .pkpass with custom colors
        val passJson = createPassJsonWithColors(
            backgroundColor = "rgb(255, 0, 0)",
            foregroundColor = "rgb(255, 255, 255)",
            labelColor = "rgb(200, 200, 200)"
        )
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Colors are extracted
        assertEquals("rgb(255, 0, 0)", pass.backgroundColor)
        assertEquals("rgb(255, 255, 255)", pass.foregroundColor)
        assertEquals("rgb(200, 200, 200)", pass.labelColor)
    }

    @Test
    fun `parse pkpass with relevant date extracts date`() {
        // Given: A .pkpass with relevant date
        val passJson = createPassJsonWithDate("2024-12-25T10:30:00Z")
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Date is extracted
        assertNotNull(pass.relevantDate)
    }

    @Test
    fun `parse pkpass without barcode returns null barcode`() {
        // Given: A .pkpass without barcode
        val passJson = createMinimalPassJson()
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Barcode is null
        assertNull(pass.barcode)
    }

    @Test
    fun `parse pkpass with locations extracts location data`() {
        // Given: A .pkpass with locations
        val passJson = createPassJsonWithLocations()
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Locations are extracted
        assertTrue(pass.locations.isNotEmpty())
        assertEquals(37.7749, pass.locations[0].latitude, 0.0001)
        assertEquals(-122.4194, pass.locations[0].longitude, 0.0001)
    }

    @Test
    fun `parse pkpass with fields extracts all field sections`() {
        // Given: A .pkpass with various fields
        val passJson = createPassJsonWithFields()
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Fields are extracted
        assertTrue(pass.fields.isNotEmpty())
    }

    // ==================== T033: Test handling corrupted/invalid .pkpass ====================

    @Test(expected = InvalidPassException::class)
    fun `parse throws InvalidPassException when file is not a valid ZIP`() {
        // Given: Invalid data that is not a ZIP file
        val invalidData = "This is not a ZIP file".toByteArray()
        val uri = mockUri(invalidData)

        // When/Then: Parsing throws InvalidPassException
        parser.parse(uri)
    }

    @Test(expected = InvalidPassException::class)
    fun `parse throws InvalidPassException when pass_json is missing`() {
        // Given: A ZIP file without pass.json
        val zipBytes = createZipWithoutPassJson()
        val uri = mockUri(zipBytes)

        // When/Then: Parsing throws InvalidPassException
        parser.parse(uri)
    }

    @Test(expected = InvalidPassException::class)
    fun `parse throws InvalidPassException when pass_json is invalid JSON`() {
        // Given: A ZIP file with invalid JSON in pass.json
        val invalidJson = "{ this is not valid json }"
        val zipBytes = createPkpassZip(invalidJson)
        val uri = mockUri(zipBytes)

        // When/Then: Parsing throws InvalidPassException
        parser.parse(uri)
    }

    @Test(expected = InvalidPassException::class)
    fun `parse throws InvalidPassException when pass_json is missing required fields`() {
        // Given: A ZIP file with pass.json missing required fields
        val incompleteJson = """{"formatVersion": 1}"""
        val zipBytes = createPkpassZip(incompleteJson)
        val uri = mockUri(zipBytes)

        // When/Then: Parsing throws InvalidPassException
        parser.parse(uri)
    }

    @Test(expected = InvalidPassException::class)
    fun `parse throws InvalidPassException when input stream is null`() {
        // Given: A URI that returns null input stream
        val uri = mockk<Uri>()
        every { contentResolver.openInputStream(uri) } returns null

        // When/Then: Parsing throws InvalidPassException
        parser.parse(uri)
    }

    @Test
    fun `parse handles malformed date gracefully`() {
        // Given: A .pkpass with malformed date
        val passJson = createPassJsonWithDate("not-a-date")
        val pkpassBytes = createPkpassZip(passJson)
        val uri = mockUri(pkpassBytes)

        // When: Parsing the file
        val pass = parser.parse(uri)

        // Then: Date is null (not throwing exception)
        assertNull(pass.relevantDate)
    }

    @Test
    fun `deletePassFiles removes pass directory`() {
        // Given: A pass directory exists
        val passId = "test-pass-id"
        val passDir = File(filesDir, "passes/$passId")
        passDir.mkdirs()
        File(passDir, "images").mkdirs()
        File(passDir, "images/logo.png").createNewFile()

        assertTrue(passDir.exists())

        // When: Deleting pass files
        parser.deletePassFiles(passId)

        // Then: Directory is deleted
        assertTrue(!passDir.exists())
    }

    // ==================== Helper Methods ====================

    private fun mockUri(data: ByteArray): Uri {
        val uri = mockk<Uri>()
        // Return a new InputStream each time openInputStream is called
        every { contentResolver.openInputStream(uri) } answers { ByteArrayInputStream(data) }
        return uri
    }

    private fun createPkpassZip(passJsonContent: String, includeImages: Boolean = false): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // Add pass.json
            zos.putNextEntry(ZipEntry("pass.json"))
            zos.write(passJsonContent.toByteArray())
            zos.closeEntry()

            // Add manifest.json (required by spec but not parsed)
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write("{}".toByteArray())
            zos.closeEntry()

            if (includeImages) {
                // Add a dummy logo image
                zos.putNextEntry(ZipEntry("logo.png"))
                zos.write(ByteArray(100)) // Fake image data
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    private fun createZipWithoutPassJson(): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write("{}".toByteArray())
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    private fun createValidBoardingPassJson(): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.airline.boarding",
            "serialNumber": "ABC123456",
            "teamIdentifier": "TEAM123",
            "organizationName": "Airline Inc.",
            "description": "Boarding Pass",
            "boardingPass": {
                "transitType": "PKTransitTypeAir",
                "headerFields": [],
                "primaryFields": [
                    {"key": "origin", "label": "From", "value": "SFO"},
                    {"key": "destination", "label": "To", "value": "LAX"}
                ],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createValidEventTicketJson(): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.venue.ticket",
            "serialNumber": "EVENT789",
            "teamIdentifier": "TEAM456",
            "organizationName": "Concert Venue",
            "description": "Concert Ticket",
            "eventTicket": {
                "headerFields": [],
                "primaryFields": [
                    {"key": "event", "label": "Event", "value": "Rock Concert"}
                ],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createMinimalPassJson(): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.example",
            "serialNumber": "MINIMAL123",
            "teamIdentifier": "TEAM789",
            "organizationName": "Example Org",
            "description": "Minimal Pass",
            "generic": {
                "headerFields": [],
                "primaryFields": [],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createPassJsonWithBarcode(format: String, message: String): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.example",
            "serialNumber": "BARCODE123",
            "teamIdentifier": "TEAM123",
            "organizationName": "Example Org",
            "description": "Pass with Barcode",
            "barcode": {
                "format": "$format",
                "message": "$message",
                "messageEncoding": "iso-8859-1"
            },
            "generic": {
                "headerFields": [],
                "primaryFields": [],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createPassJsonWithColors(
        backgroundColor: String,
        foregroundColor: String,
        labelColor: String
    ): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.example",
            "serialNumber": "COLORS123",
            "teamIdentifier": "TEAM123",
            "organizationName": "Example Org",
            "description": "Pass with Colors",
            "backgroundColor": "$backgroundColor",
            "foregroundColor": "$foregroundColor",
            "labelColor": "$labelColor",
            "generic": {
                "headerFields": [],
                "primaryFields": [],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createPassJsonWithDate(date: String): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.example",
            "serialNumber": "DATE123",
            "teamIdentifier": "TEAM123",
            "organizationName": "Example Org",
            "description": "Pass with Date",
            "relevantDate": "$date",
            "generic": {
                "headerFields": [],
                "primaryFields": [],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createPassJsonWithLocations(): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.example",
            "serialNumber": "LOCATION123",
            "teamIdentifier": "TEAM123",
            "organizationName": "Example Org",
            "description": "Pass with Locations",
            "locations": [
                {
                    "latitude": 37.7749,
                    "longitude": -122.4194,
                    "relevantText": "San Francisco"
                }
            ],
            "generic": {
                "headerFields": [],
                "primaryFields": [],
                "secondaryFields": [],
                "auxiliaryFields": [],
                "backFields": []
            }
        }
    """.trimIndent()

    private fun createPassJsonWithFields(): String = """
        {
            "formatVersion": 1,
            "passTypeIdentifier": "pass.com.example",
            "serialNumber": "FIELDS123",
            "teamIdentifier": "TEAM123",
            "organizationName": "Example Org",
            "description": "Pass with Fields",
            "eventTicket": {
                "headerFields": [
                    {"key": "header1", "label": "Header", "value": "Value1"}
                ],
                "primaryFields": [
                    {"key": "primary1", "label": "Primary", "value": "Value2"}
                ],
                "secondaryFields": [
                    {"key": "secondary1", "label": "Secondary", "value": "Value3"}
                ],
                "auxiliaryFields": [
                    {"key": "aux1", "label": "Auxiliary", "value": "Value4"}
                ],
                "backFields": [
                    {"key": "back1", "label": "Back", "value": "Value5"}
                ]
            }
        }
    """.trimIndent()
}
