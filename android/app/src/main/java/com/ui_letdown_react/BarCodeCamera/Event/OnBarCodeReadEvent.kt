package com.ui_letdown_react.BarCodeCamera.Event

import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.ui_letdown_react.BarCodeCamera.BarCodeCameraViewManager

class OnBarCodeReadEvent(id: Int, val _result: String) : Event<OnBarCodeReadEvent>(id) {
    override fun getEventName(): String {
       return ""
    }

    override fun dispatch(rctEventEmitter: RCTEventEmitter?) {
    }
}