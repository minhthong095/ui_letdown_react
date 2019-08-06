package com.ui_letdown_react.Playground.Event

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.ui_letdown_react.Playground.PlaygroundViewManager

class ClickButtonEvent constructor(id: Int) : Event<ClickButtonEvent>(id) {

    override fun getEventName() = PlaygroundViewManager.PlauygroundEvent.CLICK_BUTTON.value

    override fun dispatch(rctEventEmitter: RCTEventEmitter?) {
        rctEventEmitter?.receiveEvent(viewTag, eventName, serializeEventData())
    }

    private fun serializeEventData(): WritableMap =
            Arguments.createMap().apply {
                putString("ass", "HAHA BITCHHH")
            }
}