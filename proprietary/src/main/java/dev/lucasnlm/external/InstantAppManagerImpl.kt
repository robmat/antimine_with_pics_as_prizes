package dev.lucasnlm.external

import android.content.Context
import com.google.android.gms.instantapps.InstantApps

class InstantAppManagerImpl : InstantAppManager {
    override fun isEnabled(context: Context): Boolean = InstantApps.getPackageManagerCompat(context).isInstantApp
}
