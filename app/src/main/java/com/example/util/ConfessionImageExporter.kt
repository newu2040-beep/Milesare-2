package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import com.example.model.Post
import com.example.ui.theme.AppThemePreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class ExportResolution(
    val title: String,
    val badge: String,
    val storyWidth: Int,
    val storyHeight: Int,
    val squareSize: Int
) {
    HD("1080p Full HD", "FHD", 1080, 1920, 1080),
    UHD_4K("4K Ultra HD", "4K UHD", 2160, 3840, 2160),
    UHD_8K("8K Cinematic", "8K UHD", 4320, 7680, 4320)
}

enum class ExportFormat(val title: String, val ratioLabel: String) {
    STORY_9_16("Story / Wallpaper", "9:16"),
    SQUARE_1_1("Square Post", "1:1")
}

object ConfessionImageExporter {

    suspend fun exportConfessionImage(
        context: Context,
        post: Post,
        theme: AppThemePreset,
        resolution: ExportResolution,
        format: ExportFormat,
        includeWatermark: Boolean = true
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val (width, height) = when (format) {
                ExportFormat.STORY_9_16 -> Pair(resolution.storyWidth, resolution.storyHeight)
                ExportFormat.SQUARE_1_1 -> Pair(resolution.squareSize, resolution.squareSize)
            }

            val scale = width / 1080f
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Draw Gradient Background
            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                val colorTop = theme.gradientColors.first().toArgb()
                val colorBottom = theme.gradientColors.last().toArgb()
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    colorTop, colorBottom,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

            // 2. Draw Subtle Ambient Circles for depth
            val ambientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.previewColor.toArgb()
                alpha = if (theme.isDark) 40 else 60
            }
            canvas.drawCircle(width * 0.85f, height * 0.15f, 250f * scale, ambientPaint)
            canvas.drawCircle(width * 0.15f, height * 0.85f, 320f * scale, ambientPaint)

            // 3. Central Card Container
            val cardMarginHoriz = 64f * scale
            val cardTop = (if (format == ExportFormat.STORY_9_16) height * 0.20f else height * 0.12f)
            val cardWidth = width - (cardMarginHoriz * 2)

            // Card Shadow
            val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (theme.isDark) Color.BLACK else Color.rgb(200, 195, 215)
                alpha = if (theme.isDark) 90 else 70
            }
            val cardCornerRadius = 40f * scale

            // Card Body Paint
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.cardColor.toArgb()
            }

            // Estimate text height
            val contentPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (theme.isDark) Color.WHITE else Color.rgb(30, 24, 43)
                textSize = 42f * scale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val textPadding = 48f * scale
            val textWidth = (cardWidth - (textPadding * 2)).toInt()

            val textLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(post.content, 0, post.content.length, contentPaint, textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(14f * scale, 1.15f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(
                    post.content, contentPaint, textWidth,
                    Layout.Alignment.ALIGN_NORMAL, 1.15f, 14f * scale, false
                )
            }

            val headerHeight = 110f * scale
            val footerHeight = 90f * scale
            val cardHeight = textLayout.height + headerHeight + footerHeight + (textPadding * 2)

            val cardRect = RectF(
                cardMarginHoriz,
                cardTop,
                cardMarginHoriz + cardWidth,
                cardTop + cardHeight
            )

            // Draw Card with shadow
            val shadowRect = RectF(cardRect.left, cardRect.top + 8f * scale, cardRect.right, cardRect.bottom + 12f * scale)
            canvas.drawRoundRect(shadowRect, cardCornerRadius, cardCornerRadius, shadowPaint)
            canvas.drawRoundRect(cardRect, cardCornerRadius, cardCornerRadius, cardPaint)

            // Card Border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f * scale
                color = theme.previewColor.toArgb()
                alpha = if (theme.isDark) 80 else 110
            }
            canvas.drawRoundRect(cardRect, cardCornerRadius, cardCornerRadius, borderPaint)

            // 4. Header: Anonymous Handle + Category Tag
            val avatarSize = 44f * scale
            val avatarLeft = cardRect.left + textPadding
            val avatarTop = cardRect.top + textPadding

            val avatarCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.previewColor.toArgb()
            }
            canvas.drawCircle(avatarLeft + avatarSize / 2, avatarTop + avatarSize / 2, avatarSize / 2, avatarCirclePaint)

            // Anonymous Handle Text
            val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (theme.isDark) Color.WHITE else Color.rgb(35, 28, 50)
                textSize = 32f * scale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(
                post.anonymousName,
                avatarLeft + avatarSize + (16f * scale),
                avatarTop + (avatarSize * 0.42f),
                authorPaint
            )

            // Sphere Category Chip
            val categoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (theme.isDark) Color.rgb(180, 170, 210) else Color.rgb(100, 90, 130)
                textSize = 24f * scale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(
                "• ${post.category.uppercase()}",
                avatarLeft + avatarSize + (16f * scale),
                avatarTop + (avatarSize * 0.95f),
                categoryPaint
            )

            // 5. Draw Confession Text
            canvas.save()
            canvas.translate(cardRect.left + textPadding, cardRect.top + headerHeight + (20f * scale))
            textLayout.draw(canvas)
            canvas.restore()

            // 6. Card Footer: Reactions & Comments
            val footerTop = cardRect.top + headerHeight + textLayout.height + (30f * scale)
            val statsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (theme.isDark) Color.rgb(180, 170, 210) else Color.rgb(110, 100, 140)
                textSize = 26f * scale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val statsText = "❤️ ${post.reactionCount} reactions   💬 ${post.commentCount} replies"
            canvas.drawText(statsText, cardRect.left + textPadding, footerTop, statsPaint)

            // 7. Watermark / Footer (if enabled)
            if (includeWatermark) {
                val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (theme.isDark) Color.rgb(190, 180, 220) else Color.rgb(90, 80, 120)
                    textSize = 28f * scale
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val watermarkY = height - (48f * scale)
                canvas.drawText(
                    "MilesAre • Anonymous Whispers Network  (${resolution.badge})",
                    width / 2f,
                    watermarkY,
                    watermarkPaint
                )
            }

            // 8. Save to MediaStore (Gallery)
            val filename = "MilesAre_Confession_${resolution.name}_${System.currentTimeMillis()}.png"
            val uri = saveBitmapToGallery(context, bitmap, filename)

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String): Uri {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MilesAre")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Could not create MediaStore entry")

        resolver.openOutputStream(uri)?.use { stream: OutputStream ->
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw IllegalStateException("Failed to compress bitmap into PNG")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }
}
