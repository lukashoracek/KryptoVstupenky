// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Crypto.setupBouncyCastle()

        updateContent()
    }

    override fun onResume() {
        super.onResume()

        updateContent()
    }

    private fun updateContent() {
        lifecycleScope.launch {
            val scanAvailable = !DataStoreUtils.getString(appDataStorage, "public_key_base64").isNullOrEmpty()
            val genAvailable = !DataStoreUtils.getString(appDataStorage, "private_key_base64").isNullOrEmpty()

            runOnUiThread {
                setContent {
                    ActivityComposable(
                        scanAvailable = scanAvailable,
                        generationAvailable = genAvailable,
                    )
                }
            }
        }
    }

    @Composable
    @Preview
    private fun ActivityComposable(scanAvailable: Boolean = false, generationAvailable: Boolean = false) {
        MaterialTheme(
            colorScheme = LightColorScheme,
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val (logo, title, keyManagement, scanner, generator) = createRefs()

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
                    "KryptoVstupenky",
                    modifier = Modifier
                        .constrainAs(title) {
                            top.linkTo(logo.bottom)
                            bottom.linkTo(keyManagement.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        openKeyManagement()
                    },
                    modifier = Modifier
                        .constrainAs(keyManagement) {
                            top.linkTo(title.bottom)
                            bottom.linkTo(scanner.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            verticalBias = 1.0f
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Key pair management",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    enabled = scanAvailable,
                    onClick = {
                        openScanner()
                    },
                    modifier = Modifier
                        .constrainAs(scanner) {
                            top.linkTo(keyManagement.bottom)
                            bottom.linkTo(generator.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            verticalBias = 1.0f
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Scan",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    enabled = generationAvailable,
                    onClick = {
                        openTicketGeneration()
                    },
                    modifier = Modifier
                        .constrainAs(generator) {
                            top.linkTo(scanner.bottom)
                            bottom.linkTo(parent.bottom, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            verticalBias = 0.0f
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Generate",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }


    private fun openKeyManagement() {
        val keyManagementActivityIntent = Intent(this, KeyManagementActivity::class.java)
        startActivity(keyManagementActivityIntent)
    }

    private fun openScanner() {
        lifecycleScope.launch {
            val scannerActivityIntent = Intent(this@MenuActivity, TicketScannerActivity::class.java)
            scannerActivityIntent.putExtra("publicKeyBase64", DataStoreUtils.getString(appDataStorage, "public_key_base64"))
            startActivity(scannerActivityIntent)
        }
    }

    private fun openTicketGeneration() {
        lifecycleScope.launch {
            val ticketGenerationActivityIntent = Intent(this@MenuActivity, TicketGenerationActivity::class.java)
            ticketGenerationActivityIntent.putExtra("privateKeyBase64", DataStoreUtils.getString(appDataStorage, "private_key_base64"))
            startActivity(ticketGenerationActivityIntent)
        }
    }
}