package com.llmchat.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.llmchat.app.domain.model.AttachedFile

@Composable
fun AttachmentCard(
    file: AttachedFile,
    onClick: () -> Unit = {},
    onRemove: (() -> Unit)? = null
) {
    val (icon, color) = getFileIconAndColor(file.mimeType, file.fileName)

    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.mimeType.startsWith("image/")) {
                ImageThumbnail(file = file)
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatFileSize(file.fileSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    file.pageCount?.let {
                        Text(
                            text = " · $it pages",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                file.errorMessage?.let {
                    Text(
                        text = "Error: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnail(file: AttachedFile) {
    // Note: In a real app, you'd use the Uri to load the image.
    // Since AttachedFile currently doesn't store Uri, this is a placeholder.
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Image, contentDescription = null, tint = Color.White)
    }
}

private fun getFileIconAndColor(mimeType: String, fileName: String): Pair<ImageVector, Color> {
    return when {
        mimeType.startsWith("image/") -> Icons.Default.Image to Color(0xFF4CAF50)
        mimeType == "application/pdf" -> Icons.Default.PictureAsPdf to Color(0xFFF44336)
        mimeType.contains("word") || fileName.endsWith(".doc") || fileName.endsWith(".docx") -> Icons.Default.Description to Color(0xFF2196F3)
        mimeType.contains("excel") || fileName.endsWith(".xls") || fileName.endsWith(".xlsx") -> Icons.Default.TableChart to Color(0xFF4CAF50)
        mimeType.contains("powerpoint") || fileName.endsWith(".ppt") || fileName.endsWith(".pptx") -> Icons.Default.Slideshow to Color(0xFFFF9800)
        mimeType.startsWith("text/") || fileName.endsWith(".txt") -> Icons.Default.Article to Color(0xFF9E9E9E)
        mimeType.startsWith("audio/") -> Icons.Default.AudioFile to Color(0xFF9C27B0)
        mimeType.startsWith("video/") -> Icons.Default.VideoFile to Color(0xFFFF5722)
        fileName.endsWith(".zip") || fileName.endsWith(".rar") || fileName.endsWith(".7z") -> Icons.Default.FolderZip to Color(0xFFFFC107)
        isCodeFile(fileName) -> Icons.Default.Code to Color(0xFF607D8B)
        else -> Icons.Default.AttachFile to Color(0xFF9E9E9E)
    }
}

private fun isCodeFile(fileName: String): Boolean {
    val ext = fileName.substringAfterLast(".", "").lowercase()
    return ext in setOf("kt", "java", "py", "js", "ts", "cpp", "c", "h", "rs", "go", "swift", "rb", "php", "cs", "sql")
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
