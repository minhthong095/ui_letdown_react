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

class BarCodeCameraPackage: ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> =
            mutableListOf(
                    BarCodeCameraModule(reactContext)
            )

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<ViewManager<*, *>> =
            mutableListOf(
                    BarCodeCameraViewManager()
            )
}