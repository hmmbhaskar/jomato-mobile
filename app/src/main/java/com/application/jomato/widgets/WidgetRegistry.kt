package com.application.jomato.widgets

/**
 * Registry of widgets by type. Register concrete widgets so that
 * config-driven widget entries (type + payload from ui.json) can be resolved and displayed.
 * Zero-telemetry fork: UpdateWidget and AttributionWidget have been removed.
 */
object WidgetRegistry {

    private val byType = mutableMapOf<String, BaseWidget>()

    init {
        register(FaqWidget())
    }

    fun register(widget: BaseWidget) {
        byType[widget.type] = widget
    }

    fun resolve(type: String): BaseWidget? = byType[type]
}
