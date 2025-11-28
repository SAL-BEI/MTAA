package com.mtaa.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun MtaaMap(
    modifier: Modifier,
    onLocationSelected: (Double, Double) -> Unit
) {
    // Default to Nairobi (CBD)
    val nairobi = LatLng(-1.286389, 36.817223)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(nairobi, 15f)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { latLng ->
            // When user clicks the map, send the coordinates back
            onLocationSelected(latLng.latitude, latLng.longitude)
        }
    ) {
        // Place a marker at the center of the camera
        Marker(
            state = MarkerState(position = cameraPositionState.position.target),
            title = "My Shop Location",
            snippet = "Hold and drag to adjust"
        )
    }
}