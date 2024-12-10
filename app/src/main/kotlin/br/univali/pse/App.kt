package br.univali.pse

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.univali.pse.data.Camera
import br.univali.pse.ui.CameraCard
import br.univali.pse.ui.screens.CameraScreen
import br.univali.pse.ui.screens.CameraSettingsScreen
import br.univali.pse.ui.screens.MainScreen
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable

sealed interface AppRoutes {
    @Serializable
    data object MainScreen : AppRoutes

    @Serializable
    data class CameraScreen(val id: Int) : AppRoutes

    @Serializable
    data class CameraSettingsScreen(val id: Int) : AppRoutes
}

val LocalHttpClient = staticCompositionLocalOf<HttpClient> {
    error("HttpClient not provided!")
}

@Composable
fun App()
{
    val cameras = remember {
        listOf(
            Camera(id = 0, name = "Câmera 3", baseUrl = "https://camerastamoios.vwi.com.br/KM65/KM%2065_"),
        )
    }
    val httpClient = remember {
        HttpClient()
    }
    val navController = rememberNavController()

    CompositionLocalProvider(LocalHttpClient provides httpClient) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.MainScreen,
        )
        {
            composable<AppRoutes.MainScreen> {
                MainScreen(
                    camerasCount = cameras.size,
                    cameraItem = { index ->
                        CameraCard(
                            name = cameras[index].name,
                            baseUrl = cameras[index].baseUrl,
                            modifier = Modifier.clickable {
                                navController.navigate(AppRoutes.CameraScreen(id = index)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    },
                )
            }

            composable<AppRoutes.CameraScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<AppRoutes.CameraScreen>()
                CameraScreen(
                    name = cameras[route.id].name,
                    baseUrl = cameras[route.id].baseUrl,
                    onBack = {
                        navController.popBackStack(route = route, inclusive = true)
                    },
                    onSettings = {
                        navController.navigate(AppRoutes.CameraSettingsScreen(id = route.id)) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable<AppRoutes.CameraSettingsScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<AppRoutes.CameraSettingsScreen>()
                CameraSettingsScreen(
                    name = cameras[route.id].name,
                    onBack = {
                        navController.popBackStack(route = route, inclusive = true)
                    },
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            httpClient.close()
        }
    }
}
