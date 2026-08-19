package dev.lucasnlm.external

import android.app.Activity

/** The achievements/leaderboards half of [PlayGamesManager]'s contract. */
interface PlayGamesProgress {
    fun openAchievements(activity: Activity)

    fun openLeaderboards(activity: Activity)

    suspend fun unlockAchievement(achievement: Achievement)

    suspend fun incrementAchievement(
        achievement: Achievement,
        value: Int,
    )

    suspend fun setAchievementSteps(
        achievement: Achievement,
        value: Int,
    )

    fun submitLeaderboard(
        leaderboard: Leaderboard,
        value: Long,
    )
}
