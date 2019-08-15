package com.ui_letdown_react.BarCodeCamera

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
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