package com.ui_letdown_react.BarCodeCamera

import android.view.View
import android.view.ViewGroup
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ViewManager
import java.util.*

class BarCodeCameraPackage: ReactPackage {

    companion object {
        val VALID_ZXING_BARCODE = Collections.unmodifiableMap(
                hashMapOf<String, String>(
                        1 to BarCode
                )
        )
    }

    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> =
            Collections.emptyList()

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<*, *>> =
            mutableListOf(
                    BarCodeCameraViewManager()
            )
}