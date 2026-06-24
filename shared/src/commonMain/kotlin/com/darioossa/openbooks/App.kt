package com.darioossa.openbooks

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.darioossa.openbooks.navigation.NavigationRoot

@Composable
@Preview
fun App() {
    MaterialTheme {
        NavigationRoot()
    }
}
