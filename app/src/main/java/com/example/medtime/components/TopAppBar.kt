package com.example.medtime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medtime.ui.theme.gradientBrush


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedTimeTopAppBar(
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    showLogout: Boolean = false,
    onLogoutClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = "MedTime",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            navigationIcon?.let { icon ->
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = {
            if (showLogout) {
                IconButton(onClick = onLogoutClick) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(brush = gradientBrush, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        colors = colors
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TopAppBarPreview() {
    MaterialTheme {
        MedTimeTopAppBar()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TopAppBarWithNavigationPreview() {
    MaterialTheme {
        MedTimeTopAppBar(
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = {}
        )
    }
}