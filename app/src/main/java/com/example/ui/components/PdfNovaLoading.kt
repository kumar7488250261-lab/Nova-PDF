package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Official PDFNova Logo Mark Composable.
 * Uses the supplied official brand icon:
 * White document shape, folded corner, and geometric N with blue-to-purple gradient.
 */
@Composable
fun PdfNovaLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .testTag("pdfnova_logo_mark"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_pdfnova_mark),
            contentDescription = "PDFNova logo",
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Official PDFNova Horizontal / Stacked Brand Lockup Composable.
 * Features the official logo mark + "PDFNova" text + tagline.
 */
@Composable
fun PdfNovaBrandLockup(
    modifier: Modifier = Modifier,
    logoSize: Dp = 42.dp,
    showTagline: Boolean = true,
    horizontal: Boolean = true
) {
    if (horizontal) {
        Row(
            modifier = modifier.testTag("pdfnova_brand_lockup"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PdfNovaLogoMark(size = logoSize)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PDF",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Nova",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                }
                if (showTagline) {
                    Text(
                        text = "One App. Every PDF Tool.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier.testTag("pdfnova_brand_lockup"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PdfNovaLogoMark(size = logoSize)
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PDF",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Nova",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
            }
            if (showTagline) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "One App. Every PDF Tool.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.2.sp
                )
            }
        }
    }
}

/**
 * Reusable PDFNova Loading State Composable (Section 14).
 * Structure:
 * Box
 * ├── PDFNova logo
 * ├── message (e.g. "Preparing your workspace...", "Opening PDF...", "Processing document...", "Finalizing...")
 * └── subtle animated indicator
 * Never uses fake percentages.
 */
@Composable
fun PdfNovaLoading(
    message: String = "Preparing your workspace...",
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    // Subtle continuous breathing animation on the logo
    val infiniteTransition = rememberInfiniteTransition(label = "pdfnova_loading_transition")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("pdfnova_loading_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                PdfNovaLogoMark(size = 80.dp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("pdfnova_loading_message")
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtle linear animated progress bar indicator (indeterminate)
            LinearProgressIndicator(
                modifier = Modifier
                    .width(140.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .testTag("pdfnova_loading_indicator"),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
