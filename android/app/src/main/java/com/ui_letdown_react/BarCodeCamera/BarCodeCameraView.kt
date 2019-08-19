package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.AsyncTask
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.facebook.react.uimanager.ThemedReactContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
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
    private lateinit var _surfaceTexture: SurfaceTexture

    init {
        Log.e("@@", "INIT")
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
        Log.e("@@", "onSurfaceTextureAvailable w:$width,h:$height")
        if (surface != null)
            openCamera(surface, width, height)
    }

    fun setBarCodeTypes(codes: List<BarcodeFormat>) {
        val hints = HashMap<DecodeHintType, Any>(codes.size)
        hints[DecodeHintType.POSSIBLE_FORMATS] = codes
        _barcodeFormatReader.setHints(hints)
    }

    @SuppressLint("NewApi")
    private fun openCamera(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

        if (ContextCompat.checkSelfPermission(_context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return

        _manager = _context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (camId in _manager.cameraIdList) {
            try {
                val characteristics = _manager.getCameraCharacteristics(camId)
                val lenType = characteristics.get(CameraCharacteristics.LENS_FACING)

                if (lenType == null || lenType == CameraCharacteristics.LENS_FACING_FRONT)
                    continue

                _camId = camId
                _surfaceTexture = surfaceTexture

                configurePreviewSize()
                configureTransform(width, height)

                _surfaceTexture.setDefaultBufferSize(_previewSize.height, _previewSize.width)

                _manager.openCamera(_camId, _cameraStateCallback, null)
            } catch (er: CameraAccessException) {
                throw er
            }
        }
    }

    private fun configurePreviewSize() {
        val map = _manager.getCameraCharacteristics(_camId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val surfaceSizeSupport = map!!.getOutputSizes(SurfaceTexture::class.java)
        _previewSize = appropriateSize(width, height, surfaceSizeSupport)
    }

    // Must places after _previewSize has been configured
    private fun configureTransform(textureWidth: Int, textureHeight: Int) {
        val texture = RectF(0f, 0f, textureWidth.toFloat(), textureHeight.toFloat())
        val preview = RectF(0f, 0f, _previewSize.height.toFloat(), _previewSize.width.toFloat())

        // texture > preview
        if (texture.width() > preview.width() && texture.height() > preview.height()) {

            val matrix = Matrix()
            preview.offset(texture.centerX() - preview.centerX(), texture.centerY() - preview.centerY()) // Center

            // Read description you will understand.
            matrix.setRectToRect(texture, preview, Matrix.ScaleToFit.FILL)

            val scale = Math.max(
                    textureHeight.toFloat() / preview.height(),
                    textureWidth.toFloat() / preview.width())
            matrix.postScale(scale, scale, preview.centerX(), preview.centerY())

            setTransform(matrix)
        }
    }

    private fun startPreviewSession() {

        try {
            setupScanImageReader()
            val previewSurface = Surface(_surfaceTexture)
            val scanSurface = _scanImageReader?.surface
            _requestBuilder = _camera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            _requestBuilder.addTarget(previewSurface)

            _camera!!.createCaptureSession(mutableListOf(previewSurface, scanSurface), _sessionCallback, null)
        } catch (er: CameraAccessException) {
            throw er
        }
    }

    private fun setupScanImageReader() {
        _scanImageReader = ImageReader.newInstance(_previewSize.width, _previewSize.height, ImageFormat.JPEG, 1)
        _scanImageReader?.setOnImageAvailableListener(_imageReaderListener, null)
    }

    // For more information about this function go to https://github.com/googlesamples/android-Camera2Basic
    // With function name chooseOptimalSize
    private fun appropriateSize(textureWidth: Int, textureHeight: Int, choices: Array<Size>): Size {

        val bigger = arrayListOf<Size>()
        val smaller = arrayListOf<Size>()

        for (choice in choices) {
            // Max preview width, height that is guaranteed by Camera2 API
            if (choice.width <= 1920 && choice.height <= 1080) {
                // Choices in configuration map is known for width size larger than height size
                // And in this app we're only check with vertical so must be width < height
                if (choice.height >= textureWidth && choice.width >= textureHeight)
                    bigger.add(choice)
                else
                    smaller.add(choice)
            }
        }
        when {
            smaller.size > 0 -> return Collections.max(smaller, Conditioner())
            bigger.size > 0 -> return Collections.min(bigger, Conditioner())
        }

        return choices[0]
    }

    @SuppressLint("MissingPermission")
    fun openCameraAgain() {
        _manager.openCamera(_camId, _cameraStateCallback, null)
    }

    fun stop() {
        _session?.close()
        _session = null
        _camera?.close()
        _camera = null
        _taskBarCodeRead?.cancel(true)
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

    private val _imageReaderListener = ImageReader.OnImageAvailableListener { imageReader ->
        if (!_lockScan) {
            val imageScan = imageReader.acquireNextImage()
            val byteArray = ByteArray(imageScan.planes[0].buffer.remaining())

            val file = File(_context.externalCacheDir, "file.jpeg")
            file.createNewFile()

            _taskBarCodeRead = BarCodeAsyncTask(file, this, byteArray, imageScan.width, imageScan.height, _barcodeFormatReader).execute()
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
//        _lockScan = false
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