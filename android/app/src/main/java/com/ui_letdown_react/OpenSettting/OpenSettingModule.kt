package com.ui_letdown_react.OpenSettting

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class OpenSettingModule(private val mReactContext: ReactApplicationContext): ReactContextBaseJavaModule(mReactContext) {
    override fun getName(): String = "OpenSetting"

    @ReactMethod
    fun openAppSetting(promise: Promise) {
        try {
            val intent = Intent()
            intent.apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:" + mReactContext.packageName)
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            mReactContext.startActivity(intent)

             promise.resolve(true)

        } catch (err: Exception) {
            promise.resolve(false)
        }
    }
}