package com.application.jomato

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.application.jomato.config.UiConfigManager
import com.application.jomato.sessions.Entity
import com.application.jomato.sessions.SessionMigration
import com.application.jomato.ui.EntityFaqRoute
import com.application.jomato.ui.EntityScreen
import com.application.jomato.ui.FeatureFaqRoute
import com.application.jomato.ui.FeatureScreen
import com.application.jomato.ui.dashboard.DashboardScreen
import com.application.jomato.ui.PrivacyFaqScreen
import com.application.jomato.ui.theme.JomatoTheme
import com.application.jomato.utils.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FileLogger.log(this, "MainActivity", "Application Launched (onCreate)")
        setContent { JomatoApp() }
    }
}

@Composable
fun JomatoApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Prefs.loadThemeMode(context)

        withContext(Dispatchers.IO) {
            UiConfigManager.fetch(context)
        }

        // Integrity check skipped — this is a self-compiled fork.
        // The original check compared the APK hash against the author's
        // known digests, which will never match a self-built APK.

        val migrated = withContext(Dispatchers.IO) {
            SessionMigration.runIfNeeded(context)
        }

        if (migrated) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Your session has been reset. Please log in again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        navController.navigate("dashboard") {
            popUpTo("splash") { inclusive = true }
        }
    }


    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen() }
        composable("dashboard") { DashboardScreen(navController) }
        composable("privacy_faqs") { PrivacyFaqScreen(navController) }

        Entity.entries.forEach { entity ->
            composable(entity.loginHandler.route) {
                entity.loginHandler.LoginScreen(navController)
            }
        }

        composable("feature/{featureId}/{sessionId}") { backStackEntry ->
            FeatureScreen(
                featureId = backStackEntry.arguments?.getString("featureId")!!,
                sessionId = backStackEntry.arguments?.getString("sessionId")!!,
                navController = navController
            )
        }
        composable("feature/{featureId}") { backStackEntry ->
            FeatureScreen(
                featureId = backStackEntry.arguments?.getString("featureId")!!,
                sessionId = null,
                navController = navController
            )
        }
        composable("feature/{featureId}/faq") { backStackEntry ->
            FeatureFaqRoute(
                featureId = backStackEntry.arguments?.getString("featureId")!!,
                navController = navController
            )
        }
        composable("entity/{entityId}") { backStackEntry ->
            EntityScreen(
                entityId = backStackEntry.arguments?.getString("entityId")!!,
                navController = navController
            )
        }
        composable("entity/{entityId}/faq") { backStackEntry ->
            EntityFaqRoute(
                entityId = backStackEntry.arguments?.getString("entityId")!!,
                navController = navController
            )
        }
    }
}

@Composable
fun SplashScreen() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JomatoTheme.Background),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = visible,
            enter = androidx.compose.animation.fadeIn(
                animationSpec = androidx.compose.animation.core.tween(600)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "JOMATO",
                    color = JomatoTheme.Brand,
                    fontSize = 32.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zero Telemetry Edition",
                    color = JomatoTheme.TextGray,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = JomatoTheme.Brand,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
