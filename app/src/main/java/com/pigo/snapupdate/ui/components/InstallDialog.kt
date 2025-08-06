package com.pigo.snapupdate.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pigo.snapupdate.utils.DownloadStatus
import com.pigo.snapupdate.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun InstallDialog(
    downloadProgress: Int,
    downloadStatus: DownloadStatus,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        // Simulated progress state
        var simulatedProgress by remember { mutableStateOf(0) }
        var downloadSpeed by remember { mutableStateOf("0 KB/s") }
        var timeRemaining by remember { mutableStateOf("--") }
        
        // Simulate realistic download progress
        LaunchedEffect(downloadStatus) {
            if (downloadStatus is DownloadStatus.Progress) {
                // Start from current progress
                simulatedProgress = downloadStatus.percentage
                
                // Simulate realistic download speed and progress
                while (simulatedProgress < 100) {
                    delay(100) // Update every 100ms
                    
                    // Calculate realistic progress increment based on network conditions
                    val baseIncrement = 2
                    val networkVariation = Random.nextFloat() * 3 // 0-3% variation
                    val increment = (baseIncrement + networkVariation).toInt()
                    
                    simulatedProgress = (simulatedProgress + increment).coerceAtMost(100)
                    
                    // Calculate realistic download speed (KB/s)
                    val speedKBps = (50 + Random.nextInt(100)).toFloat() // 50-150 KB/s
                    downloadSpeed = "${speedKBps.toInt()} KB/s"
                    
                    // Calculate time remaining with better logic
                    val remainingProgress = 100 - simulatedProgress
                    val estimatedTotalTime = 30 // Estimated total time in seconds
                    val elapsedProgress = simulatedProgress / 100f
                    val elapsedTime = (estimatedTotalTime * elapsedProgress).toInt()
                    val remainingTime = estimatedTotalTime - elapsedTime
                    
                    timeRemaining = if (remainingTime > 0 && simulatedProgress > 0) {
                        "${remainingTime}s"
                    } else {
                        "--"
                    }
                }
            }
        }

        // Animated background
        val infiniteTransition = rememberInfiniteTransition()
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = CleanCardBackground
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with animated icon
                val icon = when (downloadStatus) {
                    is DownloadStatus.Progress -> Icons.Rounded.Download
                    is DownloadStatus.Success -> Icons.Rounded.CheckCircle
                    is DownloadStatus.Failed -> Icons.Rounded.Error
                    is DownloadStatus.Paused -> Icons.Rounded.Pause
                    is DownloadStatus.Pending -> Icons.Rounded.Schedule
                }
                
                val iconColor = when (downloadStatus) {
                    is DownloadStatus.Progress -> CleanInfo
                    is DownloadStatus.Success -> CleanSuccess
                    is DownloadStatus.Failed -> CleanError
                    is DownloadStatus.Paused -> CleanWarning
                    is DownloadStatus.Pending -> CleanOnSurfaceVariant
                }
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulse)
                        .background(
                            color = iconColor,
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = CleanOnPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Title
                Text(
                    text = when (downloadStatus) {
                        is DownloadStatus.Progress -> "Downloading Update"
                        is DownloadStatus.Success -> "Download Complete"
                        is DownloadStatus.Failed -> "Download Failed"
                        is DownloadStatus.Paused -> "Download Paused"
                        is DownloadStatus.Pending -> "Preparing Download"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CleanOnSurface
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = when (downloadStatus) {
                        is DownloadStatus.Progress -> "Please wait while we download the update"
                        is DownloadStatus.Success -> "Update downloaded successfully"
                        is DownloadStatus.Failed -> "Failed to download update"
                        is DownloadStatus.Paused -> "Download has been paused"
                        is DownloadStatus.Pending -> "Initializing download process"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = CleanOnSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress Section
                if (downloadStatus is DownloadStatus.Progress) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = CleanOnSurface
                                )
                            )
                            Text(
                                text = "$simulatedProgress%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = CleanInfo,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Custom Progress Bar with rounded corners
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    color = CleanSurfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(simulatedProgress / 100f)
                                    .height(8.dp)
                                    .background(
                                        color = CleanInfo,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Download Details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Speed: $downloadSpeed",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CleanOnSurfaceVariant
                                )
                            )
                            Text(
                                text = "Time: $timeRemaining",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CleanOnSurfaceVariant
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Status Message
                        Text(
                            text = when {
                                simulatedProgress < 20 -> "Initializing download..."
                                simulatedProgress < 50 -> "Downloading APK file..."
                                simulatedProgress < 80 -> "Almost complete..."
                                simulatedProgress < 100 -> "Finalizing download..."
                                else -> "Download complete"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CleanOnSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Success/Failed States
                when (downloadStatus) {
                    is DownloadStatus.Success -> {
                        Text(
                            text = "Installation will start automatically",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CleanSuccess
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    is DownloadStatus.Failed -> {
                        Text(
                            text = "Please try again later",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CleanError
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {}
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Button - Fixed styling
                when (downloadStatus) {
                    is DownloadStatus.Success -> {
                        // No button needed - auto-installation
                    }
                    is DownloadStatus.Failed -> {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanError,
                                contentColor = CleanOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Close",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    is DownloadStatus.Paused -> {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CleanWarning,
                                contentColor = CleanOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Resume",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                    else -> {
                        // Proper Continue button styling
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
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
                                    imageVector = Icons.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Continue",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 