package labs.claucookie.pasbuk.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Reusable confirmation dialog for destructive actions.
 *
 * @param title The dialog title
 * @param message The confirmation message
 * @param confirmText The text for the confirm button (defaults to "Confirm")
 * @param dismissText The text for the dismiss button (defaults to "Cancel")
 * @param onConfirm Callback when user confirms
 * @param onDismiss Callback when user dismisses or cancels
 * @param isDestructive Whether this is a destructive action (uses error color for confirm button)
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier
    )
}
