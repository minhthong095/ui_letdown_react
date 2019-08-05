package com.ui_letdown_react.Playground.Event

import androidx.core.util.Pools
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.ui_letdown_react.Playground.PlaygroundViewManager

class ClickButtonEvent private constructor() : Event<ClickButtonEvent>() {

    companion object {

        private val EventPool = Pools.SynchronizedPool<ClickButtonEvent>(3)

        fun obtain(viewId: Int): ClickButtonEvent {
            var event = EventPool.acquire()
            if (event == null)
                event = ClickButtonEvent()

            event.init(viewId)
            return event
        }
    }

    override fun getCoalescingKey(): Short = 0

    override fun getEventName() = PlaygroundViewManager.PlauygroundEvent.CLICK_BUTTON.value

    override fun dispatch(rctEventEmitter: RCTEventEmitter?) {
        rctEventEmitter?.receiveEvent(viewTag, eventName, serializeEventData())
    }

    private fun serializeEventData(): WritableMap =
            Arguments.createMap().apply {
                putString("message", "HAHA BITCHHH")
            }
}