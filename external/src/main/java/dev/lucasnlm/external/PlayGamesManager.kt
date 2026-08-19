package dev.lucasnlm.external

enum class Achievement(
    val value: String,
) {
    NoLuck(""),
    Almost(""),
    Beginner(""),
    Intermediate(""),
    Expert(""),
    ThirtySeconds(""),
    Flags(""),
    Boom(""),
}

enum class Leaderboard(
    val value: String,
) {
    BeginnerBestTime(""),
    IntermediateBestTime(""),
    ExpertBestTime(""),
    MasterBestTime(""),
    LegendaryBestTime(""),
}

interface PlayGamesManager : PlayGamesAuth, PlayGamesProgress
