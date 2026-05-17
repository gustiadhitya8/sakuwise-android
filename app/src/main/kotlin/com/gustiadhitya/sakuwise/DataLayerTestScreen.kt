package com.gustiadhitya.sakuwise

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DataLayerTestScreen(
    viewModel: DataLayerTestViewModel = hiltViewModel(),
) {
    val accountCount by viewModel.accountCount.collectAsStateWithLifecycle()
    var cryptoResult by remember { mutableStateOf("(not tested)") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "Data Layer Test",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(text = "DB initialized: ✅")
            Text(text = "Accounts in DB: $accountCount")
            Button(onClick = { viewModel.insertDummyAccount() }) {
                Text("Insert dummy account")
            }
            Button(onClick = { viewModel.testEncryptDecrypt { cryptoResult = it } }) {
                Text("Test encrypt/decrypt")
            }
            Text(text = "Crypto result: $cryptoResult")
        }
    }
}
