package com.example.storefront.auth

import android.net.Uri
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

class AuthClient(
    private val shopDomain: String,
    private val customerAccountsApiClientId: String,
    private val customerAccountsApiRedirectUri: String,
    private val storefrontAccessToken: String
) {
    private val httpClient = OkHttpClient()
    private var codeVerifier: String? = null
    private var savedState: String? = null

    // MARK: PKCE

    // [START auth.generate-pkce]
    fun generateCodeVerifier(): String {
        val buffer = ByteArray(32)
        SecureRandom().nextBytes(buffer)
        return buffer.base64UrlEncode()
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val data = codeVerifier.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        return digest.base64UrlEncode()
    }

    private fun ByteArray.base64UrlEncode(): String {
        return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
    // [END auth.generate-pkce]

    // MARK: Endpoint Discovery

    // [START auth.discover-endpoints]
    suspend fun discoverAuthEndpoints(): Pair<String, String> {
        val discoveryUrl = "https://$shopDomain/.well-known/openid-configuration"
        val request = Request.Builder().url(discoveryUrl).build()
        val response = httpClient.newCall(request).execute()
        val config = org.json.JSONObject(response.body!!.string())

        val authEndpoint = config.getString("authorization_endpoint")
        val tokenEndpoint = config.getString("token_endpoint")

        return Pair(authEndpoint, tokenEndpoint)
    }
    // [END auth.discover-endpoints]

    // MARK: Authorization URL

    // [START auth.build-auth-url]
    suspend fun buildAuthorizationUrl(): String {
        val codeVerifier = generateCodeVerifier()
        this.codeVerifier = codeVerifier

        val codeChallenge = generateCodeChallenge(codeVerifier)
        val (authEndpoint, _) = discoverAuthEndpoints()
        val state = generateRandomString(36)
        this.savedState = state

        return Uri.parse(authEndpoint).buildUpon()
            .appendQueryParameter("scope", "openid email customer-account-api:full")
            .appendQueryParameter("client_id", customerAccountsApiClientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", customerAccountsApiRedirectUri)
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
            .toString()
    }

    private fun generateRandomString(length: Int): String {
        val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val secureRandom = SecureRandom()
        return (1..length)
            .map { characters[secureRandom.nextInt(characters.length)] }
            .joinToString("")
    }
    // [END auth.build-auth-url]

    // MARK: Callback Handling

    // [START auth.handle-callback]
    fun handleCallback(uri: Uri): String? {
        val redirectUri = Uri.parse(customerAccountsApiRedirectUri)
        if (uri.scheme != redirectUri.scheme || uri.host != redirectUri.host) {
            return null
        }

        val state = uri.getQueryParameter("state")
        if (state != savedState) {
            return null
        }

        return uri.getQueryParameter("code")
    }
    // [END auth.handle-callback]

    // MARK: Token Exchange

    // [START auth.exchange-token]
    suspend fun requestAccessToken(code: String): OAuthTokenResult {
        val codeVerifier = this.codeVerifier
            ?: throw IllegalStateException("Code verifier not found. Call buildAuthorizationUrl() first.")

        val (_, tokenEndpoint) = discoverAuthEndpoints()
        val requestBody = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", customerAccountsApiClientId)
            .add("redirect_uri", customerAccountsApiRedirectUri)
            .add("code", code)
            .add("code_verifier", codeVerifier)
            .build()

        val request = Request.Builder()
            .url(tokenEndpoint)
            .post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response = httpClient.newCall(request).execute()
        val json = org.json.JSONObject(response.body!!.string())

        return OAuthTokenResult(
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            expiresIn = json.getInt("expires_in")
        )
    }
    // [END auth.exchange-token]

    // MARK: Authenticated Cart

    // [START auth.create-authenticated-cart]
    suspend fun createAuthenticatedCart(variantId: String, accessToken: String): String {
        val query = """
            mutation cartCreate(${'$'}input: CartInput!) {
                cartCreate(input: ${'$'}input) {
                    cart { checkoutUrl }
                    userErrors { field message }
                }
            }
        """.trimIndent()

        val variables = org.json.JSONObject().apply {
            put("input", org.json.JSONObject().apply {
                put("lines", org.json.JSONArray().put(org.json.JSONObject().apply {
                    put("merchandiseId", variantId)
                    put("quantity", 1)
                }))
                put("buyerIdentity", org.json.JSONObject().apply {
                    put("customerAccessToken", accessToken)
                })
            })
        }

        val payload = org.json.JSONObject().apply {
            put("query", query)
            put("variables", variables)
        }

        val request = Request.Builder()
            .url("https://$shopDomain/api/2026-01/graphql.json")
            .post(okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json"),
                payload.toString()
            ))
            .addHeader("X-Shopify-Storefront-Access-Token", storefrontAccessToken)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseJson = org.json.JSONObject(response.body!!.string())

        responseJson.optJSONArray("errors")?.let { errors ->
            if (errors.length() > 0) {
                throw Exception(errors.getJSONObject(0).optString("message", "GraphQL error"))
            }
        }

        val cartCreate = responseJson.optJSONObject("data")?.optJSONObject("cartCreate")
            ?: throw Exception("cartCreate not returned in response")

        cartCreate.optJSONArray("userErrors")?.let { userErrors ->
            if (userErrors.length() > 0) {
                throw Exception(userErrors.getJSONObject(0).optString("message", "Cart user error"))
            }
        }

        return cartCreate.optJSONObject("cart")?.optString("checkoutUrl")
            ?.takeIf { it.isNotEmpty() }
            ?: throw Exception("Cart creation failed: no checkout URL returned")
    }
    // [END auth.create-authenticated-cart]

    // MARK: Present Checkout

    // [START auth.present-checkout]
    fun presentCheckout(checkoutUrl: String, activity: android.app.Activity, eventProcessor: com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor) {
        ShopifyCheckoutSheetKit.present(checkoutUrl, activity, eventProcessor)
    }
    // [END auth.present-checkout]
}

@Serializable
data class OAuthTokenResult(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Int
)
