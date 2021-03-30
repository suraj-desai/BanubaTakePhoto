package com.banuba.sdk.example.effect_player_realtime_preview

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.banuba.sdk.effect_player.Effect
import com.banuba.sdk.manager.BanubaSdkManager
import com.banuba.sdk.manager.BanubaSdkTouchListener
import com.banuba.sdk.entity.RecordedVideoInfo
import com.banuba.sdk.entity.ContentRatioParams
import kotlinx.android.synthetic.main.activity_apply_mask.*
import kotlinx.android.synthetic.main.activity_camera_preview.surfaceView
import com.banuba.sdk.manager.IEventCallback
import com.banuba.sdk.types.Data


/**
 * Sample activity that shows how to apply masks with Banuba SDK.
 * Some Banuba masks can change their appearance if tapping on them.
 */
class MaskActivity : AppCompatActivity() {

    companion object {
        private const val MASK_NAME = "UnluckyWitch"

        private const val REQUEST_CODE_APPLY_MASK_PERMISSION = 1001

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
//    private val banubaSdkManager by lazy(LazyThreadSafetyMode.NONE) {
//        BanubaSdkManager(applicationContext)
//    }
    private val banubaSdkManager by lazy(LazyThreadSafetyMode.NONE) {
    Log.d("Icalled", "Ievent  called")
        BanubaSdkManager(applicationContext).apply {
            setCallback(banubaEventCallback)
        }
    }
    private val banubaEventCallback = object : IEventCallback {

        override fun onVideoRecordingFinished(videoInfo: RecordedVideoInfo) {
            Log.d("Hello", "Video recording finished. Recorded file = ${videoInfo.filePath}," +
                    "duration = ${videoInfo.recordedLength}")
        }
        override fun onVideoRecordingStatusChange(isStarted: Boolean) {
            Log.d("RecChanged", "Video recording status changed. isRecording")
        }

        override fun onCameraOpenError(e: Throwable) {
            // Implement custom error handling here
        }

        override fun onImageProcessed(imageBitmpa: Bitmap) {}
//
        override fun onEditingModeFaceFound(faceFound: Boolean) {}

        override fun onHQPhotoReady(photoBitmap: Bitmap) {}

        override fun onEditedImageReady(imageBitmap: Bitmap) {}

        override fun onFrameRendered(data: Data, width: Int, height: Int) {}

        override fun onScreenshotReady(screenshotBitmap: Bitmap) {
            Log.d("photo11","photo called")
        }

        override fun onCameraStatus(isOpen: Boolean) {}
    }
    private val maskUri by lazy(LazyThreadSafetyMode.NONE) {
        Uri.parse(BanubaSdkManager.getResourcesBase())
                .buildUpon()
                .appendPath("effects")
                .appendPath(MASK_NAME)
                .build()
    }

    private var shouldApply = false
    private var effect: Effect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apply_mask)

        // Set custom OnTouchListener to change mask style.
        surfaceView.setOnTouchListener(BanubaSdkTouchListener(this, banubaSdkManager.effectPlayer))

//        var crp = ContentRatioParams(100,100,true);
//        banubaSdkManager.takePhoto(crp);

        showMaskButton.setOnClickListener {
            shouldApply = !shouldApply

            updateUIState()

            if (shouldApply) {
                // The mask is loaded asynchronously and applied
                Handler().postDelayed(Runnable {
                    var crp = ContentRatioParams(100, 100, true);
                    banubaSdkManager.takePhoto(crp)
                    Log.d("takephoto", "takephoto  called")
                }, 500)
                effect = banubaSdkManager.effectManager.loadAsync(maskUri.toString())
            } else {
                // The mask is unloaded
                banubaSdkManager.effectManager.unload(effect)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        banubaSdkManager.attachSurface(surfaceView)

        if (allPermissionsGranted()) {
            banubaSdkManager.openCamera()


        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_APPLY_MASK_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            results: IntArray
    ) {
        if (requireAllPermissionsGranted(permissions, results)) {
            banubaSdkManager.openCamera()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        banubaSdkManager.effectPlayer.playbackPlay()
    }

    override fun onPause() {
        super.onPause()
        banubaSdkManager.effectPlayer.playbackPause()
    }

    override fun onStop() {
        super.onStop()
        banubaSdkManager.releaseSurface()
        banubaSdkManager.closeCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateUIState() {
        showMaskButton.text = if (shouldApply) {
            getString(R.string.hide_mask)
        } else {
            getString(R.string.show_mask)
        }

        maskStyleView.visibility = if (shouldApply) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}