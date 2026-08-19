package dev.lucasnlm.external.di

import dev.lucasnlm.external.CloudStorageManager
import dev.lucasnlm.external.CloudStorageManagerImpl
import dev.lucasnlm.external.CrashReporter
import dev.lucasnlm.external.CrashReporterImpl
import dev.lucasnlm.external.PlayGamesManager
import dev.lucasnlm.external.PlayGamesManagerImpl
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Bindings shared by every build flavor (foss, proprietary, ...), since these
 * implementations are identical regardless of the flavor-specific module used.
 */
val CommonExternalModule =
    module {
        single { PlayGamesManagerImpl() } bind PlayGamesManager::class

        single { CloudStorageManagerImpl() } bind CloudStorageManager::class

        single { CrashReporterImpl() } bind CrashReporter::class
    }
