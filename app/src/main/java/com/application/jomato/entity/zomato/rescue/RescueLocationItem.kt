package com.application.jomato.entity.zomato.rescue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.jomato.entity.zomato.api.UserLocation
import com.application.jomato.ui.theme.JomatoTheme

@Composable
fun RescueLocationItem(
    location: UserLocation,
    isSelected: Boolean,
    isFetching: Boolean = false,
    hasNoFoodRescue: Boolean = false,
    onClick: () -> Unit
) {
    val isDisabled = hasNoFoodRescue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = if (isDisabled) JomatoTheme.TextMuted else JomatoTheme.TextGray,
            modifier = Modifier.size(20.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDisabled) JomatoTheme.TextMuted else JomatoTheme.BrandBlack,
                fontSize = 14.sp
            )
            Text(
                text = if (hasNoFoodRescue) "Food Rescue not available here" else location.fullAddress,
                style = MaterialTheme.typography.bodySmall,
                color = if (hasNoFoodRescue) JomatoTheme.Warning else JomatoTheme.TextGray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isFetching) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = JomatoTheme.Brand
            )
        } else if (!isDisabled) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = JomatoTheme.Brand,
                    uncheckedColor = JomatoTheme.TextGray
                )
            )
        }
    }
}
