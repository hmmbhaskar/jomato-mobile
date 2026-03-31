package com.application.jomato.entity.zomato.rescue

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.jomato.R
import androidx.core.content.ContextCompat
import com.application.jomato.entity.zomato.ZomatoManager
import com.application.jomato.entity.zomato.api.ApiClient
import com.application.jomato.entity.zomato.api.TabbedHomeEssentials
import com.application.jomato.entity.zomato.api.UserLocation
import com.application.jomato.ui.theme.JomatoTheme
import com.application.jomato.utils.FileLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "FoodRescueContent"

private sealed class ScreenState {
    object Loading : ScreenState()
    data class Error(val message: String) : ScreenState()
    data class Active(val state: FoodRescueState) : ScreenState()
    data class Setup(
        val locations: List<UserLocation>,
        val selectedLocations: Set<Int>,  // addressIds
        val essentialsMap: Map<Int, TabbedHomeEssentials>,  // addressId -> essentials
        val fetchingAddressIds: Set<Int>,  // addressIds currently loading
        val isFetchingAll: Boolean
    ) : ScreenState()
}

@Composable
fun FoodRescueContent(sessionId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var screenState by remember { mutableStateOf<ScreenState>(ScreenState.Loading) }

    fun loadActiveOrLocations(accessToken: String) {
        val activeState = ZomatoManager.getFoodRescueState(context)
        if (activeState != null) {
            screenState = ScreenState.Active(activeState)
            return
        }
        scope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    ApiClient.getUserLocations(context, accessToken)
                }
                if (res.success) {
                    val locs = res.data
                    // Auto-select first location and fetch its essentials
                    val firstLoc = locs.firstOrNull()
                    val initialSelected = if (firstLoc != null) setOf(firstLoc.addressId) else emptySet()

                    screenState = ScreenState.Setup(
                        locations = locs,
                        selectedLocations = initialSelected,
                        essentialsMap = emptyMap(),
                        fetchingAddressIds = initialSelected,
                        isFetchingAll = false
                    )

                    // Fetch essentials for the first location
                    if (firstLoc != null) {
                        val ess = withContext(Dispatchers.IO) {
                            ApiClient.getTabbedHomeEssentials(context, firstLoc.cellId, firstLoc.addressId, accessToken)
                        }
                        screenState = (screenState as? ScreenState.Setup)?.let { s ->
                            val newMap = if (ess != null) s.essentialsMap + (firstLoc.addressId to ess) else s.essentialsMap
                            s.copy(
                                essentialsMap = newMap,
                                fetchingAddressIds = s.fetchingAddressIds - firstLoc.addressId
                            )
                        } ?: screenState
                    }
                } else {
                    screenState = ScreenState.Setup(
                        locations = emptyList(),
                        selectedLocations = emptySet(),
                        essentialsMap = emptyMap(),
                        fetchingAddressIds = emptySet(),
                        isFetchingAll = false
                    )
                }
            } catch (e: Exception) {
                FileLogger.log(context, TAG, "Failed to load locations: ${e.message}", e)
                screenState = ScreenState.Error("Failed to load locations.")
            }
        }
    }

    LaunchedEffect(sessionId) {
        val session = ZomatoManager.getSession(context, sessionId)
        if (session == null) {
            screenState = ScreenState.Error("Session not found.")
            return@LaunchedEffect
        }

        val user = withContext(Dispatchers.IO) {
            try { ApiClient.getUserInfo(context, session.accessToken) } catch (_: Exception) { null }
        }
        if (user == null) {
            screenState = ScreenState.Error("Session expired. Please log in again.")
            return@LaunchedEffect
        }

        FileLogger.log(context, TAG, "Session valid for ${user.name}")
        loadActiveOrLocations(session.accessToken)
    }

    // Polls every 2 seconds to detect when service is stopped externally (e.g. notification stop button)
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            if (screenState is ScreenState.Active && !ZomatoManager.isFoodRescueActive(context)) {
                val session = ZomatoManager.getSession(context, sessionId)
                if (session != null) loadActiveOrLocations(session.accessToken)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (val state = screenState) {
                is ScreenState.Loading -> LoadingView()
                is ScreenState.Error -> ErrorView(state.message)
                is ScreenState.Active -> ActiveView(
                    state = state.state,
                    onStop = {
                        RescuePermissionUtils.deactivateRescue(context)
                        screenState = ScreenState.Loading
                        val session = ZomatoManager.getSession(context, sessionId)
                        if (session != null) loadActiveOrLocations(session.accessToken)
                    }
                )
                is ScreenState.Setup -> SetupView(
                    state = state,
                    onLocationToggled = { loc ->
                        val session = ZomatoManager.getSession(context, sessionId) ?: return@SetupView
                        val currentSetup = screenState as? ScreenState.Setup ?: return@SetupView
                        val addressId = loc.addressId
                        val isSelected = addressId in currentSetup.selectedLocations

                        if (isSelected) {
                            // Deselect
                            screenState = currentSetup.copy(
                                selectedLocations = currentSetup.selectedLocations - addressId
                            )
                        } else {
                            // Select — and fetch essentials if not already fetched
                            val newSelected = currentSetup.selectedLocations + addressId
                            val needsFetch = addressId !in currentSetup.essentialsMap

                            screenState = currentSetup.copy(
                                selectedLocations = newSelected,
                                fetchingAddressIds = if (needsFetch) currentSetup.fetchingAddressIds + addressId else currentSetup.fetchingAddressIds
                            )

                            if (needsFetch) {
                                scope.launch {
                                    val ess = withContext(Dispatchers.IO) {
                                        ApiClient.getTabbedHomeEssentials(context, loc.cellId, loc.addressId, session.accessToken)
                                    }
                                    screenState = (screenState as? ScreenState.Setup)?.let { s ->
                                        val newMap = if (ess != null) s.essentialsMap + (addressId to ess) else s.essentialsMap
                                        s.copy(
                                            essentialsMap = newMap,
                                            fetchingAddressIds = s.fetchingAddressIds - addressId
                                        )
                                    } ?: screenState
                                }
                            }
                        }
                    },
                    onStart = {
                        val setup = screenState as? ScreenState.Setup ?: return@SetupView

                        // Build monitored addresses list from selected locations
                        val addresses = setup.selectedLocations.mapNotNull { addressId ->
                            val loc = setup.locations.find { it.addressId == addressId } ?: return@mapNotNull null
                            val ess = setup.essentialsMap[addressId] ?: return@mapNotNull null
                            if (ess.foodRescue == null) return@mapNotNull null
                            MonitoredAddress(essentials = ess, location = loc)
                        }

                        if (addresses.isEmpty()) return@SetupView

                        RescuePermissionUtils.activateRescue(context, addresses, sessionId)
                        screenState = ScreenState.Active(
                            FoodRescueState(addresses, System.currentTimeMillis())
                        )
                    }
                )
            }
        }
    }
}

// ── Loading ─────────────────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = JomatoTheme.Brand)
    }
}

// ── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = JomatoTheme.TextGray,
            fontSize = 14.sp
        )
    }
}

// ── Active monitoring ────────────────────────────────────────────────────────

@Composable
private fun ActiveView(
    state: FoodRescueState,
    onStop: () -> Unit
) {
    RescueActiveView(state = state, onStopClick = onStop)
}

// ── Location selection + start ───────────────────────────────────────────────

@Composable
private fun SetupView(
    state: ScreenState.Setup,
    onLocationToggled: (UserLocation) -> Unit,
    onStart: () -> Unit
) {
    val context = LocalContext.current

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            RescuePermissionUtils.checkBattery(
                context,
                onShowDialog = { RescuePermissionUtils.openBatterySettings(context) },
                onSuccess = onStart
            )
        }
    }

    fun onStartClick() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            RescuePermissionUtils.checkBattery(
                context,
                onShowDialog = { RescuePermissionUtils.openBatterySettings(context) },
                onSuccess = onStart
            )
        }
    }

    if (state.locations.isEmpty()) {
        EmptyLocationsView()
        return
    }

    val selectedCount = state.selectedLocations.size
    val hasEssentialsForAll = state.selectedLocations.all { id ->
        state.essentialsMap[id]?.foodRescue != null
    }
    val isAnyFetching = state.fetchingAddressIds.isNotEmpty()
    val canStart = selectedCount > 0 && hasEssentialsForAll && !isAnyFetching

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header with count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT ADDRESSES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = JomatoTheme.TextMuted,
                    letterSpacing = 1.2.sp
                )
                if (selectedCount > 0) {
                    Text(
                        text = "$selectedCount selected",
                        fontSize = 12.sp,
                        color = JomatoTheme.Brand,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            state.locations.forEachIndexed { index, loc ->
                val isSelected = loc.addressId in state.selectedLocations
                val isFetching = loc.addressId in state.fetchingAddressIds
                val essentials = state.essentialsMap[loc.addressId]
                val hasNoFoodRescue = essentials != null && essentials.foodRescue == null

                RescueLocationItem(
                    location = loc,
                    isSelected = isSelected,
                    isFetching = isFetching,
                    hasNoFoodRescue = hasNoFoodRescue,
                    onClick = { onLocationToggled(loc) }
                )
                if (index < state.locations.lastIndex) {
                    Divider(
                        color = JomatoTheme.Divider,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { onStartClick() },
            enabled = canStart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(50.dp)
                .shadow(if (canStart) 4.dp else 0.dp, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = JomatoTheme.Brand,
                contentColor = JomatoTheme.Background,
                disabledContainerColor = JomatoTheme.Brand.copy(alpha = 0.3f),
                disabledContentColor = JomatoTheme.Background.copy(alpha = 0.5f)
            )
        ) {
            if (isAnyFetching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = JomatoTheme.Background
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = when {
                    isAnyFetching -> "Loading…"
                    selectedCount <= 1 -> "START MONITORING"
                    else -> "MONITOR $selectedCount ADDRESSES"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ── Empty locations fallback ─────────────────────────────────────────────────

@Composable
private fun EmptyLocationsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.saved_address_pointer),
            contentDescription = "Add an address in Zomato",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.FillWidth
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Add an address in Zomato to get started",
            fontSize = 13.sp,
            color = JomatoTheme.TextGray,
            textAlign = TextAlign.Center
        )
    }
}