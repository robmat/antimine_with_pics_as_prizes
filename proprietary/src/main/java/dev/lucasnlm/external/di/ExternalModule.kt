package dev.lucasnlm.external.di

import dev.lucasnlm.external.AdMobAdsManager
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.BillingManagerImpl
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.FeatureFlagManagerImpl
import dev.lucasnlm.external.InAppUpdateManager
import dev.lucasnlm.external.InAppUpdateManagerImpl
import dev.lucasnlm.external.InstantAppManager
import dev.lucasnlm.external.InstantAppManagerImpl
import dev.lucasnlm.external.ReviewWrapper
import dev.lucasnlm.external.ReviewWrapperImpl
import org.koin.dsl.bind
import org.koin.dsl.module

// Bindings shared across flavors (PlayGamesManager, CloudStorageManager, CrashReporter)
// live in CommonExternalModule and are registered alongside this module at startup.
val ExternalModule =
    module {
        single { InstantAppManagerImpl() } bind InstantAppManager::class

        single { BillingManagerImpl(get(), get(), get()) } bind BillingManager::class

        single { ReviewWrapperImpl() } bind ReviewWrapper::class

        single { FeatureFlagManagerImpl() } bind FeatureFlagManager::class

        single { AdMobAdsManager(get(), get(), get()) } bind AdsManager::class

        single { InAppUpdateManagerImpl() } bind InAppUpdateManager::class
    }
