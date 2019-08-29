package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.AsyncTask
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.ui_letdown_react.BarCodeCamera.Event.OnBarCodeReadEvent
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class BarCodeCameraView(private val _context: ThemedReactContext) : TextureView(_context), BarCodeAsyncTaskDelegate, TextureView.SurfaceTextureListener {


    private val FLASH_INIT = "init"
    private val FLASH_ON = "on"
    private val FLASH_OFF = "off"

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
    private var _rawCropRect = Rect()
    private var _transformCropRect = Rect()
    private var _flashMode = FLASH_INIT
    private lateinit var _characteristics: CameraCharacteristics
    private val _exposureValue = -2

    init {
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
        if (surface != null)
            openCamera(surface, width, height)
    }

    // Raw crop rect, before converting to follow preview ratio
    fun setRectCrop(rawRect: Rect) {
        _rawCropRect = rawRect
    }

    fun setBarCodeTypes(codes: List<BarcodeFormat>) {
        val hints = HashMap<DecodeHintType, Any>(DecodeHintType.values().size)
        hints[DecodeHintType.POSSIBLE_FORMATS] = codes
        _barcodeFormatReader.setHints(hints)
    }

    fun setFlash(flash: String) {
        _flashMode = flash
        if (_flashMode == FLASH_INIT)
            return

        if (_flashMode != FLASH_INIT)
            setupFlash()

        if (_session != null) {
            try {
                _session!!.setRepeatingRequest(_requestBuilder.build(), null, null)
            } catch (e: CameraAccessException) {
                throw e
            }
        }
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
                _characteristics = characteristics

                configurePreviewSize(width, height)
                configureCropRect(width, height)
                transformCropRect()
                configureTransform(width, height)

                _manager.openCamera(_camId, _cameraStateCallback, null)
            } catch (er: CameraAccessException) {
                throw er
            }
        }
    }

    // Must be place after _requestBuilder has created
    private fun setupBarcodeScene() {
        val availableScenes: IntArray? = _characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
        if (availableScenes != null && availableScenes.contains(CaptureRequest.CONTROL_SCENE_MODE_BARCODE)) {
            _requestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE)
            _requestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE)
        }
    }

    // Range [0,0] indicates that exposure compensation is not supported.
    // Must be place after _requestBuilder
    private fun setupExposeCompensation() {
        val range = _characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
        val step = _characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
        if (step != null && range != null && range.lower != 0) {
            _requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            _requestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (_exposureValue * step.denominator) / step.numerator)
        }
    }

    // For lower brightness to easy detect shapes. Adjust right on camera sensor.
    // But have to hide it because Redmi Note 5 also not support REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR yet.
    private fun setupCustomAEMode() {
        val availableCapabilities = _characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        if (availableCapabilities != null && availableCapabilities.contains(CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR)) {
            val exporeTimeRange = _characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
            val isoRange = _characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
            val frameDurationMax = _characteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION)
        }
    }

    private fun setupFlash() {
        when (_flashMode) {
            FLASH_ON -> {
                _requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            }
            FLASH_OFF -> {
                _requestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            }
        }
    }

    // Must places after _previewSize has been configured
    private fun configureCropRect(textureWidth: Int, textureHeight: Int) {
        val widthScale = textureWidth.toFloat() / _previewSize.width.toFloat()
        val heightScale = textureHeight.toFloat() / _previewSize.height.toFloat()
        _rawCropRect.left = (_rawCropRect.left / widthScale).toInt()
        _rawCropRect.right = (_rawCropRect.right / widthScale).toInt()
        _rawCropRect.top = (_rawCropRect.top / heightScale).toInt()
        _rawCropRect.bottom = (_rawCropRect.bottom / heightScale).toInt()
    }

    private fun configurePreviewSize(textureWidth: Int, textureHeight: Int) {
        val map = _characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val surfaceSizeSupport = map!!.getOutputSizes(SurfaceTexture::class.java)
        _previewSize = getAppropriateSize(textureWidth, textureHeight, surfaceSizeSupport)
    }

    // Must places after _previewSize and configureCropRect.
    private fun transformCropRect() {
        _transformCropRect = Rect(
                _rawCropRect.top,
                _previewSize.width - _rawCropRect.right - _rawCropRect.left,
                _rawCropRect.bottom,
                _rawCropRect.right
        )
    }

    // Must places after _previewSize has been configured
    private fun configureTransform(textureWidth: Int, textureHeight: Int) {
        val texture = RectF(0f, 0f, textureWidth.toFloat(), textureHeight.toFloat())
        val preview = RectF(0f, 0f, _previewSize.width.toFloat(), _previewSize.height.toFloat())

        // texture > preview
        if (texture.width() >= preview.width() && texture.height() >= preview.height()) {

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
            // Preview size follow through screen
            // But SDK function is not follow with device screen.
            // So larger size belong to width
            // Put here to prevent Redmi Note 5 onResume bent the preview camera.
            _surfaceTexture.setDefaultBufferSize(_previewSize.height, _previewSize.width)
            setupScanImageReader()
            val previewSurface = Surface(_surfaceTexture)
            val scanSurface = _scanImageReader?.surface
            _requestBuilder = _camera!!.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
            _requestBuilder.addTarget(previewSurface)
            _requestBuilder.addTarget(scanSurface!!)

            setupFlash()
            setupBarcodeScene()
            setupExposeCompensation()
            setupZoom()

            _camera!!.createCaptureSession(mutableListOf(previewSurface, scanSurface), _sessionCallback, null)

        } catch (er: CameraAccessException) {
            throw er
        }
    }

    private fun setupZoom() {
        val characteristic = _manager.getCameraCharacteristics(_camId)
        val rectArray = characteristic.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val max = characteristic.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        if (rectArray != null && max != null) {
            val cropW = rectArray.width() / 2
            val cropH = rectArray.height() / 2
            val rectCrop = Rect(0, 0, cropW, cropH)
            val centerX = rectArray.centerX()
            val centerXCrop = rectCrop.centerX()
            val centerY = rectArray.centerY()
            val centerYCrop = rectCrop.centerY()
            rectCrop.offsetTo(centerX - centerXCrop, centerY - centerYCrop)
            _requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, Rect(0, 0, cropW, cropH))
        }
    }

    private fun setupScanImageReader() {
        _scanImageReader = ImageReader.newInstance(_previewSize.height, _previewSize.width, ImageFormat.YUV_420_888, 1)
        _scanImageReader?.setOnImageAvailableListener(_imageReaderListener, null)
    }

    // For more information about this function go to https://github.com/googlesamples/android-Camera2Basic
    // With function name chooseOptimalSize
    private fun getAppropriateSize(textureWidth: Int, textureHeight: Int, choices: Array<Size>): Size {

        val bigger = arrayListOf<Size>()
        val smaller = arrayListOf<Size>()

        for (choice in choices) {
            // Max preview width, height that is guaranteed by Camera2 API
            if (choice.width <= 1920 && choice.height <= 1080) {
                // Choices in configuration map is known for width size larger than height size
                // And in this app we're only check with vertical so must be width < height
                if (choice.width >= textureHeight && choice.height >= textureWidth)
                    bigger.add(Size(choice.height, choice.width))
                else
                    smaller.add(Size(choice.height, choice.width))
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

    private fun imageToLuminance(image: Image): ByteArray {
        val yBuffer = image.planes[0].buffer

        // nexus h:480 w:640 _previewSize.height, _previewSize.width | work
        // nexus h:640 w:480 _previewSize.width, _previewSize.height | work
        // redmi note 2 h:1080 x w: 1440  _previewSize.height, _previewSize.width | work in rmnote2
        // redmi note 2 h: x w:   _previewSize.width, _previewSize.width | not work in rmnote2
        val yBytes = ByteArray(yBuffer.capacity())
        yBuffer.get(yBytes, 0, yBuffer.capacity())

        val yChanel = ByteArray(3 * yBuffer.capacity() / 2)

        var lastPos = 0
        for (row in _transformCropRect.top until _transformCropRect.top + _transformCropRect.bottom) {
            System.arraycopy(yBytes, row * image.width + _transformCropRect.left, yChanel, lastPos, _transformCropRect.right)
            lastPos += _transformCropRect.right
        }

        return yChanel
    }

    private fun imageToI420(image: Image): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val i420 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(i420, 0, ySize)
//        uBuffer.get(i420, ySize, uSize)
//        vBuffer.get(i420, ySize + uSize, vSize)

        return i420
    }

    private val _imageReaderListener = ImageReader.OnImageAvailableListener { imageReader ->
        if (!_lockScan) {
            val imageScan = imageReader.acquireNextImage()
            val byteArray = imageToLuminance(imageScan)

            // Description like setDefaultBufferSize
            _taskBarCodeRead = BarCodeAsyncTask(this, byteArray, _transformCropRect.bottom, _transformCropRect.right, _barcodeFormatReader).execute()
            imageScan.close()
        } else {
            imageReader.acquireNextImage().close()
        }
    }

    override fun onPreBarCodeRead() {
        _lockScan = true
    }

    override fun onBarCodeRead(result: String?) {
        _lockScan = false
        if (result != null) {
            _context
                    .getNativeModule(UIManagerModule::class.java)
                    .eventDispatcher
                    .dispatchEvent(OnBarCodeReadEvent(id, result))
        }
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


    fun stop() {
        _session?.close()
        _session = null
        _camera?.close()
        _camera = null
        _taskBarCodeRead?.cancel(true)
        _scanImageReader?.close()
    }

    private class Conditioner : Comparator<Size> {
        override fun compare(o1: Size?, o2: Size?): Int = o1!!.width * o1.height - o2!!.width * o2.height
    }
}