// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class KeyImportScannerActivity : ComponentActivity() {
    private var camera: QRCodeScannerCamera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scanner)

        QRCodeScannerCamera.checkCameraPermission(this) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permission to use the camera has been denied.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        camera = QRCodeScannerCamera(this, this, findViewById<PreviewView>(R.id.previewView), ::scanCallback)
    }

    private fun scanCallback(rawData: ByteArray) {
        Log.d("KeyImportScannerActivity", "scanCallback")
        camera?.scannerPause = true

        val dataAsString = rawData.toString(Charsets.ISO_8859_1)

        if (dataAsString.startsWith(KEY_MANAGEMENT_QR_PRIVATE_KEY_PREFIX)) {
            val privateKeyBase64 = dataAsString.substring(KEY_MANAGEMENT_QR_PRIVATE_KEY_PREFIX.length)

            val publicKey = Crypto.privateKeyToPublicKey(Crypto.decodePrivateKeyFromBase64(privateKeyBase64))
            val publicKeyBase64 = Crypto.encodePublicKeyToBase64(publicKey)

            lifecycleScope.launch {
                DataStoreUtils.setString(appDataStorage, "private_key_base64", privateKeyBase64)
                DataStoreUtils.setString(appDataStorage, "public_key_base64", publicKeyBase64)
                DataStoreUtils.clear(scannedTicketStorage)

                runOnUiThread {
                    Toast.makeText(this@KeyImportScannerActivity, "Successfully imported a private and a public key!", Toast.LENGTH_LONG).show()
                }
                finish()
            }
        } else if (dataAsString.startsWith(KEY_MANAGEMENT_QR_PUBLIC_KEY_PREFIX)) {
            val publicKeyBase64 = dataAsString.substring(KEY_MANAGEMENT_QR_PUBLIC_KEY_PREFIX.length)

            lifecycleScope.launch {
                DataStoreUtils.removeString(appDataStorage, "private_key_base64")
                DataStoreUtils.setString(appDataStorage, "public_key_base64", publicKeyBase64)
                DataStoreUtils.clear(scannedTicketStorage)

                runOnUiThread {
                    Toast.makeText(this@KeyImportScannerActivity, "Successfully imported a public key!", Toast.LENGTH_LONG).show()
                }
                finish()
            }
        } else {
            runOnUiThread {
                Toast.makeText(this, "Unknown QR code!", Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }
}