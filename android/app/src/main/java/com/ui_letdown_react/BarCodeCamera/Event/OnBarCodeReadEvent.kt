package com.ui_letdown_react.BarCodeCamera.Event

import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter

class OnBarCodeReadEvent(id: Int, val _result: String) : Event<OnBarCodeReadEvent>(id) {
    override fun getEventName(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dispatch(rctEventEmitter: RCTEventEmitter?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}