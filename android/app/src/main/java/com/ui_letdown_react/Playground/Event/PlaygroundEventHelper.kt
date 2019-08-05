package com.ui_letdown_react.Playground.Event

import android.view.View
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerModule

object PlaygroundEventHelper {
    fun submitClickEvent(view: View) {
        val event = ClickButtonEvent.obtain(view.id)
        (view.context as ReactContext).getNativeModule(UIManagerModule::class.java).eventDispatcher.dispatchEvent(event)
    }
}