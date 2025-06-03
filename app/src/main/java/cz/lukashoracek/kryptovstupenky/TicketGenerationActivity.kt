// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.security.PrivateKey

class TicketGenerationActivity : ComponentActivity() {
    private lateinit var privateKey: PrivateKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        privateKey = Crypto.decodePrivateKeyFromBase64(intent.getStringExtra("privateKeyBase64")!!)

        setContent {
            MaterialTheme(
                colorScheme = LightColorScheme,
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val (logo, text, button) = createRefs()

                    Icon(
                        painterResource(R.drawable.ic_launcher_foreground),
                        "logo",
                        modifier = Modifier
                            .constrainAs(logo) {
                                top.linkTo(parent.top, margin = 16.dp)
                                bottom.linkTo(text.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .size(128.dp),
                        tint = Color(76, 175, 80),
                    )

                    Text(
                        "Here you can create a ticket and share it to an another app",
                        modifier = Modifier
                            .constrainAs(text) {
                                top.linkTo(logo.bottom)
                                bottom.linkTo(button.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .padding(32.dp),
                        fontSize = 5.em,
                        textAlign = TextAlign.Center,
                    )

                    Button(
                        onClick = {
                            generateAndShareTicket()
                        },
                        modifier = Modifier
                            .constrainAs(button) {
                                top.linkTo(text.bottom)
                                bottom.linkTo(parent.bottom)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                verticalBias = 0.0f
                            }
                            .size(192.dp, 96.dp),
                    ) {
                        Text(
                            text = "Vytvořit",
                            fontSize = 4.em,
                        )
                    }
                }
            }
        }
    }

    private fun generateAndShareTicket() {
        val ticket = Crypto.generateTicket(privateKey)
        val ticketData = Ticket.encodeTicket(ticket)
        val ticketQRCodeBitmap = QRCodeUtils.generateQRCodeBitmap(ticketData)

        shareTicketQRCode(ticketData.sliceArray(0..15), ticketQRCodeBitmap)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun shareTicketQRCode(ticketUID: ByteArray, ticketQRCodeBitmap: Bitmap) {
        val file = File(cacheDir, "generated_tickets/qr_${ticketUID.toHexString()}.png")
        file.parentFile?.mkdirs()
        if (!file.createNewFile())
            throw RuntimeException("Generated ticket file with same name already exists!")

        val outputStream = FileOutputStream(file)
        ticketQRCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()

        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.sharefileprovider", file)

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }
}