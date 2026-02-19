package com.shopify.example.storefront

// [START complete-tutorial.display-products]
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = { viewModel.addToCart(product) }
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.featuredImage?.url,
                contentDescription = product.title,
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium
                )
                product.firstVariantId?.let { _ ->
                    product.variants.edges.firstOrNull()?.node?.price?.let { price ->
                        Text(
                            text = "$${price.amount} ${price.currencyCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(onClick = onAddToCart) {
                Text("Add")
            }
        }
    }
}

class ProductListViewModel : ViewModel() {
    private val client = StorefrontClient(
        shopDomain = "{shop}.myshopify.com",
        accessToken = "your-storefront-access-token"
    )

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _products.value = client.fetchProducts()
            } catch (e: Exception) {
                Log.e("Products", "Failed to load: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart(product: Product) {
        val variantId = product.firstVariantId ?: return
        viewModelScope.launch {
            try {
                val cart = client.createCart(variantId)
                Log.d("Cart", "Checkout URL: ${cart.checkoutUrl}")
            } catch (e: Exception) {
                Log.e("Cart", "Failed to create cart: ${e.message}")
            }
        }
    }
}
// [END complete-tutorial.display-products]
