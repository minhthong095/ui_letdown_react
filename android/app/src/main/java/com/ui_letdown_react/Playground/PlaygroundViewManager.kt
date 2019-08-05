package com.ui_letdown_react.Playground

import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

class PlaygroundViewManager : SimpleViewManager<PlaygroundView>() {

    enum class PlauygroundEvent(val value: String) {
        CLICK_BUTTON("onClick")
    }

    override fun createViewInstance(reactContext: ThemedReactContext): PlaygroundView =
            PlaygroundView(reactContext)

    override fun getName(): String = "PlaygroundView"
}