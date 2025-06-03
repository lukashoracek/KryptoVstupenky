// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.Hashtable

class QRCodeUtils {
    companion object {
        fun generateQRCodeBitmap(stringData: String): Bitmap {
            val hints: Hashtable<EncodeHintType, Any> = Hashtable()
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(stringData, BarcodeFormat.QR_CODE, 1000, 1000, hints)

            val bitmap = createBitmap(bitMatrix.width, bitMatrix.height)
            for (x in 0..<bitMatrix.width) {
                for (y in 0..<bitMatrix.height) {
                    bitmap[x, y] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }

            return bitmap
        }

        fun generateQRCodeBitmap(binaryData: ByteArray): Bitmap {
            val dataInString = binaryData.toString(Charsets.ISO_8859_1) // We need fixed length, single byte character set

            if (!dataInString.toByteArray(Charsets.ISO_8859_1).contentEquals(binaryData)) {
                throw RuntimeException("Encoded binary data in string does not match original data!")
            }

            return generateQRCodeBitmap(dataInString)
        }
    }
}