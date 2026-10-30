package com.example.tiendatematisada251630

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tiendatematisada251630.navigation.StoreNavigation
import com.example.tiendatematisada251630.ui.theme.TiendaTematisada251630Theme
import com.example.tiendatematisada251630.viewmodel.StoreViewModel

import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            auth.signInAnonymously()
        }

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

    if (!uiState.isLoaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    StoreNavigation(
        uiState = uiState,
        onToggleFavorite = storeViewModel::toggleFavorite,
        onQueryChange = storeViewModel::onQueryChange,
        onCatalogOrderChange = storeViewModel::onCatalogOrderChange,
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
