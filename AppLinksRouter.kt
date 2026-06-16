package com.shopify.example.storefront

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit

// [START app-links.route-url]
class StorefrontUrl(private val uri: Uri) {
    val isCheckout: Boolean
        get() = uri.path?.contains("/checkouts/") == true

    val isCart: Boolean
        get() = uri.path == "/cart" || uri.path?.startsWith("/cart/") == true

    val isThankYouPage: Boolean
        get() = uri.path?.contains(Regex("/thank[-_]you", RegexOption.IGNORE_CASE)) == true
}

class AppLinksRouter(
    private val activity: Activity,
    private val checkoutEventProcessor: DefaultCheckoutEventProcessor,
    private val navigateToCart: () -> Unit,
) {
    fun route(uri: Uri) {
        val storefrontUrl = StorefrontUrl(uri)

        when {
            storefrontUrl.isCheckout && !storefrontUrl.isThankYouPage -> {
                ShopifyCheckoutSheetKit.present(
                    uri.toString(),
                    activity,
                    checkoutEventProcessor,
                )
            }
            storefrontUrl.isCart -> navigateToCart()
            else -> activity.startActivity(
                Intent(Intent.ACTION_VIEW, uri).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }
            )
        }
    }
}
// [END app-links.route-url]

// [START app-links.handle-intent]
class MainActivity : ComponentActivity() {
    private lateinit var appLinksRouter: AppLinksRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appLinksRouter = AppLinksRouter(
            activity = this,
            checkoutEventProcessor = DefaultCheckoutEventProcessor(this),
            navigateToCart = { navigateToCart() },
        )
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { appLinksRouter.route(it) }
        }
    }

    private fun navigateToCart() {
        // Navigate to your cart screen.
    }
}
// [END app-links.handle-intent]
