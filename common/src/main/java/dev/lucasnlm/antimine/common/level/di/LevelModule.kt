package dev.lucasnlm.antimine.common.level.di

import androidx.room.Room
import dev.lucasnlm.antimine.common.level.database.AppDataBase
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepositoryImpl
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepositoryImpl
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepositoryImpl
import dev.lucasnlm.antimine.common.level.repository.TipRepository
import dev.lucasnlm.antimine.common.level.repository.TipRepositoryImpl
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.common.level.viewmodel.GameDataDependencies
import dev.lucasnlm.antimine.common.level.viewmodel.GameEnvironmentDependencies
import dev.lucasnlm.antimine.common.level.viewmodel.GameFeedbackDependencies
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val LevelModule =
    module {
        // Shared by every app (app, wear, ...) that depends on :common, since the
        // GameViewModel constructor is identical regardless of the host app.
        viewModel {
            GameViewModel(
                GameDataDependencies(get(), get(), get(), get()),
                GameEnvironmentDependencies(get(), get(), get(), get()),
                GameFeedbackDependencies(get(), get(), get(), get()),
            )
        }

        single {
            Room
                .databaseBuilder(get(), AppDataBase::class.java, AppDataBase.DATA_BASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }

        single {
            get<AppDataBase>(AppDataBase::class).saveDao()
        }

        single {
            get<AppDataBase>(AppDataBase::class).statsDao()
        }

        single {
            Clock()
        }

        single {
            SavesRepositoryImpl(get())
        } bind SavesRepository::class

        single {
            StatsRepositoryImpl(get())
        } bind StatsRepository::class

        single {
            MinefieldRepositoryImpl()
        } bind MinefieldRepository::class

        single {
            TipRepositoryImpl(get(), get())
        } bind TipRepository::class
    }
