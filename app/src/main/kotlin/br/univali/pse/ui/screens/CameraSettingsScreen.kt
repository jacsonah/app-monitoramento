package br.univali.pse.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import br.univali.pse.R
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char

val OneSizedTimeRegex = Regex(pattern = "^[0-2]\$")
val TwoSizedTimeRegex = Regex(pattern = "^[0-1][0-9]|2[0-3]\$")
val ThreeSizedTimeRegex = Regex(pattern = "^([0-1][0-9]|2[0-3]):\$")
val FourSizedTimeRegex = Regex(pattern = "^([0-1][0-9]|2[0-3]):[0-5]\$")
val FiveSizedTimeRegex = Regex(pattern = "^([0-1][0-9]|2[0-3]):[0-5][0-9]\$")

val TimeFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}

class TimeTextFieldState(time: LocalTime? = null) {
    var time by mutableStateOf(time)
}

@Composable
fun TimeTextField(
    state: TimeTextFieldState,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
)
{
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(text = state.time?.format(format = TimeFormat) ?: "")
        )
    }

    TextField(
        value = textFieldValue,
        onValueChange = { value ->
            when (value.text.length) {
                0 -> textFieldValue = value
                1 -> if (value.text.matches(OneSizedTimeRegex)) {
                    textFieldValue = value
                }
                2 -> if (value.text.matches(TwoSizedTimeRegex)) {
                    textFieldValue = when (value.text > textFieldValue.text) {
                        true -> value.copy(
                            text = "${value.text}:",
                            selection = TextRange(value.text.length + 1),
                        )
                        false -> value
                    }
                }
                3 -> if (value.text.matches(ThreeSizedTimeRegex)) {
                    textFieldValue = value
                }
                4 -> if (value.text.matches(FourSizedTimeRegex)) {
                    textFieldValue = value
                }
                5 -> if (value.text.matches(FiveSizedTimeRegex)) {
                    textFieldValue = value
                }
            }

            runCatching {
                LocalTime.parse(value.text, format = TimeFormat)
            }.onSuccess { time ->
                state.time = time
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSettingsScreen(
    name: String,
    onBack: () -> Unit,
)
{
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Configurar $name")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    )
    {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(paddingValues = it)
                .padding(16.dp)
                .fillMaxSize()
        )
        {
            Row(
                verticalAlignment = Alignment.CenterVertically
            )
            {
                var startRecordingWhenHaveMotion by remember {
                    mutableStateOf(false)
                }

                Text(
                    text = "Iniciar a gravação caso o sensor de movimento seja ativado",
                    modifier = Modifier.weight(1f),
                )

                Switch(
                    checked = startRecordingWhenHaveMotion,
                    onCheckedChange = { checked ->
                        startRecordingWhenHaveMotion = checked
                    },
                )
            }

            Column {
                var startRecordingAtTime by remember {
                    mutableStateOf(false)
                }
                var startTimeState = remember {
                    TimeTextFieldState()
                }
                var finishTimeState = remember {
                    TimeTextFieldState()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(
                        text = "Iniciar a gravação automaticamente no seguinte horário",
                        modifier = Modifier.weight(1f),
                    )

                    Switch(
                        checked = startRecordingAtTime,
                        onCheckedChange = { checked ->
                            startRecordingAtTime = checked
                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp),
                )
                {
                    TimeTextField(
                        state = startTimeState,
                        enabled = startRecordingAtTime,
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        text = "Até",
                        modifier = Modifier.alpha(
                            when (startRecordingAtTime) {
                                true -> 1f
                                false -> 0.38f
                            }
                        ),
                    )

                    TimeTextField(
                        state = finishTimeState,
                        enabled = startRecordingAtTime,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
