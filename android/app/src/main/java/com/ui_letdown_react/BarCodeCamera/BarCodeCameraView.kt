package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
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
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.CameraCharacteristics
import android.util.Log


class BarCodeCameraView(private val _context: ThemedReactContext) : TextureView(_context), BarCodeAsyncTaskDelegate, TextureView.SurfaceTextureListener {

    private val FLASH_INIT = "init"
    private val FLASH_ON = "on"
    private val FLASH_OFF = "off"

    private var _camera: CameraDevice? = null
    private var _session: CameraCaptureSession? = null
    private lateinit var _manager: CameraManager
    private lateinit var _previewBuilder: CaptureRequest.Builder
    private lateinit var _previewSize: Size
    private var _camId: String = ""
    private var _lockScan: Boolean = false
    private var _taskBarCodeRead: AsyncTask<Void, Void, String>? = null
    private val _barcodeFormatReader = MultiFormatReader()
    private lateinit var _surfaceTexture: SurfaceTexture
    private var _rawCropRect = Rect()
    private var _configureCropRect = Rect()
    private var _flashMode = FLASH_INIT
    private lateinit var _characteristics: CameraCharacteristics
    private val _exposureValue = -2
    private val _cropRegionOnSensor = Rect()

    init {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        if (!_lockScan) {
//            val file = File(_context.externalCacheDir, "zoom.jpg")
//            file.createNewFile()
            _taskBarCodeRead = BarCodeRGBAsyncTask(_configureCropRect, this, getBitmap(_previewSize.width, _previewSize.height), _barcodeFormatReader).execute()
        }
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
            integrateFlash()

        sendRepeatingRequest()
    }

    fun touchCrop() {
        integrateIdleAeAf()
        sendRepeatingRequest()

        integrateTriggerAeAf()
        sendCaptureRequest()
    }

    private fun integrateTriggerAeAf() {
        _previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO)
        _previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
        _previewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
    }

    private fun integrateIdleAeAf() {
        _previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE)
        _previewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE)
    }

    private fun integrateTouchCrop() {
        val _cropRegionOnSensorX = Rect(0,0,100,100)
        val arrayMetering = arrayOf(MeteringRectangle(_cropRegionOnSensorX, 300))
        _previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        _previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        _previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
        _previewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayMetering)
        _previewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayMetering)
    }

    private val mPreviewCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed( session: CameraCaptureSession,
        request: CaptureRequest,  partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
            Log.e("@@", "Progressed $partialResult")
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
        request: CaptureRequest,  result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            Log.e("@@", "onCaptureCompleted " + result.get(CaptureResult.CONTROL_AF_STATE))
            when (result.get(CaptureResult.CONTROL_AF_STATE)) {

                CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED,
                CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED
                -> {
                    integrateIdleAeAf()
                    sendRepeatingRequest()

                    _previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    sendRepeatingRequest()
                }
            }
        }

        override fun onCaptureFailed( session: CameraCaptureSession,
        request: CaptureRequest,  failure: CaptureFailure) {
            super.onCaptureFailed(session, request, failure)
            Log.e("@@", "onCaptureFailed $request")
        }
    }

    private fun sendRepeatingRequest() {
        if (_session != null) {
            try {
                _session!!.setRepeatingRequest(_previewBuilder.build(), mPreviewCallback, null)
            } catch (e: CameraAccessException) {
                throw e
            }
        }
    }

    private fun sendCaptureRequest() {
        if (_session != null) {
            try {
                _session!!.capture(_previewBuilder.build(), null, null)
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
                configureTransform(width, height)
                configureAfAeRegion(width, height)

                _manager.openCamera(_camId, _cameraStateCallback, null)
            } catch (er: CameraAccessException) {
                throw er
            }
        }
    }

    // Range [0,0] indicates that exposure compensation is not supported.
    // Must be place after _previewBuilder
    private fun setupExposeCompensation() {
        val range = _characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
        val step = _characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
        if (step != null && range != null && range.lower != 0) {
            _previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            _previewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (_exposureValue * step.denominator) / step.numerator)
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

    private fun integrateFlash() {
        when (_flashMode) {
            FLASH_ON -> {
                _previewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
            }
            FLASH_OFF -> {
                _previewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
            }
        }
    }

    // Must places after _previewSize has been configured.
    // From raw rect on TextureView into convert rect in preview scale.
    private fun configureCropRect(textureWidth: Int, textureHeight: Int) {
        val widthScale = textureWidth.toFloat() / _previewSize.width.toFloat()
        val heightScale = textureHeight.toFloat() / _previewSize.height.toFloat()
        _configureCropRect.left = (_rawCropRect.left / widthScale).toInt()
        _configureCropRect.right = (_rawCropRect.right / widthScale).toInt()
        _configureCropRect.top = (_rawCropRect.top / heightScale).toInt()
        _configureCropRect.bottom = (_rawCropRect.bottom / heightScale).toInt()
    }

    private fun configureAfAeRegion(textureWidth: Int, textureHeight: Int) {

        val sensorActiveArray = _characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)

        if (sensorActiveArray != null) {
            val xPercent = _rawCropRect.left * 100 / textureWidth
            val yPercent = _rawCropRect.top * 100 / textureHeight
            val widthPercent = _rawCropRect.right * 100 / textureHeight
            val heightPercent = _rawCropRect.top * 100 / textureHeight
            _cropRegionOnSensor.left = xPercent * sensorActiveArray.left / 100
            _cropRegionOnSensor.top = yPercent * sensorActiveArray.top / 100
            _cropRegionOnSensor.right = widthPercent * sensorActiveArray.right / 100
            _cropRegionOnSensor.bottom = heightPercent * sensorActiveArray.bottom / 100
        }
    }

    private fun configurePreviewSize(textureWidth: Int, textureHeight: Int) {
        val map = _characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val surfaceSizeSupport = map!!.getOutputSizes(SurfaceTexture::class.java)
        _previewSize = getAppropriateSize(textureWidth, textureHeight, surfaceSizeSupport)
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
            val previewSurface = Surface(_surfaceTexture)
            _previewBuilder = _camera!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            _previewBuilder.addTarget(previewSurface)

            _camera!!.createCaptureSession(mutableListOf(previewSurface), _sessionCallback, null)

        } catch (er: CameraAccessException) {
            throw er
        }
    }

    private fun integrateZoom() {
        val rectArray = _characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val max = _characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        if (rectArray != null && max != null) {
            // 0.3 base on max, 0.5 = max / 2
            val scaledZoom = 0.1 * (max - 1.0f) + 1.0f
            val rectCrop = Rect(0, 0, (rectArray.width() / scaledZoom).toInt(), (rectArray.height() / scaledZoom).toInt())
            rectCrop.offsetTo(rectArray.centerX() - rectCrop.centerX(), rectArray.centerY() - rectCrop.centerY())
            _previewBuilder.set(CaptureRequest.SCALER_CROP_REGION, rectCrop)
        }
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
                integrateFlash()
                integrateZoom()
                integrateTouchCrop()
                sendRepeatingRequest()
            } catch (e: CameraAccessException) {
                throw e
            }
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
    }

    private class Conditioner : Comparator<Size> {
        override fun compare(o1: Size?, o2: Size?): Int = o1!!.width * o1.height - o2!!.width * o2.height
    }
}