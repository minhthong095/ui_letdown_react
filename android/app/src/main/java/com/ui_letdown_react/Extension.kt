package com.ui_letdown_react

import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.facebook.react.uimanager.events.Event

fun ThemedReactContext.dispatchEvent(event: Event<*>) {
    this.getNativeModule(UIManagerModule::class.java).eventDispatcher.dispatchEvent(event)
}