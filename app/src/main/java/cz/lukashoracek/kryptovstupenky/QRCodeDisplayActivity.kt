// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.constraintlayout.compose.ConstraintLayout

class QRCodeDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = LightColorScheme,
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val (logo, title, qrCode) = createRefs()

                    Icon(
                        painterResource(R.drawable.ic_launcher_foreground),
                        "logo",
                        modifier = Modifier
                            .constrainAs(logo) {
                                top.linkTo(parent.top, margin = 16.dp)
                                bottom.linkTo(title.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                            .size(128.dp),
                        tint = Color(76, 175, 80),
                    )

                    Text(
                        text = intent.getStringExtra("title")!!,
                        modifier = Modifier
                            .constrainAs(title) {
                                top.linkTo(logo.bottom)
                                bottom.linkTo(qrCode.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 6.em,
                    )

                    Image(
                        QRCodeUtils.generateQRCodeBitmap(intent.getStringExtra("qrCodeData")!!).asImageBitmap(),
                        "QR code",
                        modifier = Modifier
                            .constrainAs(qrCode) {
                                top.linkTo(title.bottom)
                                bottom.linkTo(parent.bottom, margin = 32.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    )
                }
            }
        }
    }
}