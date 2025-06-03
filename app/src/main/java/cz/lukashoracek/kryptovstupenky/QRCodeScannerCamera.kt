// Copyright (c) 2025 Lukáš Horáček
// SPDX-License-Identifier: MIT

package cz.lukashoracek.kryptovstupenky

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRCodeScannerCamera(context: Context, lifecycleOwner: LifecycleOwner, private val previewView: PreviewView, private val scanCallback: (ByteArray) -> Unit) {
    companion object {
        fun checkCameraPermission(activity: ComponentActivity, callback: (Boolean) -> Unit) {
            Log.d("QRCodeScannerCamera", "cameraPermission call")

            val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                callback(isGranted)
            }

            if (activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                callback(true)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private val cameraController: LifecycleCameraController
    private val qrAnalyzerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val qrAnalyzer: MlKitAnalyzer
    private val callbackExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var scannerPause = false
        set(newValue) {
            if (!newValue) {
                Log.d("QRCodeScannerCamera", "Clearing previewView overlay")
                previewView.overlay.clear()
            }

            field = newValue
        }

    init {
        cameraController = LifecycleCameraController(context)
        cameraController.imageAnalysisResolutionSelector = createResolutionSelector()
        cameraController.previewResolutionSelector = cameraController.imageAnalysisResolutionSelector
        cameraController.setEnabledUseCases(CameraController.IMAGE_ANALYSIS)

        cameraController.isPinchToZoomEnabled = false

        qrAnalyzer = createQRAnalyzer()
        cameraController.setImageAnalysisAnalyzer(qrAnalyzerExecutor, qrAnalyzer)

        cameraController.bindToLifecycle(lifecycleOwner)
        previewView.controller = cameraController
    }

    private fun createResolutionSelector(): ResolutionSelector {
        return ResolutionSelector.Builder().setResolutionFilter { supportedSizes, _ ->
            val sortedByPixelCount = supportedSizes.sortedBy { it.height * it.width } // sort by pixel count (low to high)

            sortedByPixelCount.forEachIndexed { index, resolution ->
                val pixelCount = resolution.width * resolution.height
                val megapixels = pixelCount / 1000000.0

                if (megapixels >= 1) {
                    Log.i(
                        "ResolutionSelection",
                        "Selected resolution: ${resolution.width}x${resolution.height} (${"%.1f".format(megapixels)} MP)"
                    )

                    if (megapixels >= 10)
                        Log.w(
                            "ResolutionSelection",
                            "Selected resolution (${"%.1f".format(megapixels)} MP) is large, this may cause performance issues!"
                        )

                    return@setResolutionFilter listOf(resolution)
                } else if (index == sortedByPixelCount.lastIndex) {
                    Log.w(
                        "ResolutionSelection",
                        "No suitable resolution found, selecting highest available resolution: ${resolution.width}x${resolution.height}  (${"%.1f".format(megapixels)} MP)"
                    )
                    return@setResolutionFilter listOf(resolution)
                }
            }

            error("Reached unreachable code")
        }.build()
    }

    private fun createQRAnalyzer(): MlKitAnalyzer {
        val barcodeOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)

        return MlKitAnalyzer(listOf(barcodeScanner), ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED, qrAnalyzerExecutor) { result: MlKitAnalyzer.Result? ->
            if (result == null || scannerPause)
                return@MlKitAnalyzer

            val barcodeResults = result.getValue(barcodeScanner)
            if (barcodeResults.isNullOrEmpty())
                return@MlKitAnalyzer

            scannerPause = true

            if (barcodeResults.size > 1) {
                Log.w("QRCodeScannerCamera", "Multiple QR codes detected!")
            }

            val barcode = barcodeResults[0]

            previewView.overlay.clear() // For some reason this is needed for the overlay to show up
            previewView.overlay.add(QRCodeDrawable(barcode.boundingBox!!))

            barcode.rawBytes?.let {
                callbackExecutor.execute {
                    Thread.sleep(50) // Sleep for a moment so the user can see the scanned QR code bounding box
                    scanCallback(it)
                }
            }
        }
    }
}