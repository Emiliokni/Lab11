package com.example.tiendatematisada251630.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.model.Product
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteProductIds: Set<String>,
    onProductClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Café de Origen")
                }
            )
        }
    ) { innerPadding ->
        CatalogContent(
            products = products,
            favoriteProductIds = favoriteProductIds,
            onProductClick = onProductClick,
            onFavoriteClick = onFavoriteClick,
            contentPadding = innerPadding
        )
    }
}

@Composable
private fun CatalogContent(
    products: List<Product>,
    favoriteProductIds: Set<String>,
    onProductClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Nuestro catálogo",
            fontWeight = FontWeight.Bold
        )

        products.forEach { product ->
            val isFavorite = product.id in favoriteProductIds

            ProductCard(
                product = product,
                isFavorite = isFavorite,
                onOpen = {
                    onProductClick(product.id)
                },
                onFavoriteClick = {
                    onFavoriteClick(product.id)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold
            )

            Text(product.description)

            Text(
                text = String.format(
                    Locale.getDefault(),
                    "Q %.2f",
                    product.price
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onOpen) {
                    Text("Ver detalle")
                }

                TextButton(onClick = onFavoriteClick) {
                    Text(
                        if (isFavorite) {
                            "★ Favorito"
                        } else {
                            "☆ Marcar favorito"
                        }
                    )
                }
            }
        }
    }
}