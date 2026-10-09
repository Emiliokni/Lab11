package com.example.tiendatematisada251630.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.model.Product
import com.example.tiendatematisada251630.model.StoreMessage
import com.example.tiendatematisada251630.model.formatQuetzales

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    producerName: String,
    isFavorite: Boolean,
    quantityInOrder: Int,
    totalOrderUnits: Int,
    message: StoreMessage?,
    onFavoriteClick: () -> Unit,
    onProducerClick: () -> Unit,
    onAddToOrder: () -> Unit,
    onOrderClick: () -> Unit,
    onBack: () -> Unit
) {
    var showTechnicalDetails by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                actions = {
                    OrderActionButton(totalUnits = totalOrderUnits, onClick = onOrderClick)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProductImage(
                imageUrl = product.imageUrl,
                productName = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Text(text = product.name, fontWeight = FontWeight.Bold)
            Text(product.description)
            Text(
                text = formatQuetzales(product.priceCents),
                fontWeight = FontWeight.Bold
            )
            Text("${product.stock} disponibles · $quantityInOrder en el pedido")

            Button(onClick = onAddToOrder) {
                Text("Agregar al pedido")
            }

            if (message != null) {
                MessagePanel(message = message, modifier = Modifier.fillMaxWidth())
            }

            TextButton(onClick = onFavoriteClick) {
                Text(if (isFavorite) "★ Quitar de favoritos" else "☆ Agregar a favoritos")
            }

            HorizontalDivider()

            TextButton(onClick = { showTechnicalDetails = !showTechnicalDetails }) {
                Text(if (showTechnicalDetails) "Ocultar ficha técnica" else "Ver ficha técnica")
            }
            if (showTechnicalDetails) {
                Text(product.technicalDetails)
            }

            HorizontalDivider()
            Text("Productor: $producerName")
            Button(onClick = onProducerClick) {
                Text("Ver perfil del productor")
            }
        }
    }
}
