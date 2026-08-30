package com.pal.starktest.features.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pal.starktest.domain.model.Session
import com.pal.starktest.features.common.UiState
import com.pal.starktest.ui.theme.StarkTheme
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
fun SessionsScreen(
    sessions: UiState<List<Session>>,
    modifier: Modifier = Modifier,
) {
    when (sessions) {
        is UiState.Loading -> LoadingState(modifier)
        is UiState.Empty -> EmptyState(modifier, NO_SESSIONS)
        is UiState.Error -> EmptyState(modifier, "Error: ${sessions.message}")
        is UiState.Success ->
            // A ride only becomes a row once it ends, so an empty list is the normal first-run
            // state rather than a failure.
            if (sessions.data.isEmpty()) EmptyState(modifier, NO_SESSIONS)
            else Content(sessions.data, modifier)
    }
}

private const val NO_SESSIONS = "No sessions yet."

@Composable
private fun Content(sessions: List<Session>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(StarkTheme.dimens.spacingMedium),
    ) {
        items(sessions, key = { it.id }) { session -> SessionCard(session) }
    }
}

@Composable
private fun SessionCard(session: Session) {
    Card {
        Column(modifier = Modifier.padding(StarkTheme.dimens.spacingLarge)) {
            Text("Session #${session.id}", style = MaterialTheme.typography.labelMedium)
            Text(
                String.format(
                    LocalLocale.current.platformLocale,
                    "%s · %.1f km · max %.0f km/h",
                    formatDuration(session.durationS),
                    session.distanceKm,
                    session.maxSpeedKmh,
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val remainder = seconds % 60
    return if (minutes > 0) "$minutes min $remainder s" else "$remainder s"
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
        modifier = modifier
            .fillMaxSize()
            .padding(StarkTheme.dimens.spacingExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { Text(message, style = MaterialTheme.typography.bodyLarge) }
}
