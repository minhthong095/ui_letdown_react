package com.ui_letdown_react.BarCodeCamera

import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

class BarCodeCameraViewManager : SimpleViewManager<BarCodeCameraView>() {

    enum class Event(val value: String) {
        ON_BARCODE_READ("onBarCodeRead")
    }


    override fun createViewInstance(reactContext: ThemedReactContext): BarCodeCameraView {
        return BarCodeCameraView(reactContext)
    }

    override fun onDropViewInstance(view: BarCodeCameraView) {
        view.stopClearly()
        super.onDropViewInstance(view)
    }

    override fun getName(): String = "BarCodeCameraView"

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val builder = MapBuilder.builder<String, Any>()
        for (event in Event.values())
            builder.put(event.value, MapBuilder.of("registrationName", event.value))
        return builder.build()
    }
}