package com.application.jomato.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.application.jomato.config.UiConfigManager
import com.application.jomato.widgets.WidgetRegistry

/**
 * Bottom bar: shows the privacy FAQ widget if configured, or a minimal spacer.
 * Zero-telemetry fork: update and attribution widgets have been removed.
 */
@Composable
fun DashboardBottomBar() {
    val config = UiConfigManager.config
    val faqConfig = config?.widgets?.find { it.type == "jomato_privacy_faqs" }

    when {
        faqConfig != null ->
            WidgetRegistry.resolve("jomato_privacy_faqs")?.Display(faqConfig.payload)
        else ->
            Box(Modifier.fillMaxWidth().height(56.dp))
    }
}
