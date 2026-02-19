package com.shopify.example.storefront

import kotlinx.serialization.Serializable

// [START complete-tutorial.response-types]
@Serializable
data class ProductsResponse(val data: ProductsData)

@Serializable
data class ProductsData(val products: ProductConnection)

@Serializable
data class ProductConnection(val edges: List<ProductEdge>)

@Serializable
data class ProductEdge(val node: Product)

@Serializable
data class Product(
    val id: String,
    val title: String,
    val description: String? = null,
    val featuredImage: FeaturedImage? = null,
    val variants: VariantConnection
) {
    val firstVariantId: String?
        get() = variants.edges.firstOrNull()?.node?.id
}

@Serializable
data class FeaturedImage(val url: String)

@Serializable
data class VariantConnection(val edges: List<VariantEdge>)

@Serializable
data class VariantEdge(val node: ProductVariant)

@Serializable
data class ProductVariant(
    val id: String,
    val title: String,
    val price: Price
)

@Serializable
data class Price(val amount: String, val currencyCode: String)

@Serializable
data class CartCreateResponse(val data: CartCreateData)

@Serializable
data class CartCreateData(val cartCreate: CartCreatePayload)

@Serializable
data class CartCreatePayload(
    val cart: Cart? = null,
    val userErrors: List<UserError> = emptyList()
)

@Serializable
data class Cart(
    val id: String,
    val checkoutUrl: String
)

@Serializable
data class UserError(
    val field: List<String>? = null,
    val message: String
)
// [END complete-tutorial.response-types]
