package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import dev.lucasnlm.external.model.Price
import dev.lucasnlm.external.model.PurchaseInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

/**
 * The purchase-polling/acknowledgement helper functions
 * ([asyncRefreshPurchasesList] and its siblings) were split out into
 * BillingManagerImplPurchases.kt since this class's function count was over
 * threshold; several fields are `internal` rather than `private` only
 * because those extension functions, living outside the class body, need
 * access to them.
 */
class BillingManagerImpl(
    private val context: Context,
    internal val crashReporter: CrashReporterImpl,
    internal val coroutineScope: CoroutineScope,
) : BillingManager,
    BillingClientStateListener,
    PurchasesUpdatedListener {
    private var retry = 0
    private var isLoading = false
    internal val purchaseBroadcaster = MutableStateFlow<PurchaseInfo?>(null)
    internal val unlockPrice = MutableStateFlow<Price?>(null)
    internal val billingClient by lazy {
        try {
            BillingClient
                .newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
                ).build()
        } catch (e: IllegalStateException) {
            crashReporter.sendError("Failed to initialize BillingClient: ${e.message}")
            throw e
        }
    }

    private val allowedErrorCodes =
        listOf(
            BillingClient.BillingResponseCode.OK,
            BillingClient.BillingResponseCode.USER_CANCELED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
        )

    internal var premiumProduct: ProductDetails? = null

    override suspend fun getPrice(): Price? = unlockPrice.value

    override suspend fun getPriceFlow(): Flow<Price> = unlockPrice.asSharedFlow().filterNotNull()

    override fun listenPurchases(): Flow<PurchaseInfo> = purchaseBroadcaster.asSharedFlow().filterNotNull()

    override fun onBillingServiceDisconnected() {
        crashReporter.sendError("Billing service disconnected $retry")
        isLoading = false

        if (retry < MAX_BILLING_RETRY_ATTEMPTS) {
            retry++
            coroutineScope.launch {
                delay(BILLING_RETRY_DELAY_MS)
                start()
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        isLoading = false

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            retry = 0

            val premiumProductParams =
                QueryProductDetailsParams
                    .Product
                    .newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .setProductId(PREMIUM)
                    .build()

            val productDetailsParams =
                QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(listOf(premiumProductParams))
                    .build()

            billingClient
                .queryProductDetailsAsync(productDetailsParams) { _, list ->
                    onReceivePremiumProduct(list.productDetailsList.firstOrNull())
                }

            asyncRefreshPurchasesList()
        } else {
            val code = billingResult.responseCode
            val message = billingResult.debugMessage
            crashReporter.sendError("Billing setup failed due to response $code. $message")
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        val resultCode = billingResult.responseCode
        if (resultCode == BillingClient.BillingResponseCode.OK) {
            asyncRefreshPurchasesList()
        } else if (!allowedErrorCodes.contains(resultCode)) {
            crashReporter.sendError("Charge update failed due to response $resultCode")
        }
    }

    override fun start() {
        if (!billingClient.isReady && !isLoading) {
            billingClient.startConnection(this)
        }
    }

    override fun isEnabled(): Boolean = false

    override suspend fun charge(activity: Activity) {
        val premiumProduct = this.premiumProduct

        if (billingClient.isReady && premiumProduct != null) {
            val productDetailsParams =
                BillingFlowParams.ProductDetailsParams
                    .newBuilder()
                    .setProductDetails(premiumProduct)
                    .build()

            val flowParams =
                BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(listOf(productDetailsParams))
                    .build()

            billingClient.launchBillingFlow(activity, flowParams)
        } else {
            crashReporter.sendError("Fail to charge due to unready status")
            purchaseBroadcaster.tryEmit(PurchaseInfo.PurchaseFail)
        }
    }

    companion object {
        internal const val PREMIUM = "unlock_0"
        internal const val PURCHASE_POLL_DELAY_SECONDS = 30L
        private const val MAX_BILLING_RETRY_ATTEMPTS = 3
        private const val BILLING_RETRY_DELAY_MS = 5000L
    }
}
