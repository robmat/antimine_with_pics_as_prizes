package dev.lucasnlm.external

import android.text.format.DateUtils
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryPurchasesAsync
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * [BillingManagerImpl]'s purchase-polling/acknowledgement logic, split out
 * of the class body - see its class doc.
 */
internal fun BillingManagerImpl.asyncRefreshPurchasesList() {
    coroutineScope.launch {
        var shouldContinuePolling = true
        while (shouldContinuePolling) {
            shouldContinuePolling = refreshPurchasesListOnce()
        }
    }
}

internal suspend fun BillingManagerImpl.refreshPurchasesListOnce(): Boolean {
    val queryPurchasesParams =
        QueryPurchasesParams
            .newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

    val purchasesList: List<Purchase> =
        billingClient
            .queryPurchasesAsync(queryPurchasesParams)
            .purchasesList

    if (purchasesList.isEmpty()) {
        return false
    }

    val handled = handlePurchases(purchasesList)
    if (!handled) {
        delay(BillingManagerImpl.PURCHASE_POLL_DELAY_SECONDS * DateUtils.SECOND_IN_MILLIS)
    }
    return !handled
}

internal suspend fun BillingManagerImpl.handlePurchases(purchases: List<Purchase>): Boolean {
    val status: Boolean =
        purchases
            .firstOrNull {
                it.products.contains(BillingManagerImpl.PREMIUM)
            }.let {
                when (it?.purchaseState) {
                    Purchase.PurchaseState.PURCHASED, Purchase.PurchaseState.PENDING -> true
                    else -> false
                }.also { purchased ->
                    if (purchased && it?.isAcknowledged == false) {
                        val acknowledgePurchaseParams =
                            AcknowledgePurchaseParams
                                .newBuilder()
                                .setPurchaseToken(it.purchaseToken)
                                .build()

                        val result = billingClient.acknowledgePurchase(acknowledgePurchaseParams)

                        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                            return false
                        }
                    }
                }
            }

    purchaseBroadcaster.tryEmit(
        PurchaseInfo.PurchaseResult(
            isFreeUnlock = false,
            unlockStatus = status,
        ),
    )
    return true
}

internal fun BillingManagerImpl.onReceivePremiumProduct(productDetails: ProductDetails?) {
    val premiumProductDetails = productDetails?.productId == BillingManagerImpl.PREMIUM

    if (productDetails != null && premiumProductDetails) {
        premiumProduct = productDetails
        val premiumPrice = productDetails.oneTimePurchaseOfferDetails?.formattedPrice

        if (premiumPrice != null) {
            val price =
                Price(
                    premiumPrice,
                    offer = false,
                )

            unlockPrice.tryEmit(price)
        }
    }
}
