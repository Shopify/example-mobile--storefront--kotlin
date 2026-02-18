# Mobile Storefront — Kotlin

Sample Android app that fetches products from Shopify using the Storefront API, creates a cart, and presents checkout with [Checkout Kit for Android](https://github.com/Shopify/checkout-sheet-kit-android).

This code accompanies the following tutorials on [shopify.dev](https://shopify.dev):

- [Build a mobile storefront](https://shopify.dev/docs/storefronts/mobile/complete-tutorial)
- [Embed Checkout Kit](https://shopify.dev/docs/storefronts/mobile/checkout-kit/integrate)

## What's included

| File | Description |
|---|---|
| `StorefrontClient.kt` | Storefront API client — product queries and cart creation |
| `Models.kt` | Serializable data classes for GraphQL responses |
| `ProductListScreen.kt` | Jetpack Compose product list with Add to Cart |
| `CartActivity.kt` | Checkout Kit integration with event handling |

## Run locally

1. Clone this repo.
2. Open the project in Android Studio.
3. In `StorefrontClient.kt`, replace `{shop}.myshopify.com` with your store domain and add your Storefront API access token.
4. Build and run on an emulator or device (SDK 23+).

## Requirements

- Android Studio Arctic Fox+
- Android SDK 23+ (Android 6.0)
- JDK 17+
- A [Shopify development store](https://shopify.dev/docs/storefronts/headless/building-with-the-storefront-api/getting-started) with at least one product
- A Storefront API access token with `unauthenticated_read_product_listings` and `unauthenticated_write_checkouts` scopes

## Contributing

This repository doesn't accept issues or external contributions. It exists as a companion to the tutorials linked above. If you find an issue with the tutorial content, use the feedback form on the tutorial page.

## License

This project is licensed under the [MIT License](LICENSE.md).
