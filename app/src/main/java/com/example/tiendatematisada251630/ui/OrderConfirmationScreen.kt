package com.example.tiendatematisada251630.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.model.OrderReceipt
import com.example.tiendatematisada251630.model.formatQuetzales

@Composable
fun OrderConfirmationScreen(
    receipt: OrderReceipt,
    onCatalog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "¡Pedido confirmado!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Orden registrada exitosamente en su tienda.",
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReceiptRow("Folio", receipt.folio, emphasizeValue = true)
                ReceiptRow("Cliente", receipt.clientName)
                ReceiptRow("Facturación", receipt.billingDescription)
                ReceiptRow("Método de pago", receipt.paymentDescription)
                HorizontalDivider()
                ReceiptRow(
                    label = "Total del pedido",
                    value = formatQuetzales(receipt.totalCents),
                    emphasizeValue = true
                )
            }
        }

        Button(
            onClick = onCatalog,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .height(52.dp)
        ) {
            Text("Volver al catálogo")
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    emphasizeValue: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            modifier = Modifier.weight(0.38f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.62f),
            textAlign = TextAlign.End,
            fontWeight = if (emphasizeValue) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasizeValue) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
