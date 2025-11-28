package com.mtaa.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// We tell the app: "Expect a function called MtaaMap to exist on every platform"
@Composable
expect fun MtaaMap(
    modifier: Modifier = Modifier,
    onLocationSelected: (Double, Double) -> Unit
)