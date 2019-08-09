package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import java.util.*
import kotlin.Comparator

class BarCodeCameraView(private val _context: ThemedReactContext) : TextureView(_context), BarCodeAsyncTaskDelegate, TextureView.SurfaceTextureListener, LifecycleEventListener {

    private var _camera: CameraDevice? = null
    private var _session: CameraCaptureSession? = null
    private lateinit var _manager: CameraManager
    private lateinit var _requestBuilder: CaptureRequest.Builder
    private var _lockBarCodeReadTask: Boolean = false // May use with volatile
    private var _lockOpenCamera: Boolean = false // May use with volatile
    private lateinit var _surfaceTextureSizeAvailable: Size
    private lateinit var _surfaceTextureAvailable: SurfaceTexture
    private var _scanImageReader: ImageReader? = null

    init {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        _context.addLifecycleEventListener(this)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        if (!_lockBarCodeReadTask) {
            _lockBarCodeReadTask = true
            BarCodeAsyncTask(this).execute()
        }
    }

    override fun onBarCodeRead(result: String) {
        _lockBarCodeReadTask = false
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        stop()
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        _surfaceTextureSizeAvailable = Size(width, height)
        _surfaceTextureAvailable = surface!!
        openCamera()
    }

    private fun setupScanImageReader(bestSize: Size) {
        _scanImageReader?.close()
        _scanImageReader = ImageReader.newInstance(bestSize.width, bestSize.height, ImageFormat.YUV_420_888, 1)
        _scanImageReader?.setOnImageAvailableListener(_imageAvailableListener, null)
    }

    @SuppressLint("NewApi")
    private fun openCamera() {

        if (_lockOpenCamera)
            return

        _lockOpenCamera = true

        if (ContextCompat.checkSelfPermission(_context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return

        _manager = _context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (camId in _manager.cameraIdList) {
            try {
                val characteristics = _manager.getCameraCharacteristics(camId)
                val lenType = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (lenType == null || lenType == CameraCharacteristics.LENS_FACING_FRONT)
                    continue

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val surfaceSizeSupport = map!!.getOutputSizes(SurfaceTexture::class.java)
                val bestSize = appropriateSize(_surfaceTextureSizeAvailable.width, _surfaceTextureSizeAvailable.height, surfaceSizeSupport)
                setupScanImageReader(bestSize)
                _surfaceTextureAvailable.setDefaultBufferSize(bestSize.width, bestSize.height)

                _manager.openCamera(camId, _cameraStateCallback, null)
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
            // Max preview width, height that is guaranteed by Camera2 API
            if (choice.width <= 1920 && choice.height <= 1080) {
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

    override fun onHostResume() {
        if (surfaceTextureListener != null)
            openCamera()
        else
            surfaceTextureListener = this
    }

    override fun onHostPause() {
        stop()
    }

    override fun onHostDestroy() {
        stopClearly()
    }

    fun stop() {
        _session?.close()
        _session = null
        _camera?.close()
        _camera = null
        _lockOpenCamera = false
    }

    fun stopClearly() {
        stop()
        _context.removeLifecycleEventListener(this)
    }

    private fun startPreviewSession() {

        if (_camera == null) return

        try {
            val previewSurface = Surface(surfaceTexture)
            _requestBuilder = _camera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            _requestBuilder.addTarget(previewSurface)
            _requestBuilder.addTarget(_scanImageReader?.surface)

            _requestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_BARCODE)

            _camera!!.createCaptureSession(mutableListOf(previewSurface, _scanImageReader?.surface), _sessionCallback, null)
        } catch (er: CameraAccessException) {
            throw er
        }
    }

    private val _sessionCallback = object : CameraCaptureSession.StateCallback() {

        override fun onConfigureFailed(session: CameraCaptureSession) {}

        override fun onConfigured(session: CameraCaptureSession) {
            try {
                _session = session
                _session!!.setRepeatingRequest(_requestBuilder.build(), null, null)
            } catch (e: CameraAccessException) {
                throw e
            }
        }
    }

    private val _imageAvailableListener = ImageReader.OnImageAvailableListener {
        println("LL")
    }

    private val _cameraStateCallback = object : CameraDevice.StateCallback() {
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