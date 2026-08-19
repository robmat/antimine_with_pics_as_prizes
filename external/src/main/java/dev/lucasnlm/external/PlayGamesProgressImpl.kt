package dev.lucasnlm.external

import android.app.Activity

/**
 * No-op [PlayGamesProgress] - this build flavor doesn't have Google Play Games.
 * Split out of what used to be `PlayGamesManagerImpl` directly, since that
 * class's function count was over threshold. Delegated into
 * `PlayGamesManagerImpl` via `by`.
 */
class PlayGamesProgressImpl : PlayGamesProgress {
    override fun openAchievements(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }

    override fun openLeaderboards(activity: Activity) {
        // F-droid build doesn't have Google Play Games
    }

    override suspend fun unlockAchievement(achievement: Achievement) {
        // F-droid build doesn't have Google Play Games
    }

    override suspend fun incrementAchievement(
        achievement: Achievement,
        value: Int,
    ) {
        // F-droid build doesn't have Google Play Games
    }

    override suspend fun setAchievementSteps(
        achievement: Achievement,
        value: Int,
    ) {
        // F-droid build doesn't have Google Play Games
    }

    override fun submitLeaderboard(
        leaderboard: Leaderboard,
        value: Long,
    ) {
        // F-droid build doesn't have Google Play Games
    }
}
