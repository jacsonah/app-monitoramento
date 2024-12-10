package br.univali.pse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.univali.pse.R
import br.univali.pse.ui.Camera

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    name: String,
    baseUrl: String,
    onBack: () -> Unit,
    onSettings: () -> Unit,
)
{
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = name)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    )
    {
        Column(
            modifier = Modifier.padding(paddingValues = it)
        )
        {
            Camera(url = baseUrl, isPlaying = true)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp),
            )
            {
                Text(
                    text = "Movimentar câmera",
                    style = MaterialTheme.typography.titleMedium,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    IconButton(
                        onClick = {}
                    )
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_upward_alt),
                            contentDescription = null,
                        )
                    }

                    Row {
                        Spacer(
                            modifier = Modifier.weight(2f)
                        )

                        IconButton(
                            onClick = {},
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left_alt),
                                contentDescription = null,
                            )
                        }

                        Spacer(
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {},
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_right_alt),
                                contentDescription = null,
                            )
                        }

                        Spacer(
                            modifier = Modifier.weight(2f)
                        )
                    }

                    IconButton(
                        onClick = {}
                    )
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_downward_alt),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
