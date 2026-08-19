package dev.lucasnlm.external

class PlayGamesManagerImpl :
    PlayGamesManager,
    PlayGamesAuth by PlayGamesAuthImpl(),
    PlayGamesProgress by PlayGamesProgressImpl()
