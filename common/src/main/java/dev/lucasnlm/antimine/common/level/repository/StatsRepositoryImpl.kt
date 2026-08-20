package dev.lucasnlm.antimine.common.level.repository

import dev.lucasnlm.antimine.common.level.database.dao.StatsDao
import dev.lucasnlm.antimine.common.level.database.models.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatsRepositoryImpl(
    private val statsDao: StatsDao,
) : StatsRepository {
    override suspend fun getAllStats(minId: Int): List<Stats> =
        withContext(Dispatchers.IO) {
            statsDao.getAll(minId)
        }

    override suspend fun addAllStats(stats: List<Stats>): Long? =
        withContext(Dispatchers.IO) {
            statsDao.insertAll(stats).firstOrNull()
        }

    override suspend fun addStats(stats: Stats): Long =
        withContext(Dispatchers.IO) {
            statsDao.insert(stats)
        }

    override suspend fun deleteLastStats() =
        withContext(Dispatchers.IO) {
            statsDao.deleteLast()
        }
}
