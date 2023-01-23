package com.flowerencee9.mlkittextrecognition.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.flowerencee9.mlkittextrecognition.R
import com.flowerencee9.mlkittextrecognition.databinding.ActivityMainBinding
import com.flowerencee9.mlkittextrecognition.databinding.LayoutPopupPermissionRequireBinding
import com.flowerencee9.mlkittextrecognition.result.ScannResultActivity
import com.flowerencee9.mlkittextrecognition.support.CameraManager
import com.flowerencee9.mlkittextrecognition.support.setVisible
import com.flowerencee9.mlkittextrecognition.support.showLoadingDialog
import com.flowerencee9.mlkittextrecognition.support.showPopupAction
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var cameraManager: CameraManager
    private var inputImage: InputImage? = null

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        cameraManager =
            CameraManager(this@MainActivity, binding.previewViewFinder, this@MainActivity) { uri ->
                Handler(Looper.getMainLooper()).post {
                    onSuccessCapture(uri)
                }
            }
        setContentView(binding.root)
        setupView()
        resultObserver()
    }

    override fun onResume() {
        super.onResume()
        checkForPermission()
    }

    override fun onPause() {
        super.onPause()
        cameraManager.freezeCamera()
    }

    private fun onSuccessCapture(uri: Uri) {
        inputImage = InputImage.fromFilePath(this, uri)
        binding.imgResource.setImageURI(uri)
        binding.cameraContainer.visibility = View.GONE
        binding.btnRead.visibility = View.VISIBLE
        binding.btnRetake.visibility = View.VISIBLE
        cameraManager.freezeCamera()
        setupButtonStates()
    }

    private fun checkForPermission() {
        when {
            allPermissionsGranted() -> cameraManager.startCamera()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            ) -> showPopupForcePermission()
            else -> requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    private fun showPopupForcePermission() {
        val dialog = Dialog(this, R.style.DialogSlideAnimFullWidth)
        val popupBinding = LayoutPopupPermissionRequireBinding.bind(
            layoutInflater.inflate(
                R.layout.layout_popup_permission_require,
                null
            )
        )
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(popupBinding.root)
            setCancelable(false)
        }
        popupBinding.btnOk.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri =
                Uri.fromParts(
                    "package",
                    this.packageName,
                    null
                )
            intent.data = uri
            startActivity(intent)
            dialog.dismiss()
        }
        dialog.show()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.CAMERA] ?: false -> cameraManager.startCamera()
            else -> {}
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun resultObserver() {
        viewModel.success.observe(this) {
            Log.d(TAG, "read text success $it")
        }
        viewModel.resultText.observe(this) {
            Log.d(TAG, "result text $it")
        }

        viewModel.progress.observe(this) {
            if (it && !loadingDialog.isShowing) loadingDialog.show()
            else {
                loadingDialog.dismiss()
                if (viewModel.success.value == true) {
                    gotoResult(viewModel.resultText.value.toString())
                } else {
                    showPopupAction(
                        viewModel.message.value.toString(),
                        viewModel.resultText.value.toString()) {
                        gotoResult("")
                    }
                }
            }
        }
    }

    private fun gotoResult(value: String) {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                ScannResultActivity.myIntent(
                    this,
                    value
                )
            )
        }, 500)
    }

    private fun setupView() {
        with(binding) {
            loadingDialog = showLoadingDialog(root)
            btnRead.setOnClickListener {
                inputImage?.let {
                    viewModel.readText(it)
                }
            }
            btnFlash.apply {
                setOnClickListener {
                    when (cameraManager.isFlashOn()) {
                        true -> cameraManager.flashControl(false)
                        else -> cameraManager.flashControl(true)
                    }
                    isActivated = cameraManager.isFlashOn()
                }
            }
            btnAlbum.setOnClickListener {
                openGallery()
            }
            btnCapture.setOnClickListener {
                cameraManager.captureImage()
            }
            btnRetake.apply {
                setOnClickListener {
                    cameraManager.startCamera()
                    cameraManager.deleteImage()
                    cameraContainer.setVisible(true)
                    imgResource.setImageURI(null)
                    inputImage = null
                    setupButtonStates()
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        cameraManager.flashControl(false)
        binding.btnFlash.isActivated = false
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Handler(Looper.getMainLooper()).post {
                val selectedImg = result.data?.data as Uri
                onSuccessCapture(selectedImg)
            }
        }
    }

    private fun setupButtonStates() {
        with(binding) {
            btnRead.apply {
                setVisible(inputImage != null)
                isEnabled = inputImage != null
            }
            btnRetake.setVisible(inputImage != null)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}