package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Post
import com.example.ui.components.AnonymousAvatar
import com.example.ui.theme.AppThemePreset
import com.example.util.ConfessionImageExporter
import com.example.util.ExportFormat
import com.example.util.ExportResolution
import com.example.util.rememberGalleryPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportStudioScreen(
    post: Post,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTheme by remember { mutableStateOf(AppThemePreset.PASTEL_LILAC) }
    var selectedResolution by remember { mutableStateOf(ExportResolution.UHD_4K) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.STORY_9_16) }
    var isExporting by remember { mutableStateOf(false) }
    var exportedUri by remember { mutableStateOf<Uri?>(null) }

    val galleryPermissionState = rememberGalleryPermissionState { granted ->
        if (granted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Gallery access permission granted!")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("4K / 8K Pastel Studio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gallery Full Access Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (galleryPermissionState.hasFullAccess)
                        Color(0xFF059669).copy(alpha = 0.15f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("gallery_permission_status_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (galleryPermissionState.hasFullAccess) Icons.Default.CheckCircle else Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = if (galleryPermissionState.hasFullAccess) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (galleryPermissionState.hasFullAccess)
                                "Full Gallery Access Active"
                            else
                                "Gallery Permissions Required",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (galleryPermissionState.hasFullAccess)
                                "Ready to export up to 8K resolution into your Photos"
                            else
                                "Grant access to store 4K & 8K exports directly into your gallery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!galleryPermissionState.hasFullAccess) {
                        OutlinedButton(
                            onClick = { galleryPermissionState.requestPermissions() },
                            modifier = Modifier.testTag("request_gallery_permissions_button")
                        ) {
                            Text("Grant Access")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real-Time Live Preview of Pastel Card
            Text(
                text = "Real-Time Preview (${selectedResolution.badge} • ${selectedFormat.ratioLabel})",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(if (selectedFormat == ExportFormat.STORY_9_16) 9f / 14f else 1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.verticalGradient(selectedTheme.gradientColors))
                    .border(1.5.dp, selectedTheme.previewColor.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                // Card Inner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = selectedTheme.cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnonymousAvatar(avatarKey = post.anonymousAvatar, size = 32.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = post.anonymousName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedTheme.isDark) Color.White else Color(0xFF221A33)
                                )
                                Text(
                                    text = "• ${post.category.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = selectedTheme.previewColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = post.content,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = if (selectedTheme.isDark) Color(0xFFF3F0FA) else Color(0xFF2B2140)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "❤️ ${post.reactionCount} reactions   💬 ${post.commentCount} replies",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTheme.isDark) Color(0xFFAFA7C9) else Color(0xFF6B5E85)
                        )
                    }
                }

                // Watermark in preview
                Text(
                    text = "MilesAre • Anonymous Whispers Network (${selectedResolution.badge})",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = if (selectedTheme.isDark) Color(0xFFAFA7C9) else Color(0xFF6B5E85),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Pastel Theme Selector
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select Pastel Palette", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppThemePreset.entries.forEach { themePreset ->
                    val isSelected = selectedTheme == themePreset
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = themePreset.cardColor,
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 2.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else themePreset.previewColor.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { selectedTheme = themePreset }
                            .testTag("pastel_theme_option_${themePreset.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(themePreset.previewColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = themePreset.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                color = if (themePreset.isDark) Color.White else Color(0xFF221A33)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Resolution Selector (Up to 4K and 8K)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Default.HighQuality, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Resolution", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportResolution.entries.forEach { res ->
                    val isSelected = selectedResolution == res
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedResolution = res },
                        label = { Text(res.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).testTag("resolution_chip_${res.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Aspect Ratio Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportFormat.entries.forEach { fmt ->
                    val isSelected = selectedFormat == fmt
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFormat = fmt },
                        label = { Text(fmt.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier.weight(1f).testTag("format_chip_${fmt.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Export Button
            Button(
                onClick = {
                    if (!galleryPermissionState.hasFullAccess) {
                        galleryPermissionState.requestPermissions()
                    }
                    isExporting = true
                    coroutineScope.launch {
                        val result = ConfessionImageExporter.exportConfessionImage(
                            context = context,
                            post = post,
                            theme = selectedTheme,
                            resolution = selectedResolution,
                            format = selectedFormat,
                            includeWatermark = true
                        )
                        isExporting = false
                        if (result.isSuccess) {
                            exportedUri = result.getOrThrow()
                            snackbarHostState.showSnackbar(
                                "Saved ${selectedResolution.title} to Gallery (${if (selectedFormat == ExportFormat.STORY_9_16) "${selectedResolution.storyWidth}x${selectedResolution.storyHeight}" else "${selectedResolution.squareSize}x${selectedResolution.squareSize}"})!"
                            )
                        } else {
                            snackbarHostState.showSnackbar("Export error: ${result.exceptionOrNull()?.localizedMessage}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("export_to_gallery_button"),
                shape = RoundedCornerShape(16.dp),
                enabled = !isExporting
            ) {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Rendering ${selectedResolution.badge}...")
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Export & Save ${selectedResolution.badge} to Gallery", fontWeight = FontWeight.Bold)
                }
            }

            exportedUri?.let { uri ->
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share ${selectedResolution.badge} Image"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share ${selectedResolution.badge}")
                    }

                    OutlinedButton(
                        onClick = {
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "image/png")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(viewIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View in Gallery")
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
