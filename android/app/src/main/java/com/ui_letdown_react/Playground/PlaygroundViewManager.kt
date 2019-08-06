package com.ui_letdown_react.Playground

import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

class PlaygroundViewManager : SimpleViewManager<PlaygroundView>() {

    enum class PlauygroundEvent(val value: String) {
        CLICK_BUTTON("onXXXClick")
    }

    override fun createViewInstance(reactContext: ThemedReactContext): PlaygroundView =
            PlaygroundView(reactContext)

    override fun getName(): String = "PlaygroundView"

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        val builder = MapBuilder.builder<String, Any>()
        for (event in PlauygroundEvent.values())
            builder.put(event.value, MapBuilder.of("registrationName", event.value))
        return builder.build()
    }
}