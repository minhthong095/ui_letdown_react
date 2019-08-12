package com.ui_letdown_react.BarCodeCamera

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.google.zxing.BarcodeFormat

class BarCodeCameraViewManager : SimpleViewManager<BarCodeCameraView>() {

    enum class Event(val value: String) {
        ON_BARCODE_READ("onBarCodeRead")
    }

    override fun getName(): String = "BarCodeCameraView"

    override fun createViewInstance(reactContext: ThemedReactContext): BarCodeCameraView {
        return BarCodeCameraView(reactContext)
    }

    override fun onDropViewInstance(view: BarCodeCameraView) {
        view.stopClearly()
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
}