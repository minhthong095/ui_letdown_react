package com.ui_letdown_react.BarCodeCamera

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.NativeViewHierarchyManager
import com.facebook.react.uimanager.UIBlock
import com.facebook.react.uimanager.UIManagerModule

class BarCodeCameraModule(
        private val _context: ReactApplicationContext
): ReactContextBaseJavaModule(_context) {

    override fun getName() = "BarCodeCameraModule"

    @ReactMethod
    private fun touchCrop(viewTag: Int) {
        val uiManager = _context.getNativeModule(UIManagerModule::class.java)
        uiManager.addUIBlock{ nativeViewHierarchyManager ->
            (nativeViewHierarchyManager.resolveView(viewTag) as BarCodeCameraView)
                    .touchCrop()
        }
    }
}