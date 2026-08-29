package com.pal.starktest.features.userdata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pal.starktest.domain.model.User
import com.pal.starktest.features.common.UiState
import com.pal.starktest.ui.theme.StarkTheme

@Composable
fun UserDataScreen(
    user: UiState<User>,
    modifier: Modifier = Modifier,
) {
    when (user) {
        is UiState.Loading -> LoadingState(modifier)
        is UiState.Empty -> EmptyState(modifier, "No user data available.")
        is UiState.Error -> EmptyState(modifier, "Error: ${user.message}")
        is UiState.Success -> Content(user.data, modifier)
    }
}

@Composable
private fun Content(data: User, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(StarkTheme.dimens.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(StarkTheme.dimens.spacingLarge),
    ) {
        InfoCard("Name", data.name)
        InfoCard("Email", data.email)
        data.phone?.let { InfoCard("Phone", it) }
        data.country?.let { InfoCard("Country", it) }
    }
}

@Composable
private fun InfoCard(title: String, value: String) {
    Card {
        Column(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { CircularProgressIndicator() }
}

@Composable
private fun EmptyState(modifier: Modifier, message: String) {
    Column(
        modifier = modifier.fillMaxSize().padding(StarkTheme.dimens.spacingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { Text(message, style = MaterialTheme.typography.bodyLarge) }
}
