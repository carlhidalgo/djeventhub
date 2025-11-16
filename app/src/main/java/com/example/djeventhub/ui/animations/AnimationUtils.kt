package com.example.djeventhub.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry

// Animación de entrada/salida para navegación entre pantallas
@OptIn(ExperimentalAnimationApi::class)
fun slideIntoContainer(
    towards: AnimatedContentTransitionScope.SlideDirection,
    initialOffset: (offsetForFullSlide: Int) -> Int = { it },
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(300, easing = FastOutSlowInEasing)
): EnterTransition {
    return slideIntoContainer(
        towards = towards,
        animationSpec = animationSpec,
        initialOffset = initialOffset
    ) + fadeIn(animationSpec = tween(300))
}

@OptIn(ExperimentalAnimationApi::class)
fun slideOutOfContainer(
    towards: AnimatedContentTransitionScope.SlideDirection,
    targetOffset: (offsetForFullSlide: Int) -> Int = { it },
    animationSpec: FiniteAnimationSpec<IntOffset> = tween(300, easing = FastOutSlowInEasing)
): ExitTransition {
    return slideOutOfContainer(
        towards = towards,
        animationSpec = animationSpec,
        targetOffset = targetOffset
    ) + fadeOut(animationSpec = tween(300))
}

// Animación de bounce para elementos clickeables
fun Modifier.bounceClick(
    scaleDown: Float = 0.92f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceClick"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = { onClick() }
            )
        }
}

// Animación de shake (vibración) para errores
fun Modifier.shake(trigger: Boolean): Modifier = composed {
    val shakeOffset by animateFloatAsState(
        targetValue = if (trigger) 0f else 1f,
        animationSpec = repeatable(
            iterations = 3,
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    this.graphicsLayer {
        translationX = if (trigger) {
            (shakeOffset * 10f - 5f)
        } else 0f
    }
}

// Animación de aparición progresiva para listas
fun Modifier.animateItemPlacement(index: Int): Modifier = composed {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        isVisible = true
    }

    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemAppear"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "itemFade"
    )

    this
        .graphicsLayer {
            translationY = offsetY.toPx()
            this.alpha = alpha
        }
}

// Animación de pulso para elementos destacados
@Composable
fun rememberPulseAnimation(
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    duration: Int = 1000
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    ).value
}

// Animación de rotación infinita
@Composable
fun rememberRotationAnimation(duration: Int = 2000): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    ).value
}

// Animación de shimmer para loading states
@Composable
fun rememberShimmerAnimation(): ShimmerColors {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    return ShimmerColors(offset)
}

data class ShimmerColors(val offset: Float)

// Animación de fade in con delay
fun Modifier.fadeInWithDelay(delay: Long = 0): Modifier = composed {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "fadeIn"
    )

    this.graphicsLayer { this.alpha = alpha }
}
