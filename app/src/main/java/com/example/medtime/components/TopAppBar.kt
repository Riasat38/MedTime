package com.example.medtime.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedTimeTopAppBar(
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
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

        colors = colors
    )
}

/**
 * Preview for the MedTimeTopAppBar with just a title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun TopAppBarPreview() {
    MaterialTheme {
        MedTimeTopAppBar()
    }
}

/**
 * Preview for the MedTimeTopAppBar with navigation icon
 */
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