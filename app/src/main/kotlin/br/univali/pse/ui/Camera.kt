package br.univali.pse.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.univali.pse.LocalHttpClient
import br.univali.pse.R
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val SystemDefaultTimeZone = TimeZone.currentSystemDefault()

private val DateTimeFormat = LocalDateTime.Format {
    yearTwoDigits(baseYear = 2024)
    monthNumber()
    dayOfMonth()
    hour()
    minute()
    second()
    chars("00")
}

private val EmptyImageBitmap = ImageBitmap(width = 362, height = 252)

@Stable
class CameraState(isPlaying: Boolean = false, initialImage: ImageBitmap = EmptyImageBitmap) {
    var isPlaying by mutableStateOf(isPlaying)
    var image by mutableStateOf(initialImage)
}

@Composable
fun Camera(
    baseUrl: String,
    state: CameraState,
    modifier: Modifier = Modifier,
)
{
    val httpClient = LocalHttpClient.current
    val bufferCapacity = 10
    val imageBuffer = remember {
        ArrayDeque<ImageBitmap>(initialCapacity = bufferCapacity)
    }

    LaunchedEffect(Unit) {
        if (state.image === EmptyImageBitmap) {
            val response = withContext(Dispatchers.IO) {
                httpClient.get(
                    urlString = buildString {
                        append(
                            baseUrl,
                            Clock.System
                                .now()
                                .minus(1.minutes)
                                .toLocalDateTime(timeZone = SystemDefaultTimeZone)
                                .format(format = DateTimeFormat),
                            ".jpg",
                        )
                    }
                )
            }

            if (response.status == HttpStatusCode.OK) {
                val bytes = response.readRawBytes()
                state.image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
            }
        }
    }

    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            val semaphore = Semaphore(permits = bufferCapacity)

            launch(context = Dispatchers.Default) {
                while (true) {
                    val url  = buildString {
                        append(
                            baseUrl,
                            Clock.System
                                .now()
                                .minus(1.minutes)
                                .toLocalDateTime(timeZone = SystemDefaultTimeZone)
                                .format(format = DateTimeFormat),
                            ".jpg",
                        )
                    }

                    println(url)

                    launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                httpClient.get {
                                    url(urlString = url)
                                    timeout {
                                        requestTimeoutMillis = 1000
                                    }
                                }
                            }
                        }.onSuccess { response ->
                            if (response.status == HttpStatusCode.OK) {
                                val bytes = response.readRawBytes()
                                semaphore.acquire()
                                imageBuffer.addLast(
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                                )
                            }
                        }
                    }

                    delay(1.seconds)
                }
            }

            launch {
                while (true) {
                    imageBuffer
                        .removeFirstOrNull()
                        ?.let { firstImage ->
                            state.image = firstImage
                            semaphore.release()
                        }

                    delay(timeMillis = 33)
                }
            }
        }
    }

    Image(
        bitmap = state.image,
        contentDescription = null,
        modifier = Modifier
            .aspectRatio(state.image.width.toFloat() / state.image.height.toFloat())
            .then(modifier),
    )
}

@Composable
fun CameraCard(
    name: String,
    baseUrl: String,
    state: CameraState,
    modifier: Modifier = Modifier,
)
{
    OutlinedCard(
        modifier = modifier
    )
    {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )

        Box(
            contentAlignment = Alignment.BottomStart
        )
        {
            Camera(baseUrl = baseUrl, state = state)

            FilledTonalIconButton(
                onClick = {
                    state.isPlaying = !state.isPlaying
                },
                modifier = Modifier.padding(4.dp),
            )
            {
                Icon(
                    painter = painterResource(
                        id = when (state.isPlaying) {
                            true -> R.drawable.pause
                            false -> R.drawable.play
                        }
                    ),
                    contentDescription = null,
                )
            }
        }
    }
}
