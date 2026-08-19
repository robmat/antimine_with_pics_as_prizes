package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent

/** The sign-in half of [PlayGamesManager]'s contract. */
interface PlayGamesAuth {
    suspend fun playerId(): String?

    fun hasGooglePlayGames(): Boolean

    suspend fun silentLogin(): Boolean

    fun showPlayPopUp(activity: Activity)

    fun getLoginIntent(): Intent?

    fun handleLoginResult(data: Intent?)

    fun isLogged(): Boolean

    fun keepRequestingLogin(status: Boolean)

    fun shouldRequestLogin(): Boolean

    fun signInToFirebase(activity: Activity)
}
