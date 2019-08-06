package com.ui_letdown_react.Playground

import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerModule
import com.ui_letdown_react.Playground.Event.ClickButtonEvent
import com.ui_letdown_react.dispatchEvent

class PlaygroundView(private val _context: ThemedReactContext) : LinearLayout(_context) {
    init {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        orientation = VERTICAL

        val txt1 = TextView(_context)
        txt1.text = "1"

        val btn = Button(_context)
        btn.text = "Button"
        addView(txt1)
        addView(btn)

        btn.setOnClickListener {
            _context.dispatchEvent(ClickButtonEvent(id))
        }
    }
}