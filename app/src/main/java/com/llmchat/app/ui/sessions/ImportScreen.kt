package com.llmchat.app.ui.sessions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ImportChatButton(
    viewModel: SessionsViewModel = hiltViewModel(),
    onImported: (Long) -> Unit
) {
    var importError by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Import handled in ViewModel
        }
    }

    importError?.let { error ->
        AlertDialog(
            onDismissRequest = { importError = null },
            title = { Text("Import Failed") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { importError = null }) { Text("OK") }
            }
        )
    }

    OutlinedButton(
        onClick = { launcher.launch(arrayOf("application/json")) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Upload, null)
        Spacer(Modifier.width(8.dp))
        Text("Import Chat Backup")
    }
}
