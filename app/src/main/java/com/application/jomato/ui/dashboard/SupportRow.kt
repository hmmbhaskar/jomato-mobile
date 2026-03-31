package com.application.jomato.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.application.jomato.Prefs
import com.application.jomato.R
import com.application.jomato.ui.theme.JomatoTheme

/**
 * Row that opens the system ringtone picker for the Food Rescue alert sound.
 * Selected sound is saved to Prefs and used by RescueService when creating the notification channel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSoundRow() {
    val context = LocalContext.current
    val defaultSoundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.food_rescue_alert}")

    // Resolve a sound URI to a human-readable name
    fun resolveSoundName(uri: String?): String {
        if (uri == null) return "Default (Food Rescue Alert)"
        return try {
            val ringtone = RingtoneManager.getRingtone(context, Uri.parse(uri))
            ringtone?.getTitle(context) ?: "Custom sound"
        } catch (_: Exception) {
            "Custom sound"
        }
    }

    // Reactive state so the UI updates immediately after picking a new sound
    var currentSoundName by remember { mutableStateOf(resolveSoundName(Prefs.getAlertSoundUri(context))) }

    // Launcher for the system ringtone picker
    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            Prefs.setAlertSoundUri(context, uri?.toString())
            Prefs.incrementAlertChannelVersion(context)
            // Update display immediately
            currentSoundName = resolveSoundName(uri?.toString())
        }
    }

    Card(
        onClick = {
            val currentUri = Prefs.getAlertSoundUri(context)?.let { Uri.parse(it) } ?: defaultSoundUri

            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Food Rescue Alert Sound")
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultSoundUri)
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
            }
            ringtoneLauncher.launch(intent)
        },
        colors = CardDefaults.cardColors(containerColor = JomatoTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (JomatoTheme.isDark) 0.dp else 1.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, JomatoTheme.GlassBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JomatoTheme.BrandLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = JomatoTheme.Brand,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Alert Sound",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = JomatoTheme.BrandBlack,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentSoundName,
                    style = MaterialTheme.typography.bodySmall,
                    color = JomatoTheme.TextGray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = JomatoTheme.TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyFaqRow(navController: NavController) {
    Card(
        onClick = { navController.navigate("privacy_faqs") },
        colors = CardDefaults.cardColors(containerColor = JomatoTheme.CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (JomatoTheme.isDark) 0.dp else 1.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, JomatoTheme.GlassBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JomatoTheme.BrandLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.QuestionAnswer,
                    contentDescription = null,
                    tint = JomatoTheme.Brand,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Privacy & FAQs",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = JomatoTheme.BrandBlack,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Data and privacy questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = JomatoTheme.TextGray,
                    fontSize = 12.sp
                )
            }
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = JomatoTheme.TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
