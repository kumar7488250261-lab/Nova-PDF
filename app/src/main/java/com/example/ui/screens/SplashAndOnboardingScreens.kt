package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PdfNovaButton
import com.example.ui.components.PdfNovaLogoMark
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Official PDFNova Splash Screen.
 * Implements subtle, professional startup sequence:
 * 1. PDFNova logo appears smoothly.
 * 2. Logo performs very subtle scale-in.
 * 3. Logo settles into position.
 * 4. "PDFNova" fades in.
 * 5. "One App. Every PDF Tool." fades in.
 * 6. Transitions swiftly without artificial waiting (no delay 5000).
 */
@Composable
fun SplashScreen(
    onInitializationComplete: () -> Unit
) {
    val scale = remember { Animatable(0.88f) }
    val titleAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo subtle scale-in
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
        // Title fade-in
        launch {
            delay(100)
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260)
            )
        }
        // Tagline fade-in
        launch {
            delay(180)
            taglineAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260)
            )
        }

        // Swift, premium transition
        delay(480)
        onInitializationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official PDFNova Brand Logo Mark (white sheet, folded corner, geometric N)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                PdfNovaLogoMark(size = 96.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("splash_title_row")
            ) {
                Text(
                    text = "PDF",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = titleAlpha.value),
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Nova",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = titleAlpha.value),
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Official Tagline
            Text(
                text = "One App. Every PDF Tool.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = taglineAlpha.value),
                letterSpacing = 0.5.sp,
                modifier = Modifier.testTag("splash_tagline")
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen"),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page indicator dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                if (pagerState.currentPage == 3) {
                    PdfNovaButton(
                        text = "Get Started",
                        onClick = onFinishOnboarding,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "onboarding_get_started"
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onFinishOnboarding,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("onboarding_skip")
                        ) {
                            Text(
                                text = "Skip",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("onboarding_next")
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = "PDFNova",
                    subtitle = "One App. Every PDF Tool.",
                    description = "Create, edit, convert, organize, and protect your documents with unmatched simplicity and power.",
                    useLogo = true,
                    fallbackIcon = Icons.Default.Description,
                    badgeList = listOf("Fast", "Reliable", "All-in-One")
                )
                1 -> OnboardingPage(
                    title = "33 powerful tools",
                    subtitle = "Complete Document Superpowers",
                    description = "Merge, Compress, Convert, Edit, Sign, Protect, and analyze with next-gen PDF Intelligence.",
                    useLogo = false,
                    fallbackIcon = Icons.Default.GridView,
                    badgeList = listOf("Merge", "Compress", "Convert", "Edit", "Sign", "Protect", "AI")
                )
                2 -> OnboardingPage(
                    title = "Your documents, your privacy",
                    subtitle = "Strict Offline-First Security",
                    description = "Your files stay on your device whenever possible. Zero unnecessary cloud tracking or telemetry.",
                    useLogo = false,
                    fallbackIcon = Icons.Default.Shield,
                    badgeList = listOf("On-Device AES", "Zero Leakage", "Local Privacy")
                )
                3 -> OnboardingPage(
                    title = "Ready to get started?",
                    subtitle = "One App. Every PDF Tool.",
                    description = "Experience the ultimate PDF workflow designed specifically for your Android device.",
                    useLogo = true,
                    fallbackIcon = Icons.Default.CheckCircle,
                    badgeList = listOf("Start Creating", "100% Free", "Google Play Quality")
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    title: String,
    subtitle: String,
    description: String,
    useLogo: Boolean,
    fallbackIcon: ImageVector,
    badgeList: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (useLogo) {
            Box(
                modifier = Modifier
                    .size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                PdfNovaLogoMark(size = 112.dp)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Tool badges
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            badgeList.take(4).forEach { badge ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
