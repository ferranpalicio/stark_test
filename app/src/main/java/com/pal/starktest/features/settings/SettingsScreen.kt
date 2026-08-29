package com.pal.starktest.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pal.starktest.ui.theme.StarkTheme

@Composable
fun SettingsScreen(
    isRiding: Boolean,
    onRidingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(StarkTheme.dimens.spacingLarge)) {
        Text("Simulation", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = StarkTheme.dimens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = isRiding, onCheckedChange = onRidingChanged)
            Text("Riding (connected to bike)")
        }
    }
}
