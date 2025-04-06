package com.ys.phdmama.ui.screens.billing

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.SnackbarHost
import com.android.billingclient.api.*
import com.ys.phdmama.ui.main.MainActivity
import com.ys.phdmama.viewmodel.UserDataViewModel
import kotlinx.coroutines.launch

// Billing Screen with Google Play Billing Library Integration
@Composable
fun BillingScreen() {
    val context = LocalContext.current
    val activity = context as Activity
    val scope = rememberCoroutineScope()
    val userViewModel = UserDataViewModel()

    var products by remember { mutableStateOf<List<ProductDetails>>(emptyList()) }
    var purchaseStatus by remember { mutableStateOf("") }
    var debugMessage by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductDetails?>(null) }

    val billingClientState = remember { mutableStateOf<BillingClient?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showMessage by remember { mutableStateOf(false) }

    // Initialize Billing Listener
    val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d("Billing", "Billing Result Code: ${billingResult.responseCode}")

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    Log.d("Billing", "Purchase State: ${purchase.purchaseState}")

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d("Billing", "Purchase completed: ${purchase.orderId}")
                        // Consume the purchase
                        consumePurchase(purchase, billingClientState.value)

                        // Update user role
                        userViewModel.updateUserRole(
                            newRole = "born",
                            onSuccess = {
                                Log.d("Billing", "User role updated successfully")
                                showMessage = true },
                            onFailure = { exception ->
                                Log.e("Billing", "Failed to update user role: ${exception.message}")
                            }
                        )
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        Log.d("Billing", "Purchase is pending.")
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Log.d("Billing", "Purchase cancelled by user.")
            } else {
                Log.e("Billing", "Purchase failed: ${billingResult.debugMessage}")
            }
        }

    if (showMessage) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Purchase successful! Redirecting...")
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }, 2000) // Delay for 2 seconds before navigating
        }
    }

    SnackbarHost(hostState = snackbarHostState)

    // Initialize Billing Client
    LaunchedEffect(Unit) {
        val billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClientState.value = billingClient

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryExistingPurchases(billingClient)

                    // 🔥 Force fresh data by invalidating cache
//                    billingClient.invalidate()

                    val queryProductParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId("premium")
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build()
                            )
                        )
                        .build()

                    billingClient.queryProductDetailsAsync(queryProductParams) { result, productList ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            scope.launch {
                                products = productList
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                debugMessage = "Billing service disconnected"
            }
        })
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = debugMessage, color = MaterialTheme.colorScheme.error)

        if (products.isEmpty()) {
            CircularProgressIndicator()
        } else {
            products.forEach { productDetails ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
                        selectedProduct = productDetails
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = productDetails.name, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Price: ${productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: "N/A"}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                selectedProduct?.let { product ->
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(product)
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClientState.value?.launchBillingFlow(activity, billingFlowParams)
                } ?: run {
                    purchaseStatus = "Please select a product first"
                }
            },
            enabled = selectedProduct != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Purchase Selected Product")
        }

        if (purchaseStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = purchaseStatus, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Function to Consume the Purchase
fun consumePurchase(purchase: Purchase, billingClient: BillingClient?) {
    billingClient?.let {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        it.consumeAsync(consumeParams) { billingResult, purchaseToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                println("Purchase consumed successfully: $purchaseToken")
            } else {
                println("Failed to consume purchase: ${billingResult.debugMessage}")
            }
        }
    }
}


// Function to Query and Consume Existing Purchases
fun queryExistingPurchases(billingClient: BillingClient) {
    val params = QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.INAPP)
        .build()

    billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            for (purchase in purchases) {
                Log.d("Billing", "Existing Purchase: ${purchase.purchaseToken}")
                consumePurchase(purchase, billingClient)
            }
        }
    }
}
