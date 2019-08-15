package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.YuvImage
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.AsyncTask
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class BarCodeCameraView(private val _context: ThemedReactContext) : TextureView(_context), BarCodeAsyncTaskDelegate, TextureView.SurfaceTextureListener {

    private var _camera: CameraDevice? = null
    private var _session: CameraCaptureSession? = null
    private lateinit var _manager: CameraManager
    private lateinit var _requestBuilder: CaptureRequest.Builder
    private var _scanImageReader: ImageReader? = null
    private lateinit var _previewSize: Size
    private var _camId: String = ""
    private var _lockScan: Boolean = false
    private var _taskBarCodeRead: AsyncTask<Void, Void, String>? = null
    private val _barcodeFormatReader = MultiFormatReader()
    private var _threadCamera: HandlerThread? = null
    private var _handlerCamera: Handler? = null

    init {
        Log.e("@@", "INIT")
        startCameraThread()
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Log.e("@@", "onSurfaceTextureAvailable")
        openCamera(surface!!, width, height)
    }

    fun setBarCodeTypes(codes: List<BarcodeFormat>) {
        val hints = HashMap<DecodeHintType, Any>(codes.size)
        hints[DecodeHintType.POSSIBLE_FORMATS] = codes
        _barcodeFormatReader.setHints(hints)
    }

    @SuppressLint("NewApi")
    private fun openCamera(ssurfaceTexture: SurfaceTexture, width: Int, height: Int) {

        if (ContextCompat.checkSelfPermission(_context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return

        _manager = _context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (camId in _manager.cameraIdList) {
            try {
                val characteristics = _manager.getCameraCharacteristics(camId)
                val lenType = characteristics.get(CameraCharacteristics.LENS_FACING)
                val maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
                val sensorArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
                val infoArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val sensorInfoPhysic = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                if (lenType == null || lenType == CameraCharacteristics.LENS_FACING_FRONT)
                    continue

                _camId = camId
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val surfaceSizeSupport = map!!.getOutputSizes(SurfaceTexture::class.java)
                _previewSize = appropriateSize(width, height, surfaceSizeSupport)
//                surfaceTexture.setDefaultBufferSize(_previewSize.height, _previewSize.width)
                surfaceTexture.setDefaultBufferSize(50, 50)
                _manager.openCamera(_camId, _cameraStateCallback, _handlerCamera)
            } catch (er: CameraAccessException) {
                throw er
            }
        }
    }

    private fun setupScanImageReader() {
//        _scanImageReader = ImageReader.newInstance(_previewSize.width, _previewSize.height, ImageFormat.YUV_420_888, 1)
//        _scanImageReader?.setOnImageAvailableListener(_imageReaderListener, null)
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

    private fun startCameraThread() {
        _threadCamera = HandlerThread("ThreadCamera")
        _threadCamera!!.start()
        _handlerCamera = Handler(_threadCamera!!.looper)
    }

    private fun stopCameraThread() {
        _threadCamera?.quitSafely()
        try {
            _threadCamera?.join()
            _threadCamera = null
            _handlerCamera = null
        } catch (e: InterruptedException) { }
    }

    @SuppressLint("MissingPermission")
    fun openCameraAgain() {
        startCameraThread()
        _manager.openCamera(_camId, _cameraStateCallback, _handlerCamera)
    }

    fun stop() {
        _session?.close()
        _session = null
        _camera?.close()
        _camera = null
        stopCameraThread()
        _taskBarCodeRead?.cancel(true)
    }

    private fun startPreviewSession() {

        if (_camera == null) return

        try {
            setupScanImageReader()
            val previewSurface = Surface(surfaceTexture)
//            val scanSurface = _scanImageReader?.surface
            _requestBuilder = _camera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            val scalerCropRegion = _requestBuilder.get(CaptureRequest.SCALER_CROP_REGION)
            _requestBuilder.addTarget(previewSurface)
//            if (scanSurface != null)
//                _requestBuilder.addTarget(scanSurface)

            _camera!!.createCaptureSession(mutableListOf(previewSurface), _sessionCallback, _handlerCamera)
        } catch (er: CameraAccessException) {
            throw er
        }
    }

    private val _sessionCallback = object : CameraCaptureSession.StateCallback() {

        override fun onConfigureFailed(session: CameraCaptureSession) {}

        override fun onConfigured(session: CameraCaptureSession) {
            try {
                _session = session
                _session!!.setRepeatingRequest(_requestBuilder.build(), null, _handlerCamera)
            } catch (e: CameraAccessException) {
                throw e
            }
        }
    }

    private val _imageReaderListener = ImageReader.OnImageAvailableListener { imageReader ->
        if (!_lockScan) {
            val imageScan = imageReader.acquireNextImage()
            val byteArray = ByteArray(imageScan.planes[0].buffer.remaining())

            _taskBarCodeRead = BarCodeAsyncTask(this, byteArray, imageScan.width, imageScan.height, _barcodeFormatReader).execute()
            imageScan.close()
        } else {
            imageReader.acquireNextImage().close()
        }
    }

    override fun onPreBarCodeRead() {
        Log.e("@@", "===")
        Log.e("@@", "Scan Lock")
        _lockScan = true
    }

    override fun onBarCodeRead(result: String?) {
        Log.e("@@", "Scan UnLock")
        _lockScan = false
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