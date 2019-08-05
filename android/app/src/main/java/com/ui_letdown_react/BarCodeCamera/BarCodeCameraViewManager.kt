package com.ui_letdown_react.BarCodeCamera

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager

class BarCodeCameraViewManager : SimpleViewManager<BarCodeCameraView>() {

    private lateinit var _barcodeView: BarCodeCameraView

    override fun createViewInstance(reactContext: ThemedReactContext): BarCodeCameraView {
        _barcodeView = BarCodeCameraView(reactContext)
        return _barcodeView
    }

    override fun getName(): String = "BarCodeCameraView"
}