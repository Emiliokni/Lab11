package com.example.tiendatematisada251630.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.data.CatalogOrder
import com.example.tiendatematisada251630.model.Product
import com.example.tiendatematisada251630.model.filterProducts
import com.example.tiendatematisada251630.model.formatQuetzales
import com.example.tiendatematisada251630.model.orderCatalog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteProductIds: Set<String>,
    query: String,
    catalogOrder: CatalogOrder,
    totalOrderUnits: Int,
    gridState: LazyGridState,
    isError: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onQueryChange: (String) -> Unit,
    onCatalogOrderChange: (CatalogOrder) -> Unit,
    onProductClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOrderClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var orderMenuExpanded by remember { mutableStateOf(false) }
    val filteredProducts = remember(products, query, catalogOrder) {
        orderCatalog(filterProducts(products, query), catalogOrder)
    }
    val showScrollToTop by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 ||
                gridState.firstVisibleItemScrollOffset > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Café de Origen") },
                actions = {
                    OrderActionButton(totalUnits = totalOrderUnits, onClick = onOrderClick)
                }
            )
        },
        floatingActionButton = {
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        scope.launch { gridState.animateScrollToItem(0) }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Volver arriba"
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isError) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Ocurrió un error al cargar el catálogo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = errorMessage ?: "No se pudo conectar con Firestore.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(onClick = onRetry) {
                    Text("Reintentar")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { newQuery ->
                                onQueryChange(newQuery)
                                scope.launch { gridState.scrollToItem(0) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Buscar productos") },
                            singleLine = true,
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            onQueryChange("")
                                            scope.launch { gridState.scrollToItem(0) }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar búsqueda"
                                        )
                                    }
                                }
                            }
                        )
                        Text(
                            text = "${filteredProducts.size} de ${products.size} productos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        androidx.compose.foundation.layout.Box {
                            TextButton(onClick = { orderMenuExpanded = true }) {
                                Text("Orden: ${catalogOrder.label}")
                            }
                            DropdownMenu(
                                expanded = orderMenuExpanded,
                                onDismissRequest = { orderMenuExpanded = false }
                            ) {
                                CatalogOrder.entries.forEach { choice ->
                                    DropdownMenuItem(
                                        text = { Text(choice.label) },
                                        onClick = {
                                            onCatalogOrderChange(choice)
                                            orderMenuExpanded = false
                                            scope.launch { gridState.scrollToItem(0) }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredProducts.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("No encontramos productos.")
                            Button(
                                onClick = {
                                    onQueryChange("")
                                    scope.launch { gridState.scrollToItem(0) }
                                }
                            ) {
                                Text("Limpiar búsqueda")
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredProducts,
                        key = { product -> product.id }
                    ) { product ->
                        val isFavorite = product.id in favoriteProductIds
                        CatalogProductCard(
                            product = product,
                            isFavorite = isFavorite,
                            onClick = { onProductClick(product.id) },
                            onFavoriteClick = { onToggleFavorite(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogProductCard(
    product: Product,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            ProductImage(
                imageUrl = product.thumbnailUrl,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.sizeIn(minWidth = 32.dp, minHeight = 32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
            Text(
                text = formatQuetzales(product.priceCents),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (product.stock > 0) "${product.stock} disponibles" else "Agotado",
                style = MaterialTheme.typography.bodySmall,
                color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
            )
        }
    }
}
