package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import java.util.*
import kotlin.Comparator

class BarCodeCameraView(private val _context: ThemedReactContext) : TextureView(_context), TextureView.SurfaceTextureListener, LifecycleEventListener {

    // Max preview width that is guaranteed by Camera2 API
    private val MAX_PREVIEW_WIDTH = 1920

    // Max preview height that is guaranteed by Camera2 API
    private val MAX_PREVIEW_HEIGHT = 1080

    private var _camera: CameraDevice? = null
    private var _session: CameraCaptureSession? = null
    private lateinit var _manager: CameraManager
    private lateinit var _requestBuilder: CaptureRequest.Builder

    init {
        _context.addLifecycleEventListener(this)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        openCamera(width, height)
    }

    override fun onHostResume() {
        if(isAvailable)
            openCamera(width, height)
        else {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            surfaceTextureListener = this
        }
    }

    override fun onHostPause() {
        stop()
    }

    override fun onHostDestroy() {
        stop()
        _context.removeLifecycleEventListener(this)
    }

    @SuppressLint("NewApi")
    private fun openCamera(width: Int, height: Int) {

        if (ContextCompat.checkSelfPermission(_context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("You must grant Camera permission.")
        }

        _manager = _context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (camId in _manager.cameraIdList) {
            val characteristics = _manager.getCameraCharacteristics(camId)
            val lenType = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lenType == null || lenType == CameraCharacteristics.LENS_FACING_FRONT)
                continue

            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val surfaceSizeSupport = map.getOutputSizes(SurfaceTexture::class.java)
            val bestSize = appropriateSize(width, height, surfaceSizeSupport)
            surfaceTexture.setDefaultBufferSize(bestSize.width, bestSize.height)

            try {
//                _manager.openCamera(camId, _stateCallback, null)
            } catch (er: CameraAccessException) {
                throw er
            }
        }
    }

    // For more information about this function go to https://github.com/googlesamples/android-Camera2Basic
    // With function name chooseOptimalSize
    private fun appropriateSize(textureWidth: Int, textureHeight: Int, choices: Array<Size>): Size {

        val bigger = arrayListOf<Size>()
        val smaller = arrayListOf<Size>()

        for (choice in choices) {
            if (choice.width <= MAX_PREVIEW_WIDTH && choice.height <= MAX_PREVIEW_HEIGHT) {
                if (choice.width >= textureWidth && choice.height >= textureHeight)
                    bigger.add(choice)
                else
                    smaller.add(choice)
            }
        }
        when {
            bigger.size > 0 -> return Collections.min(bigger, Conditioner())
            smaller.size > 0 -> return Collections.max(smaller, Conditioner())
        }

        return choices[0]
    }

    fun stop() {
        _session?.close()
        _session = null
        _camera?.close()
        _camera = null
    }

    private fun startPreviewSession() {

        if (_camera == null) return

        try {
            val surface = Surface(surfaceTexture)
            _requestBuilder = _camera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            _requestBuilder.addTarget(surface)

            _camera!!.createCaptureSession(mutableListOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {}

                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        _session = session
                        _requestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_BARCODE)
                        _session!!.setRepeatingRequest(_requestBuilder.build(), null, null)
                    } catch (e: CameraAccessException) { throw e }
                }
            }, null)
        } catch (er: CameraAccessException) {
            throw er
        }
    }

    private val _stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            _camera = camera
            startPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            _camera?.close()
            _camera = null

        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            _camera?.close()
            _camera = null
        }

    }

    private class Conditioner : Comparator<Size> {
        override fun compare(o1: Size?, o2: Size?): Int = o1!!.width * o1.height - o2!!.width * o2.height
    }
}