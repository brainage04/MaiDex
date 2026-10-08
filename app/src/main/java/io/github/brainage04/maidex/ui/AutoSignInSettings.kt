package io.github.brainage04.maidex.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.brainage04.maidex.AutoLoginUiState

/** Opt-in settings for signing in to DX NET automatically when the app opens. Off by default. */
@Composable
internal fun AutoSignInCard(
    state: AutoLoginUiState,
    busy: Boolean,
    onSaveCredentials: (username: String, password: String) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onForget: () -> Unit,
) {
    var showCredentials by remember { mutableStateOf(false) }
    var confirmForget by remember { mutableStateOf(false) }
    val context = LocalContext.current
    // Action deferred until the notification-permission prompt is answered, so the first
    // automatic update can already post its start/end notifications.
    var afterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        // Notifications are optional; the in-app status still marks start and end.
        afterPermission?.invoke()
        afterPermission = null
    }
    val withNotificationPermission: (() -> Unit) -> Unit = { action ->
        val needsPrompt = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPrompt) {
            afterPermission = action
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Automatic sign-in", fontWeight = FontWeight.Bold)
                    Text(
                        "Sign in with your SEGA ID and update your data in the background whenever " +
                            "MaiDex opens, and when hourly tracking finds your session expired. " +
                            "International DX NET only.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.enabled,
                    enabled = state.supported && !busy,
                    onCheckedChange = { checked ->
                        when {
                            !checked -> onEnabledChange(false)
                            state.username == null || state.pausedReason != null -> showCredentials = true
                            else -> withNotificationPermission { onEnabledChange(true) }
                        }
                    },
                    modifier = Modifier
                        .testTag("auto-sign-in-switch")
                        .semantics { contentDescription = "Automatic sign-in" },
                )
            }
            if (!state.supported) {
                Text(
                    "Automatic sign-in is only available for International (maimaidx-eng.com) accounts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            state.username?.let { username ->
                Text(
                    "Saved SEGA ID: $username (encrypted on this device, never backed up)",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            state.pausedReason?.let { reason ->
                Text(reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
            if (state.pausedReason == null && state.enabled) {
                state.lastResult?.let { result ->
                    Text(
                        "Last automatic update: $result",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.username != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showCredentials = true }, enabled = !busy) {
                        Text("Update credentials")
                    }
                    TextButton(onClick = { confirmForget = true }) { Text("Forget credentials") }
                }
            }
        }
    }

    if (showCredentials) {
        CredentialsDialog(
            initialUsername = state.username.orEmpty(),
            onDismiss = { showCredentials = false },
            onSave = { username, password ->
                showCredentials = false
                withNotificationPermission { onSaveCredentials(username, password) }
            },
        )
    }
    if (confirmForget) {
        AlertDialog(
            onDismissRequest = { confirmForget = false },
            title = { Text("Forget credentials?") },
            text = { Text("Your saved SEGA ID and password are deleted from this device and automatic sign-in is turned off.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmForget = false
                    onForget()
                }) { Text("Forget") }
            },
            dismissButton = { TextButton(onClick = { confirmForget = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun CredentialsDialog(
    initialUsername: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    val valid = username.isNotBlank() && password.isNotEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SEGA ID sign-in") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Use the SEGA ID and password you use on the DX NET sign-in page. They are encrypted " +
                        "with a key kept in Android Keystore and only sent to SEGA's sign-in server.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("SEGA ID") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                    modifier = Modifier.testTag("sega-id-field"),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier.testTag("sega-password-field"),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(username, password) }, enabled = valid) { Text("Save and enable") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
