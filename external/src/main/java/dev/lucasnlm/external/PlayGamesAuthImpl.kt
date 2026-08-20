package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent

/**
 * No-op [PlayGamesAuth] - this build flavor doesn't have Google Play Games.
 * Split out of what used to be `PlayGamesManagerImpl` directly, since that
 * class's function count was over threshold. Delegated into
 * `PlayGamesManagerImpl` via `by`.
 */
class PlayGamesAuthImpl : PlayGamesAuth {
    override suspend fun playerId(): String? = null

    override fun hasGooglePlayGames(): Boolean = false

    override suspend fun silentLogin(): Boolean {
        // F-droid build doesn't have Google Play Games
        return false
    }

    override fun showPlayPopUp(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }

    override fun getLoginIntent(): Intent? = null

    override fun handleLoginResult(data: Intent?) {
        // F-droid build doesn't have Google Play Games
    }

    override fun isLogged(): Boolean = false

    override fun keepRequestingLogin(status: Boolean) {
        // F-droid build doesn't have Google Play Games
    }

    override fun shouldRequestLogin(): Boolean = false

    override fun signInToFirebase(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }
}
