package br.univali.pse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    camerasCount: Int,
    cameraItem: @Composable (index: Int) -> Unit,
)
{
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Câmeras")
                }
            )
        }
    )
    {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(all = 8.dp),
            modifier = Modifier.padding(paddingValues = it)
        )
        {
            items(count = camerasCount) { index ->
                cameraItem(index)
            }
        }
    }
}
