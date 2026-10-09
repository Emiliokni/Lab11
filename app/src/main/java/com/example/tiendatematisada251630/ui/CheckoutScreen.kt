package com.example.tiendatematisada251630.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tiendatematisada251630.model.BillingType
import com.example.tiendatematisada251630.model.CheckoutUiState
import com.example.tiendatematisada251630.model.PaymentMethod
import com.example.tiendatematisada251630.model.formatQuetzales

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    checkout: CheckoutUiState,
    totalOrderUnits: Int,
    totalCents: Int,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBillingTypeChange: (BillingType) -> Unit,
    onNitChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val nitFocusRequester = remember { FocusRequester() }
    val isConfirmEnabled by remember(checkout, totalOrderUnits) {
        derivedStateOf { checkout.isFormValid && totalOrderUnits > 0 }
    }

    val fullNameError = checkout.fullNameError.takeIf { checkout.isFullNameTouched }
    val phoneError = checkout.phoneError.takeIf { checkout.isPhoneTouched }
    val nitError = checkout.nitError.takeIf { checkout.isNitTouched }
    val businessNameError = checkout.businessNameError.takeIf { checkout.isBusinessNameTouched }

    fun dismissKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al pedido"
                        )
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Total: ${formatQuetzales(totalCents)}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Resumen del pedido", fontWeight = FontWeight.Bold)
                        Text(
                            "Café de origen · ${formatQuetzales(totalCents)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        "$totalOrderUnits ${if (totalOrderUnits == 1) "unidad" else "unidades"}",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                value = checkout.fullName,
                onValueChange = onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre completo *") },
                placeholder = { Text("Ej. María Morales") },
                singleLine = true,
                isError = fullNameError != null,
                supportingText = fullNameError?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) }
                )
            )

            OutlinedTextField(
                value = checkout.phone,
                onValueChange = onPhoneChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Teléfono / WhatsApp *") },
                placeholder = { Text("Ej. 55123456") },
                singleLine = true,
                isError = phoneError != null,
                supportingText = phoneError?.let { error -> { Text(error) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = if (checkout.billingType == BillingType.NIT) {
                        ImeAction.Next
                    } else {
                        ImeAction.Done
                    }
                ),
                keyboardActions = KeyboardActions(
                    onNext = { nitFocusRequester.requestFocus() },
                    onDone = { dismissKeyboard() }
                )
            )

            OptionGroupTitle("Facturación *")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                BillingType.entries.forEachIndexed { index, type ->
                    SelectableRadioRow(
                        text = type.label,
                        selected = checkout.billingType == type,
                        onClick = {
                            dismissKeyboard()
                            onBillingTypeChange(type)
                        }
                    )
                    if (index < BillingType.entries.lastIndex) HorizontalDivider()
                }
            }

            AnimatedVisibility(visible = checkout.billingType == BillingType.NIT) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "DATOS DE FACTURACIÓN FISCAL",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        OutlinedTextField(
                            value = checkout.nit,
                            onValueChange = onNitChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(nitFocusRequester),
                            label = { Text("NIT *") },
                            singleLine = true,
                            isError = nitError != null,
                            supportingText = nitError?.let { error -> { Text(error) } },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) }
                            )
                        )
                        OutlinedTextField(
                            value = checkout.businessName,
                            onValueChange = onBusinessNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Razón Social / Nombre fiscal *") },
                            placeholder = { Text("Ej. Guzmán Inversiones S.A.") },
                            singleLine = true,
                            isError = businessNameError != null,
                            supportingText = businessNameError?.let { error -> { Text(error) } },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { dismissKeyboard() }
                            )
                        )
                    }
                }
            }

            OptionGroupTitle("Método de pago *")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
            ) {
                PaymentMethod.entries.forEachIndexed { index, method ->
                    SelectableRadioRow(
                        text = method.label,
                        selected = checkout.paymentMethod == method,
                        onClick = {
                            dismissKeyboard()
                            onPaymentMethodChange(method)
                        }
                    )
                    if (index < PaymentMethod.entries.lastIndex) HorizontalDivider()
                }
            }

            Button(
                onClick = {
                    dismissKeyboard()
                    onConfirm()
                },
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text("Confirmar pedido (${formatQuetzales(totalCents)})")
            }

            if (!isConfirmEnabled) {
                Text(
                    text = "Completa los campos obligatorios para continuar.",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun OptionGroupTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SelectableRadioRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}
