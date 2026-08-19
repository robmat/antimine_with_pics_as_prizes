package com.batodev.antimine.main

import android.content.Intent
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.batodev.antimine.main.viewmodel.MainEvent
import com.batodev.antimine.playgames.PlayGamesDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns the Google Play Games sign-in flow (silent login, interactive login
 * fallback, user-id migration) - split out of [MainActivity] since this was
 * the bulk of its function count.
 */
class PlayGamesFlow(private val activity: MainActivity) {
    fun launch() {
        if (activity.playGamesManager.hasGooglePlayGames() &&
            activity.playGamesManager.shouldRequestLogin() &&
            activity.preferenceRepository.keepRequestPlayGames()
        ) {
            activity.playGamesManager.keepRequestingLogin(false)

            activity.lifecycleScope.launch {
                var logged = false

                runCatching {
                    withContext(Dispatchers.IO) {
                        logged = activity.playGamesManager.silentLogin()
                        if (logged) {
                            refreshUserId()
                        }
                        activity.playGamesManager.showPlayPopUp(activity)
                    }
                }.onFailure {
                    Log.e(MainActivity.TAG, "Failed silent login", it)
                }

                if (!logged) {
                    runCatching {
                        activity.playGamesManager.getLoginIntent()?.let {
                            activity.googlePlayLauncher.launch(it)
                        }
                    }.onFailure {
                        Log.e(MainActivity.TAG, "User not logged or doesn't have Play Games", it)
                    }
                } else {
                    afterLogin()
                }
            }
        } else {
            afterLogin()
        }
    }

    fun show() {
        if (activity.playGamesManager.isLogged()) {
            if (activity.supportFragmentManager.findFragmentByTag(PlayGamesDialogFragment.TAG) == null &&
                !activity.isFinishing
            ) {
                PlayGamesDialogFragment().show(activity.supportFragmentManager, PlayGamesDialogFragment.TAG)
            }
        } else {
            activity.playGamesManager.getLoginIntent()?.let {
                activity.googlePlayLauncher.launch(it)
            }
        }
    }

    fun handleResult(data: Intent?) {
        activity.playGamesManager.handleLoginResult(data)
        activity.lifecycleScope.launch {
            refreshUserId()
        }
    }

    private fun afterLogin() {
        activity.playGamesManager.signInToFirebase(activity)
        activity.inAppUpdateManager.checkUpdate(activity)
    }

    private suspend fun refreshUserId() {
        withContext(Dispatchers.Default) {
            val lastId = activity.preferencesRepository.userId()
            val newId = activity.playGamesManager.playerId()

            if (lastId != newId && newId != null) {
                activity.preferencesRepository.setUserId(newId)

                withContext(Dispatchers.Main) {
                    migrateDataAndRecreate()
                }
            }
        }
    }

    private fun migrateDataAndRecreate() {
        activity.lifecycleScope.launch {
            if (!activity.isFinishing) {
                activity.preferencesRepository.userId()?.let {
                    activity.viewModel.sendEvent(MainEvent.FetchCloudSave(it))
                }
            }
        }
    }
}
