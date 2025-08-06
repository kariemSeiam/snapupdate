package com.pigo.snapupdate.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pigo.snapupdate.ui.viewmodel.UpdateUiState
import com.pigo.snapupdate.ui.components.InstallDialog
import com.pigo.snapupdate.ui.components.UpdateDialog
import com.pigo.snapupdate.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.rounded.OpenInNew
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: UpdateUiState,
    onCheckUpdate: () -> Unit,
    onAcceptUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
    onDismissInstall: () -> Unit,
    onIncrementVersion: () -> Unit = {},
    onResetVersion: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentVersion = remember { getAppVersion(context) }

    // Clean background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Header
            CenteredHeader(currentVersion = currentVersion ?: "1.0")

            Spacer(modifier = Modifier.height(32.dp))

            // Horizontal Version Cards
            HorizontalVersionCards(uiState = uiState, currentVersion = currentVersion ?: "1.0")

            Spacer(modifier = Modifier.height(32.dp))

            // Responsive Actions
            ResponsiveActions(
                onCheckUpdate = onCheckUpdate,
                onIncrementVersion = onIncrementVersion,
                onResetVersion = onResetVersion,
                uiState = uiState
            )

            Spacer(modifier = Modifier.weight(1f))

            // Clean Footer
            CleanFooter()
        }

        // Update Dialog
        if (uiState.showUpdateDialog) {
            UpdateDialog(
                updateInfo = uiState.updateInfo,
                onAccept = onAcceptUpdate,
                onDismiss = onDismissUpdate
            )
        }

        // Install Dialog
        if (uiState.showInstallDialog) {
            InstallDialog(
                downloadProgress = uiState.downloadProgress,
                downloadStatus = uiState.downloadStatus,
                onDismiss = onDismissInstall
            )
        }
    }
}

private fun getAppVersion(context: Context): String? {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

@Composable
private fun CenteredHeader(currentVersion: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier.fillMaxWidth()
    ) {
        // Animated App Icon
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f, targetValue = 1.05f, animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
            )
        )
        
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing), repeatMode = RepeatMode.Restart
            )
        )

        // Icon Container
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(
                    color = CleanPrimary, 
                    shape = RoundedCornerShape(20.dp)
                ), 
            contentAlignment = Alignment.Center
        ) {
            // Background glow
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = CleanPrimary.copy(alpha = 0.3f), 
                        shape = RoundedCornerShape(16.dp)
                    )
            )
            
            // Main icon
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                modifier = Modifier
                    .size(36.dp)
                    .rotate(rotation),
                tint = CleanOnPrimary
            )
            
            // Accent icon
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd),
                tint = CleanSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Title
        Text(
            text = "SnapUpdate", 
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold, 
                color = CleanOnBackground
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub Repository Chip
        GitHubRepoChip()
    }
}

@Composable
private fun GitHubRepoChip() {
    val context = LocalContext.current
    
    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pigo/snapupdate"))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error if browser is not available
                }
            },
        shape = RoundedCornerShape(16.dp),
        color = CleanSurfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, CleanPrimary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // GitHub icon with creative styling
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = CleanPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Code,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = CleanPrimary
                )
            }
            
            Text(
                text = "pigo/snapupdate",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = CleanOnSurface,
                    fontWeight = FontWeight.Medium
                )
            )
            
            // External link indicator
            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = CleanOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun HorizontalVersionCards(uiState: UpdateUiState, currentVersion: String) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current Version Card - Creative Design
        CreativeVersionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Info,
            iconColor = CleanInfo,
            title = "Current",
            value = currentVersion,
            description = "Installed",
            backgroundColor = CleanPrimary.copy(alpha = 0.1f),
            borderColor = CleanPrimary.copy(alpha = 0.3f)
        )

        // Server Version Card - Creative Design
        CreativeVersionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.CloudDownload,
            iconColor = CleanSecondary,
            title = "Available",
            value = uiState.serverVersion?.currentVersion ?: "1.3",
            description = "Latest",
            backgroundColor = CleanSecondary.copy(alpha = 0.1f),
            borderColor = CleanSecondary.copy(alpha = 0.3f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Update Status Card
    CleanStatusCard(uiState = uiState)
}

@Composable
private fun CreativeVersionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    description: String,
    backgroundColor: Color,
    borderColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = CleanCardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon with animated glow
            val infiniteTransition = rememberInfiniteTransition()
            val glow by infiniteTransition.animateFloat(
                initialValue = 0.6f, targetValue = 1.0f, animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
                )
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconColor.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp)
                    ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(glow),
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = title, style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium, color = CleanOnSurface.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Version number
            Text(
                text = value, style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = iconColor
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = description, style = MaterialTheme.typography.bodySmall.copy(
                    color = CleanOnSurfaceVariant, fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun CleanStatusCard(uiState: UpdateUiState) {
    val statusIcon = when {
        uiState.isLoading -> Icons.Rounded.Refresh
        uiState.error != null -> Icons.Rounded.Error
        uiState.updateInfo != null -> Icons.Rounded.SystemUpdate
        else -> Icons.Rounded.CheckCircle
    }

    val statusColor = when {
        uiState.isLoading -> CleanInfo
        uiState.error != null -> CleanError
        uiState.updateInfo != null -> CleanSuccess
        else -> CleanSuccess
    }

    val statusTitle = when {
        uiState.isLoading -> "Checking Updates"
        uiState.error != null -> "Update Error"
        uiState.updateInfo != null -> "Update Ready"
        else -> "Up to Date"
    }

    val statusDescription = when {
        uiState.isLoading -> "Verifying latest version"
        uiState.error != null -> uiState.error ?: "An unknown error occurred"
        uiState.updateInfo != null -> "New version available for download"
        else -> "Latest version installed"
    }

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (uiState.isLoading) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CleanCardBackground
            // containerColor = statusColor.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp, Brush.radialGradient(
                colors = listOf(
                    statusColor.copy(alpha = 0.5f),
                    statusColor.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset.Zero,
                radius = 200f,

                )
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation),
                tint = statusColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusTitle, style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium, color = CleanOnSurface
                    )
                )

                Text(
                    text = statusDescription ?: "No status to display",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CleanOnSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun ResponsiveActions(
    onCheckUpdate: () -> Unit, 
    onIncrementVersion: () -> Unit, 
    onResetVersion: () -> Unit,
    uiState: UpdateUiState
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp), 
        modifier = Modifier.fillMaxWidth()
    ) {
        // Check Updates Button - Consistent color
        Button(
            onClick = onCheckUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanPrimary, 
                contentColor = CleanOnPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Check for Updates", 
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Increment Version Button - Consistent color
        Button(
            onClick = onIncrementVersion,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.isIncrementingVersion && canIncrementVersion(uiState.serverVersion?.currentVersion),
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanPrimary, 
                contentColor = CleanOnPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Increment Version", 
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        // Reset Version Button
        Button(
            onClick = onResetVersion,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isIncrementingVersion,
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanWarning, 
                contentColor = CleanOnPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Reset to v1.0", 
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

private fun canIncrementVersion(currentVersion: String?): Boolean {
    if (currentVersion == null) return true
    
    return when (currentVersion) {
        "1.0" -> true
        "1.1" -> true
        "1.2" -> true
        "1.3" -> false // Max reached
        else -> true // Allow for other versions
    }
}

@Composable
private fun CleanFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            color = CleanCardBorder, thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Built with Kotlin & Jetpack Compose",
            style = MaterialTheme.typography.bodySmall.copy(
                color = CleanOnSurfaceVariant
            )
        )
    }
} 