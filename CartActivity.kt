package com.shopify.example.storefront

// [START complete-tutorial.present-checkout]
// [START integrate.present-checkout]
// [START integrate.install]
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.shopify.checkoutsheetkit.*
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import kotlinx.coroutines.launch
// [END integrate.install]

class CartActivity : AppCompatActivity() {
    private var checkoutUrl: String? = null
    private val storefrontClient = StorefrontClient(
        shopDomain = "{shop}.myshopify.com",
        accessToken = "your-storefront-access-token"
    )

    private val checkoutEventProcessor = object : DefaultCheckoutEventProcessor(this) {
        override fun onCheckoutCompleted(event: CheckoutCompletedEvent) {
            val orderId = event.orderDetails.id
            Log.d("Checkout", "Order completed: $orderId")
            checkoutUrl = null
        }

        override fun onCheckoutCanceled() {
            Log.d("Checkout", "Checkout canceled")
        }

        override fun onCheckoutFailed(error: CheckoutException) {
            Log.e("Checkout", "Checkout failed: ${error.message}")
            Toast.makeText(
                this@CartActivity,
                "Checkout error: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onCheckoutLinkClicked(uri: android.net.Uri) {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        findViewById<Button>(R.id.checkout_button).setOnClickListener {
            presentCheckout()
        }
    }

    fun addToCart(variantId: String) {
        lifecycleScope.launch {
            try {
                val cart = storefrontClient.createCart(variantId)
                checkoutUrl = cart.checkoutUrl
            } catch (e: Exception) {
                Log.e("Cart", "Failed to create cart: ${e.message}")
            }
        }
    }

    private fun presentCheckout() {
        val url = checkoutUrl
        if (url == null) {
            Toast.makeText(this, "No checkout URL available", Toast.LENGTH_SHORT).show()
            return
        }

        ShopifyCheckoutSheetKit.present(url, this, checkoutEventProcessor)
    }
}
// [END integrate.present-checkout]
// [END complete-tutorial.present-checkout]
