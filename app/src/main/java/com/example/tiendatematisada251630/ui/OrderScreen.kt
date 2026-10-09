package com.example.tiendatematisada251630.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.res.painterResource
import com.example.tiendatematisada251630.R
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.model.OrderLine
import com.example.tiendatematisada251630.model.StoreMessage
import com.example.tiendatematisada251630.model.formatQuetzales

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    orderLines: List<OrderLine>,
    totalCents: Int,
    message: StoreMessage?,
    onIncrease: (String) -> Unit,
    onDecrease: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
    onCatalog: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi pedido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (orderLines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("No hay productos en tu pedido.")
                    Text("Total ${formatQuetzales(0)}", fontWeight = FontWeight.Bold)
                    Button(onClick = onCatalog) {
                        Text("Volver al catálogo")
                    }
                    Button(onClick = onCheckout, enabled = false) {
                        Text("Continuar al checkout")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (message != null) {
                    item(key = "message-${message.id}") {
                        MessagePanel(
                            message = message,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                items(items = orderLines, key = { it.product.id }) { line ->
                    OrderLineCard(
                        line = line,
                        onIncrease = { onIncrease(line.product.id) },
                        onDecrease = { onDecrease(line.product.id) },
                        onRemove = { onRemove(line.product.id) }
                    )
                }

                item(key = "total") {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = formatQuetzales(totalCents),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item(key = "checkout") {
                    Button(
                        onClick = onCheckout,
                        enabled = orderLines.sumOf { it.quantity } > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .sizeIn(minHeight = 52.dp)
                    ) {
                        Text("Continuar al checkout")
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderLineCard(
    line: OrderLine,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(line.product.name, fontWeight = FontWeight.Bold)
            Text("${formatQuetzales(line.product.priceCents)} por unidad")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_remove),
                            contentDescription = "Disminuir ${line.product.name}"
                        )
                    }
                    Text(line.quantity.toString(), fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar ${line.product.name}")
                    }
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar ${line.product.name}")
                }
            }
            Text(
                text = "Subtotal ${formatQuetzales(line.subtotalCents)}",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
