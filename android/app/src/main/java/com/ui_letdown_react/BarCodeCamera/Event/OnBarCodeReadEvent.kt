package com.ui_letdown_react.BarCodeCamera.Event

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.ui_letdown_react.BarCodeCamera.BarCodeCameraManager

class OnBarCodeReadEvent(id: Int, val _result: String) : Event<OnBarCodeReadEvent>(id) {
    override fun getEventName(): String {
       return BarCodeCameraManager.Event.ON_BARCODE_READ.value
    }

    override fun dispatch(rctEventEmitter: RCTEventEmitter?) {
        rctEventEmitter?.receiveEvent(viewTag, eventName, serializeEventData())
    }

    private fun serializeEventData(): WritableMap {
        val writableMap = Arguments.createMap()
        writableMap.putString("result", _result)
        return writableMap
    }
}