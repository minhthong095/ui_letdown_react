package com.ui_letdown_react.BarCodeCamera

import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ViewManager
import com.google.zxing.BarcodeFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class BarCodeCameraPackage : ReactPackage {

    companion object {

        // Anybody want to change supported barcode, so change this.
        // And also in iOS platform and React platform
        private val mSupportedBarCode = BarcodeFormat.values()

        // Put into Hash collection to efficient get. (performance)
        val VALID_SUPPORT_BARCODE by lazy {
            Collections.unmodifiableMap(object : HashMap<String, BarcodeFormat>(mSupportedBarCode.size) {
                init {
                    mSupportedBarCode.map {
                        put(it.toString(), it)
                    }
                }
            })
        }

    }

    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> =
            mutableListOf(
                    BarCodeCameraModule(reactContext)
            )

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<*, *>> =
            mutableListOf(
                    BarCodeCameraViewManager()
            )
}