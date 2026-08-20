package dev.lucasnlm.external

import android.content.Context

class ExternalAnalyticsWrapperImpl : ExternalAnalyticsWrapper {
    override fun setup(
        context: Context,
        properties: Map<String, String>,
    ) {
        // No-op: analytics not available in this build.
    }

    override fun sendEvent(
        name: String,
        content: Map<String, String>,
    ) {
        // No-op: analytics not available in this build.
    }
}
