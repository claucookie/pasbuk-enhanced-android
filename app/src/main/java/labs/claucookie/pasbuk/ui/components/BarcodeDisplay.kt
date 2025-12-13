package labs.claucookie.pasbuk.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import labs.claucookie.pasbuk.domain.model.Barcode
import labs.claucookie.pasbuk.domain.model.BarcodeFormat as DomainBarcodeFormat

/**
 * Composable that displays a barcode using ZXing.
 *
 * @param barcode The barcode data to display
 * @param modifier Modifier for the composable
 * @param size The size of the barcode image
 * @param showAltText Whether to show the alternative text below the barcode
 */
@Composable
fun BarcodeDisplay(
    barcode: Barcode,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    showAltText: Boolean = true
) {
    val bitmap = remember(barcode.message, barcode.format, size) {
        generateBarcodeBitmap(
            message = barcode.message,
            format = barcode.format,
            width = size.value.toInt() * 2, // Higher resolution for clarity
            height = if (barcode.format == DomainBarcodeFormat.QR ||
                barcode.format == DomainBarcodeFormat.AZTEC
            ) {
                size.value.toInt() * 2
            } else {
                (size.value.toInt() * 0.5).toInt() * 2 // Shorter height for 1D barcodes
            }
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Barcode",
                    modifier = Modifier.size(size),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (showAltText && !barcode.altText.isNullOrBlank()) {
            Text(
                text = barcode.altText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Generates a barcode bitmap using ZXing.
 */
private fun generateBarcodeBitmap(
    message: String,
    format: DomainBarcodeFormat,
    width: Int,
    height: Int
): Bitmap? {
    return try {
        val zxingFormat = when (format) {
            DomainBarcodeFormat.QR -> BarcodeFormat.QR_CODE
            DomainBarcodeFormat.PDF417 -> BarcodeFormat.PDF_417
            DomainBarcodeFormat.AZTEC -> BarcodeFormat.AZTEC
            DomainBarcodeFormat.CODE128 -> BarcodeFormat.CODE_128
        }

        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            message,
            zxingFormat,
            width,
            height
        )

        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }

        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true)
@Composable
private fun BarcodeDisplayQRPreview() {
    MaterialTheme {
        Surface {
            BarcodeDisplay(
                barcode = Barcode(
                    message = "https://example.com/pass/12345",
                    format = DomainBarcodeFormat.QR,
                    messageEncoding = "iso-8859-1",
                    altText = "12345-ABCDE"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarcodeDisplayPDF417Preview() {
    MaterialTheme {
        Surface {
            BarcodeDisplay(
                barcode = Barcode(
                    message = "BOARDING PASS DATA",
                    format = DomainBarcodeFormat.PDF417,
                    messageEncoding = "iso-8859-1",
                    altText = "Flight AA123"
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
