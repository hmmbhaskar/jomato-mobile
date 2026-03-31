package com.application.jomato.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.jomato.Prefs
import com.application.jomato.config.UiConfigManager
import com.application.jomato.ui.theme.JomatoTheme
import java.util.Calendar

@Composable
fun DashboardTopBar() {
    val context = LocalContext.current
    val themeMode by Prefs.themeMode.collectAsState()

    val themeIcon = when (themeMode) {
        "dark" -> Icons.Rounded.DarkMode
        "light" -> Icons.Rounded.LightMode
        else -> Icons.Rounded.SettingsBrightness
    }

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning!"
        in 12..16 -> "Good afternoon!"
        in 17..20 -> "Good evening!"
        else -> "Good night!"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = JomatoTheme.Background,
        shadowElevation = if (JomatoTheme.isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val appName = UiConfigManager.config?.metadata?.name?.takeIf { it.isNotBlank() } ?: "JOMATO"
                Text(
                    text = appName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = JomatoTheme.Brand,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleLarge,
                    color = JomatoTheme.BrandBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            IconButton(onClick = { Prefs.cycleThemeMode(context) }) {
                Icon(
                    themeIcon,
                    contentDescription = "Theme: $themeMode",
                    tint = JomatoTheme.TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
