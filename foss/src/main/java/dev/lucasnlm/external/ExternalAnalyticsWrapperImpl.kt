package dev.lucasnlm.external

import android.content.Context

class ExternalAnalyticsWrapperImpl : ExternalAnalyticsWrapper {
    override fun setup(
        context: Context,
        properties: Map<String, String>,
    ) {
        // F-droid build doesn't have analytics.
    }

    override fun sendEvent(
        name: String,
        content: Map<String, String>,
    ) {
        // F-droid build doesn't have analytics.
    }
}
