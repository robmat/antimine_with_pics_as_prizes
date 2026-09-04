package dev.lucasnlm.external

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewWrapperImpl : ReviewWrapper {
    override fun startReviewPage(
        activity: Activity,
        appPackage: String,
    ) {
        if (!activity.isFinishing) {
            val playStoreUri = "market://details?id=$appPackage"
            val playStorePage = "https://play.google.com/store/apps/details?id=$appPackage"

            runCatching {
                activity.startActivity(Intent(Intent.ACTION_VIEW, playStoreUri.toUri()))
            }.onFailure {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        playStorePage.toUri(),
                    ),
                )
            }
        }
    }

    override fun startInAppReview(activity: Activity) {
        ReviewManagerFactory.create(activity).run {
            requestReviewFlow()
                .addOnCompleteListener {
                    if (it.isSuccessful && !activity.isFinishing) {
                        launchReviewFlow(activity, it.result)
                    }
                }
        }
    }
}
