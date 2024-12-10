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
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private const val ImageWidth = 640
private const val ImageHeight = 480
private const val ImageAspectRatio = ImageWidth.toFloat() / ImageHeight.toFloat()
private val EmptyImageBitmap = ImageBitmap(width = ImageWidth, height = ImageHeight)

@Composable
fun Camera(
    url: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
)
{
    val httpClient = LocalHttpClient.current
    val imagesFlow = remember {
        MutableSharedFlow<ImageBitmap>(extraBufferCapacity = 10)
    }
    val image = imagesFlow.collectAsState(initial = EmptyImageBitmap).value

    suspend fun nextImage() {
        runCatching {
            withContext(Dispatchers.IO) {
                httpClient.get {
                    url(urlString = url)
                    timeout {
                        requestTimeoutMillis = 500
                    }
                }
            }
        }.onSuccess { response ->
            if (response.status == HttpStatusCode.OK) {
                val bytes = response.readRawBytes()
                imagesFlow.emit(
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                )
            }
        }.onFailure {
            println(it.message)
        }
    }

    LaunchedEffect(Unit) {
        if (image === EmptyImageBitmap) {
            nextImage()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                launch(context = Dispatchers.Default) {
                    nextImage()
                }
                delay(30.milliseconds)
            }
        }
    }

    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier
            .aspectRatio(ImageAspectRatio)
            .then(modifier),
    )
}

@Composable
fun CameraCard(
    name: String,
    url: String,
    modifier: Modifier = Modifier,
)
{
    var isPlaying by remember {
        mutableStateOf(false)
    }

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
            Camera(url = url, isPlaying = isPlaying)

            FilledTonalIconButton(
                onClick = {
                    isPlaying = !isPlaying
                },
                modifier = Modifier.padding(4.dp),
            )
            {
                Icon(
                    painter = painterResource(
                        id = when (isPlaying) {
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
