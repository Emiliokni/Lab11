package com.example.tiendatematisada251630

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendatematisada251630.navigation.StoreNavigation
import com.example.tiendatematisada251630.ui.theme.TiendaTematisada251630Theme
import com.example.tiendatematisada251630.viewmodel.StoreViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TiendaTematisada251630Theme {
                StoreApp()
            }
        }
    }
}

@Composable
private fun StoreApp(
    storeViewModel: StoreViewModel = viewModel()
) {
    val uiState =
        storeViewModel.uiState.collectAsStateWithLifecycle().value

    StoreNavigation(
        uiState = uiState,
        onToggleFavorite = storeViewModel::toggleFavorite,
        onQueryChange = storeViewModel::onQueryChange,
        onAddToOrder = storeViewModel::addToOrder,
        onDecreaseOrder = storeViewModel::decreaseOrder,
        onRemoveFromOrder = storeViewModel::removeFromOrder,
        onFullNameChange = storeViewModel::onFullNameChange,
        onPhoneChange = storeViewModel::onPhoneChange,
        onBillingTypeChange = storeViewModel::onBillingTypeChange,
        onNitChange = storeViewModel::onNitChange,
        onBusinessNameChange = storeViewModel::onBusinessNameChange,
        onPaymentMethodChange = storeViewModel::onPaymentMethodChange,
        onConfirmOrder = storeViewModel::confirmOrder
    )
}
