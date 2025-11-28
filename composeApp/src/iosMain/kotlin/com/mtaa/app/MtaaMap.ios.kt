package com.mtaa.app

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun MtaaMap(
    modifier: Modifier,
    onLocationSelected: (Double, Double) -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("Google Maps is Android-only for this demo.")
    }
}