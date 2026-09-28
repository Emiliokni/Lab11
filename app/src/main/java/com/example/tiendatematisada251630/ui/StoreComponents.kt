package com.example.tiendatematisada251630.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.tiendatematisada251630.model.StoreMessage

private enum class ProductImageState {
    Loading,
    Success,
    Error
}

@Composable
fun ProductImage(
    imageUrl: String,
    productName: String,
    modifier: Modifier = Modifier
) {
    var imageState by remember(imageUrl) {
        mutableStateOf(ProductImageState.Loading)
    }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Imagen de $productName",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onLoading = { imageState = ProductImageState.Loading },
            onSuccess = { imageState = ProductImageState.Success },
            onError = { imageState = ProductImageState.Error }
        )

        when (imageState) {
            ProductImageState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            ProductImageState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Imagen no disponible",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            ProductImageState.Success -> Unit
        }
    }
}

@Composable
fun OrderActionButton(
    totalUnits: Int,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
    ) {
        BadgedBox(
            badge = {
                if (totalUnits > 0) {
                    Badge {
                        Text(totalUnits.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Abrir pedido con $totalUnits unidades"
            )
        }
    }
}

@Composable
fun MessagePanel(
    message: StoreMessage,
    modifier: Modifier = Modifier
) {
    val background = if (message.isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val foreground = if (message.isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
    ) {
        Text(
            text = message.text,
            color = foreground,
            modifier = Modifier
                .sizeIn(minHeight = 48.dp)
                .padding(12.dp)
        )
    }
}
