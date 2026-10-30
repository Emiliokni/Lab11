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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.model.Product
import com.example.tiendatematisada251630.data.CatalogOrder
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
    onQueryChange: (String) -> Unit,
    onCatalogOrderChange: (CatalogOrder) -> Unit,
    onProductClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
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
                gridState.firstVisibleItemScrollOffset > 240
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
                        androidx.compose.material3.Button(
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
                    key = Product::id,
                    contentType = { "product" }
                ) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = product.id in favoriteProductIds,
                        onOpen = { onProductClick(product.id) },
                        onFavoriteClick = { onFavoriteClick(product.id) }
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(88.dp))
            }
        }
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
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProductImage(
                imageUrl = product.thumbnailUrl,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
            )
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatQuetzales(product.priceCents),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (product.stock == 0) "Agotado" else "${product.stock} disponibles",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ver detalle",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                androidx.compose.material3.TextButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = if (isFavorite) {
                            "Quitar ${product.name} de favoritos"
                        } else {
                            "Agregar ${product.name} a favoritos"
                        }
                    )
                }
            }
        }
    }
}
