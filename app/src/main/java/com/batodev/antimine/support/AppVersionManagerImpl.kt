package com.batodev.antimine.support

import android.app.Application
import dev.lucasnlm.antimine.core.AppVersionManager

class AppVersionManagerImpl(
    private val debug: Boolean,
    private val application: Application,
) : AppVersionManager {
    private val valid by lazy {
        val id = application.packageName
        debug || id == "com.logical.minato" || id == "com.batodev.antimine" || id == "dev.lucanlm.antimine"
    }

    override fun isValid(): Boolean {
        // If you want to distribution a fork of this game,
        // please check its LICENSE and conditions before do it.
        // Any fork / changed version must also be open sourced.
        return valid
    }

    override fun isWatch(): Boolean {
        return false
    }
}
