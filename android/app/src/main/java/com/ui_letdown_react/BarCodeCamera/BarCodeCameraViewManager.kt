package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.ViewGroup
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.annotations.ReactPropGroup
import com.google.zxing.BarcodeFormat

class BarCodeCameraViewManager : SimpleViewManager<BarCodeCameraView>(), LifecycleEventListener {

    enum class Event(val value: String) {
        ON_BARCODE_READ("onBarCodeRead")
    }

    private lateinit var _view: BarCodeCameraView
    private lateinit var _context: ThemedReactContext
    private var _isPause = false

    override fun getName(): String = "BarCodeCameraView"

    override fun createViewInstance(reactContext: ThemedReactContext): BarCodeCameraView {
        _context = reactContext
        _context.addLifecycleEventListener(this)
        _view = BarCodeCameraView(reactContext)
        return _view
    }

    override fun onDropViewInstance(view: BarCodeCameraView) {
        Log.e("@@", "DropViewInstance")
        _view.stop()
        _context.removeLifecycleEventListener(this)

        super.onDropViewInstance(view)
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val builder = MapBuilder.builder<String, Any>()
        for (event in Event.values())
            builder.put(event.value, MapBuilder.of("registrationName", event.value))
        return builder.build()
    }

    // Always call because JS platform has set default with []
    @ReactProp(name = "barcodeTypes")
    fun setBarcodeTypes(view: BarCodeCameraView, codes: ReadableArray) {
        if (codes.size() > 0) {
            val result = ArrayList<BarcodeFormat>()
            for (select in codes.toArrayList()) {
                result.add(BarCodeCameraPackage.VALID_SUPPORT_BARCODE[select]!!)
            }
            if (result.size > 0)
                view.setBarCodeTypes(result)
        } else {
            view.setBarCodeTypes(BarCodeCameraPackage.VALID_SUPPORT_BARCODE.values.toList())
        }
    }

    @ReactProp(name = "flash")
    fun setBarcodeTypes(view: BarCodeCameraView, flash: String) {
        view.setFlash(flash)
    }

    // JS will handle wrong data format. This method will be considered perfect.
    // Crop data must follow Rect rules ( right > left, bottom > top )
    @ReactProp(name = "cropData")
    fun setCropData(view: BarCodeCameraView, cropData: String) {
        val parts = cropData.split(",").map { it.toInt() }

        val sensorCropRect = RectF(
                parts[0].toFloat(),
                parts[1].toFloat(),
                parts[0].toFloat() + parts[2].toFloat(),
                parts[1].toFloat() + parts[3].toFloat())

        view.setRectCrop(sensorCropRect)
    }

    @ReactMethod
    fun touchCrop(promise: Promise) {
        promise.resolve(1)
        Log.e("@@", "HAHA")
    }

    override fun onHostResume() {
        Log.e("@@", "onHostResume")
        if (_isPause) {
            _isPause = false
            _view.openCameraAgain()
        }
    }

    override fun onHostPause() {
        Log.e("@@", "onHostPause")
        _isPause = true
        _view.stop()
    }

    override fun onHostDestroy() {
        // Not call
    }
}