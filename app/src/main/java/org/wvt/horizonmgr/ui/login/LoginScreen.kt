package org.wvt.horizonmgr.ui.login

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

private const val TAG = "ComposeLogin"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (account: String, avatar: String?, name: String, uid: String) -> Unit,
    onCancel: () -> Unit
) {
    val fabState by viewModel.fabState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var screen by remember { mutableStateOf(0) }
    val gearResource = ImageVector.vectorResource(id = R.drawable.ic_gear_full)

    val gearRotation by rememberInfiniteTransition().animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = InfiniteRepeatableSpec(
            tween(durationMillis = 5000, easing = LinearEasing),
            RepeatMode.Restart
        )
    )

    BackHandler(onBack = {
        if (screen == 1) {
            screen = 0
        } else if (screen == 0) {
            onCancel()
        }
    })

    Box(Modifier.fillMaxSize()) {
        val gearColor = MaterialTheme.colors.onSurface.copy(0.12f)

        // Gear Animations
        Box(
            Modifier
                .size(256.dp)
                .graphicsLayer()
                .offset(x = 128.dp)
                .rotate(gearRotation)
                .align(Alignment.TopEnd)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                imageVector = gearResource,
                contentDescription = null,
                colorFilter = ColorFilter.tint(gearColor)
            )
        }

        Box(
            Modifier
                .size(256.dp)
                .offset(x = (-128).dp)
                .graphicsLayer(rotationZ = gearRotation)
                .align(Alignment.BottomStart)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                imageVector = gearResource,
                contentDescription = null,
                colorFilter = ColorFilter.tint(gearColor)
            )
        }

        // Back button
        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            onClick = {
                if (screen == 1)
                    screen = 0
                else if (screen == 0)
                    onCancel()
            }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
        }

        // Login Page
        AnimatedVisibility(
            visible = screen == 0,
            enter = remember { fadeIn() + slideInHorizontally({ -80 }) },
            exit = remember { fadeOut() + slideOutHorizontally({ -80 }) }
        ) {
            LoginPage(onLoginClicked = { account, password ->
                viewModel.login(account, password, snackbarHostState, onLoginSuccess)
            }, onRegisterRequested = {
                screen = 1
            }, fabState = fabState)
        }

        // RegisterPage
        AnimatedVisibility(
            visible = screen == 1,
            enter = remember { fadeIn() + slideInHorizontally({ 80 }) },
            exit = remember { fadeOut() + slideOutHorizontally({ 80 }) }
        ) {
            RegisterPage(
                fabState = fabState,
                onRegisterRequest = { u, e, p, c ->
                    viewModel.register(u, e, p, c, snackbarHostState) { _, _, _ ->
                        screen = 0
                    }
                },
            )
        }

        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            hostState = snackbarHostState
        )
    }
}