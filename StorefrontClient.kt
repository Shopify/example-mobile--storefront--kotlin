package com.shopify.example.storefront

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

// [START integrate.config]
class StorefrontClient(
    private val shopDomain: String,
    private val accessToken: String,
    private val apiVersion: String = "2026-01"
) {
    private val json = Json { ignoreUnknownKeys = true }
// [END integrate.config]

    // [START complete-tutorial.fetch-products]
    suspend fun fetchProducts(): List<Product> = withContext(Dispatchers.IO) {
        val url = URL("https://$shopDomain/api/$apiVersion/graphql.json")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Shopify-Storefront-Access-Token", accessToken)
        connection.doOutput = true

        val query = """
            query Products {
              products(first: 10) {
                edges {
                  node {
                    id
                    title
                    description
                    featuredImage { url }
                    variants(first: 1) {
                      edges {
                        node {
                          id
                          title
                          price { amount currencyCode }
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val body = """{"query": "${query.replace("\n", "\\n").replace("\"", "\\\"")}"}"""
        connection.outputStream.write(body.toByteArray())

        val response = connection.inputStream.bufferedReader().readText()
        val result = json.decodeFromString<ProductsResponse>(response)

        result.data.products.edges.map { it.node }
    }
    // [END complete-tutorial.fetch-products]

    // [START complete-tutorial.create-cart]
    suspend fun createCart(variantId: String, quantity: Int = 1): Cart = withContext(Dispatchers.IO) {
        val url = URL("https://$shopDomain/api/$apiVersion/graphql.json")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("X-Shopify-Storefront-Access-Token", accessToken)
        connection.doOutput = true

        val mutation = """
            mutation CartCreate(${'$'}input: CartInput!) {
              cartCreate(input: ${'$'}input) {
                cart {
                  id
                  checkoutUrl
                }
                userErrors {
                  field
                  message
                }
              }
            }
        """.trimIndent()

        val variables = mapOf(
            "input" to mapOf(
                "lines" to listOf(
                    mapOf("merchandiseId" to variantId, "quantity" to quantity)
                )
            )
        )

        val body = Json.encodeToString(
            mapOf("query" to mutation, "variables" to variables)
        )
        connection.outputStream.write(body.toByteArray())

        val response = connection.inputStream.bufferedReader().readText()
        val result = json.decodeFromString<CartCreateResponse>(response)

        val errors = result.data.cartCreate.userErrors
        if (errors.isNotEmpty()) {
            throw Exception(errors.first().message)
        }

        result.data.cartCreate.cart ?: throw Exception("Cart not created")
    }
    // [END complete-tutorial.create-cart]

    // [START integrate.cart-permalink]
    fun buildCartPermalink(variantId: String, quantity: Int = 1): String {
        return "https://$shopDomain/cart/$variantId:$quantity"
    }
    // [END integrate.cart-permalink]
}
