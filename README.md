# Mobile Storefront — Kotlin

Companion code for [shopify.dev](https://shopify.dev) tutorials covering the Storefront API, [Checkout Kit for Android](https://github.com/Shopify/checkout-sheet-kit-android), and Customer Account API authentication.

> **This repository is not a standalone runnable app.** It contains the source files referenced inline by the tutorials. To use them, drop the files into an existing Android project and add the dependencies listed below.

## Tutorials

- [Build a mobile storefront](https://shopify.dev/docs/storefronts/mobile/build-mobile-storefront)
- [Embed Checkout Kit](https://shopify.dev/docs/storefronts/mobile/checkout-kit)
- [Authenticate checkouts](https://shopify.dev/docs/storefronts/mobile/checkout-kit/authenticate-checkouts)

## What's included

| File | Description |
|---|---|
| `StorefrontClient.kt` | Storefront API client — product queries, cart creation, cart permalinks |
| `Models.kt` | Serializable data classes for GraphQL responses |
| `ProductListScreen.kt` | Jetpack Compose product list with Add to Cart |
| `CartActivity.kt` | Checkout Kit integration with event handling |
| `AuthClient.kt` | OAuth + PKCE flow against the Customer Account API |

## Use these snippets in your project

1. Copy the relevant files into your Android project (typically under `app/src/main/java/`).
2. Add the [Checkout Sheet Kit Android library](https://github.com/Shopify/checkout-sheet-kit-android) and the dependencies imported by these files (OkHttp, kotlinx-serialization, kotlinx-coroutines, AppCompat, lifecycle-runtime-ktx, and Jetpack Compose if you're using `ProductListScreen.kt`).
3. In `StorefrontClient.kt`, replace `{shop}.myshopify.com` with your store domain and add your Storefront API access token.

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 23 or later (Android 6.0)
- JDK 17 or later
- A [Shopify development store](https://shopify.dev/docs/storefronts/headless/building-with-the-storefront-api/getting-started) with at least one product
- A Storefront API access token with `unauthenticated_read_product_listings` and `unauthenticated_write_checkouts` scopes

## Contributing

This repository doesn't accept issues or external contributions. It exists as a companion to the tutorials linked above. If you find an issue with the tutorial content, use the feedback form on the tutorial page.

## License

This project is licensed under the [MIT License](LICENSE.md).
