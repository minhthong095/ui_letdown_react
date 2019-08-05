package com.ui_letdown_react.BarCodeCamera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class BarCodeCameraModule(private val _reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(_reactContext) {

    override fun getName(): String = "BarCodeCameraModule"

}