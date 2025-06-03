// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.view.PreviewView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.security.PublicKey

class TicketScannerActivity : ComponentActivity() {
    private var camera: QRCodeScannerCamera? = null
    private lateinit var publicKey: PublicKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        publicKey = Crypto.decodePublicKeyFromBase64(intent.getStringExtra("publicKeyBase64")!!)

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
        Log.d("TicketScannerActivity", "scanCallback")
        camera?.scannerPause = true

        var ticket: Ticket? = null
        try {
            ticket = Ticket.decodeTicket(rawData)
        } catch (e: InvalidTicketDataPrefixException) {
            ticketResultCallback(null, false, "This QR code is not a ticket!")
        } catch (e: InvalidTicketDataException) {
            ticketResultCallback(null, false, "This ticket is not valid!")
        }

        if (ticket != null) {
            val validTicket = Crypto.verifyTicket(ticket, publicKey)

            if (validTicket) {
                lifecycleScope.launch {
                    val ticketUIDAsString = ticket.uid.toString(Charsets.ISO_8859_1)

                    if (DataStoreUtils.getBoolean(scannedTicketStorage, "ticket_$ticketUIDAsString") == true) {
                        ticketResultCallback(ticket, false, "This ticket has been already scanned!")
                    } else {
                        DataStoreUtils.setBoolean(scannedTicketStorage, "ticket_$ticketUIDAsString", true)
                        ticketResultCallback(ticket, true, null)
                    }
                }
            } else {
                ticketResultCallback(ticket, false, "This ticket is signed using an unknown key pair!")
            }
        }
    }

    private fun ticketResultCallback(ticket: Ticket?, validTicket: Boolean, userVisibleError: String?) {
        Log.d("TicketScannerActivity", "ticketResultCallback(${ticket != null}, $validTicket)")

        val ticketResultIntent = Intent(this, TicketResult::class.java)
        ticketResultIntent.putExtra("ticketValid", validTicket)
        if (ticket != null)
            ticketResultIntent.putExtra("ticketUID", ticket.uid)
        if (userVisibleError != null)
            ticketResultIntent.putExtra("userVisibleError", userVisibleError)

        startActivity(ticketResultIntent)
    }

    override fun onPause() {
        super.onPause()

        Log.d("TicketScannerActivity", "onPause")
        camera?.scannerPause = true
    }

    override fun onResume() {
        super.onResume()

        Log.d("TicketScannerActivity", "onResume")
        camera?.scannerPause = false
    }
}