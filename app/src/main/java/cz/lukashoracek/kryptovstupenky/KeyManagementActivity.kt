// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

const val KEY_MANAGEMENT_QR_PUBLIC_KEY_PREFIX = "KVPUBLIC_"
const val KEY_MANAGEMENT_QR_PRIVATE_KEY_PREFIX = "KVPRIVATE_"

class KeyManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateContent()
    }

    override fun onResume() {
        super.onResume()

        updateContent()
    }

    private fun updateContent() {
        lifecycleScope.launch {
            val publicKeyAvailable = !DataStoreUtils.getString(appDataStorage, "public_key_base64").isNullOrEmpty()
            val privateKeyAvailable = !DataStoreUtils.getString(appDataStorage, "private_key_base64").isNullOrEmpty()

            runOnUiThread {
                setContent {
                    ActivityComposable(
                        publicKeyAvailable = publicKeyAvailable,
                        privateKeyAvailable = privateKeyAvailable,
                    )
                }
            }
        }
    }

    @Composable
    @Preview
    private fun ActivityComposable(publicKeyAvailable: Boolean = false, privateKeyAvailable: Boolean = false) {
        MaterialTheme(
            colorScheme = LightColorScheme,
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val (logo, title, generateNewPair, import, exportPublic, exportPrivate) = createRefs()

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
                    "Key pair management",
                    modifier = Modifier
                        .constrainAs(title) {
                            top.linkTo(logo.bottom)
                            bottom.linkTo(generateNewPair.top, margin = 32.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        },
                    fontSize = 6.em,
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        generateAndPersistNewPair()
                    },
                    modifier = Modifier
                        .constrainAs(generateNewPair) {
                            top.linkTo(title.bottom, margin = 32.dp)
                            bottom.linkTo(import.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Generate a new key pair",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = {
                        startImportScanner()
                    },
                    modifier = Modifier
                        .constrainAs(import) {
                            top.linkTo(generateNewPair.bottom)
                            bottom.linkTo(exportPublic.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Import",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    enabled = publicKeyAvailable,
                    onClick = {
                        showPublicKey()
                    },
                    modifier = Modifier
                        .constrainAs(exportPublic) {
                            top.linkTo(import.bottom)
                            bottom.linkTo(exportPrivate.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Show public key",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    enabled = privateKeyAvailable,
                    onClick = {
                        showPrivateKey()
                    },
                    modifier = Modifier
                        .constrainAs(exportPrivate) {
                            top.linkTo(exportPublic.bottom)
                            bottom.linkTo(parent.bottom, margin = 16.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .size(192.dp, 96.dp),
                ) {
                    Text(
                        text = "Show private key",
                        fontSize = 4.em,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    private fun startImportScanner() {
        var importScannerActivityIntent = Intent(this, KeyImportScannerActivity::class.java)
        startActivity(importScannerActivityIntent)
    }

    private fun showQRCode(title: String, data: String) {
        val qrCodeDisplayActivityIntent = Intent(this, QRCodeDisplayActivity::class.java)
        qrCodeDisplayActivityIntent.putExtra("title", title)
        qrCodeDisplayActivityIntent.putExtra("qrCodeData", data)
        startActivity(qrCodeDisplayActivityIntent)
    }

    private fun generateAndPersistNewPair() {
        Log.i("KeyManagementActivity", "Generating and persistent a new key pair")

        val keyPair = Crypto.generateNewKeyPair()

        val publicKeyBase64 = Crypto.encodePublicKeyToBase64(keyPair.public)
        val privateKeyBase64 = Crypto.encodePrivateKeyToBase64(keyPair.private)

        lifecycleScope.launch {
            DataStoreUtils.setString(appDataStorage, "public_key_base64", publicKeyBase64)
            DataStoreUtils.setString(appDataStorage, "private_key_base64", privateKeyBase64)
            DataStoreUtils.clear(scannedTicketStorage)

            Log.i("KeyManagementActivity", "Successfully generated and persisted a new key pair")
            Toast.makeText(this@KeyManagementActivity, "A new key pair has been generated!", Toast.LENGTH_LONG).show()

            updateContent() // We now have public and private key, update the button states
        }
    }

    private fun showPublicKey() {
        lifecycleScope.launch {
            val publicKeyBase64 = DataStoreUtils.getString(appDataStorage, "public_key_base64")

            showQRCode("Veřejný klíč", "$KEY_MANAGEMENT_QR_PUBLIC_KEY_PREFIX$publicKeyBase64")
        }
    }

    private fun showPrivateKey() {
        lifecycleScope.launch {
            val privateKeyBase64 = DataStoreUtils.getString(appDataStorage, "private_key_base64")

            showQRCode("Soukromý klíč", "$KEY_MANAGEMENT_QR_PRIVATE_KEY_PREFIX$privateKeyBase64")
        }
    }
}