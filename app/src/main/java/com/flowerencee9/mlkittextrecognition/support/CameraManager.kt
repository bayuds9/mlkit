package com.flowerencee9.mlkittextrecognition.support

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val finderView: PreviewView,
    private val lifecycleOwner: LifecycleOwner,
    private val captureListener: (Uri) -> Unit
) {
    private var preview: Preview? = null

    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var outputDirectory: File

    private var isFlashAvailable: Boolean = false
    private var flashOn = false

    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null

    companion object {
        private const val PHOTO_EXTENSION = ".jpeg"
        private const val FILENAME = "Captured"
        private val TAG = CameraManager::class.java.simpleName
    }

    init {
        cameraSelectorOption = CameraSelector.LENS_FACING_BACK
        createNewExecutor()
    }

    private fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        outputDirectory = getOutputDirectory(context)
    }

    private fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .build()

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetRotation(Surface.ROTATION_0)
                    .build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

                setCameraConfig(cameraProvider, cameraSelector)

            }, ContextCompat.getMainExecutor(context)
        )
    }

    private fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {
        val viewPort = ViewPort.Builder(
            Rational(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ), Surface.ROTATION_0
        ).build()
        val group = UseCaseGroup.Builder()
            .addUseCase(preview!!)
            .addUseCase(imageAnalyzer!!)
            .addUseCase(imageCapture!!)
            .setViewPort(viewPort)
            .build()

        try {
            cameraProvider?.unbindAll()

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                group
            )
            preview?.setSurfaceProvider(
                finderView.surfaceProvider
            )
            isFlashAvailable = camera?.cameraInfo?.hasFlashUnit() ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    fun isFlashOn(): Boolean = flashOn

    fun flashControl(states: Boolean) {
        if (isFlashAvailable) {
            camera?.cameraControl?.enableTorch(states)
            flashOn = states
        }
    }

    fun freezeCamera() {
        cameraProvider?.unbind(preview)
    }

    fun deleteImage() {
        val photoFile = createFile(outputDirectory, FILENAME + PHOTO_EXTENSION)
        if (photoFile.exists()) photoFile.delete()
    }

    fun captureImage() {
        imageCapture?.let { imageCapture: ImageCapture ->
            val photoFile = createFile(outputDirectory, FILENAME + PHOTO_EXTENSION)
            val metaData = ImageCapture.Metadata()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metaData)
                .build()

            val capture = object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                    captureListener(savedUri)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "Photo capture failed ; ${exception.message}")
                }

            }
            imageCapture.takePicture(outputOptions, cameraExecutor, capture)
        }
    }

    private fun createFile(baseFolder: File, formatFile: String) =
        File(
            baseFolder, formatFile
        )
}