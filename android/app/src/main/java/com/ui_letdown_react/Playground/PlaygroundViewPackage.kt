package com.ui_letdown_react.Playground

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ViewManager
import java.util.*

class PlaygroundViewPackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext): MutableList<NativeModule> =
            Collections.emptyList()

    override fun createViewManagers(reactContext: ReactApplicationContext): MutableList<SimpleViewManager<*>> =
            mutableListOf(PlaygroundViewManager())
}