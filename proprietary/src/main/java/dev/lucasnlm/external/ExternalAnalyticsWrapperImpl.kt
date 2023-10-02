package dev.lucasnlm.external

import android.content.Context
import android.os.Bundle
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import org.json.JSONObject

class ExternalAnalyticsWrapperImpl(
    private val context: Context,
) : ExternalAnalyticsWrapper {

    override fun setup(
        context: Context,
        properties: Map<String, String>,
    ) {
    }

    override fun sendEvent(
        name: String,
        content: Map<String, String>,
    ) {
    }
}
