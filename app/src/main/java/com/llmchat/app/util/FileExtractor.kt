package com.llmchat.app.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class ExtractedFileContent(
    val fileName: String,
    val mimeType: String,
    val text: String,
    val fileSize: Long
)

@Singleton
class FileExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun extractFromUri(uri: Uri): Result<ExtractedFileContent> = withContext(Dispatchers.IO) {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val fileName = cursor?.use { c ->
                val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (c.moveToFirst()) {
                    c.getString(nameIdx) to c.getLong(sizeIdx)
                } else null
            }?.let { (name, _) -> name } ?: "unknown_file"

            val mimeType = context.contentResolver.getType(uri) ?: guessMimeType(fileName)
            val fileSize = getFileSize(uri)

            val text = when {
                mimeType == "application/pdf" -> extractPdf(uri)
                mimeType.startsWith("text/") -> extractText(uri)
                isCodeFile(fileName) -> extractText(uri)
                mimeType == "application/json" -> extractText(uri)
                mimeType == "text/csv" || fileName.endsWith(".csv") -> extractText(uri)
                else -> extractText(uri)
            }

            Result.success(ExtractedFileContent(fileName, mimeType, text, fileSize))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractText(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: ""
    }

    private fun extractPdf(uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val pdfDoc = org.apache.pdfbox.pdmodel.PDDocument.load(stream)
                val stripper = org.apache.pdfbox.text.PDFTextStripper()
                val text = stripper.getText(pdfDoc)
                pdfDoc.close()
                text
            } ?: ""
        } catch (e: Exception) {
            "[PDF extraction failed: ${e.message}]"
        }
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun guessMimeType(fileName: String): String = when {
        fileName.endsWith(".txt") -> "text/plain"
        fileName.endsWith(".md") -> "text/markdown"
        fileName.endsWith(".pdf") -> "application/pdf"
        fileName.endsWith(".json") -> "application/json"
        fileName.endsWith(".csv") -> "text/csv"
        isCodeFile(fileName) -> "text/plain"
        else -> "text/plain"
    }

    private fun isCodeFile(fileName: String): Boolean {
        val codeExtensions = setOf(
            "kt", "java", "py", "js", "ts", "tsx", "jsx", "cpp", "c", "h",
            "rs", "go", "swift", "rb", "php", "cs", "scala", "sh", "bash",
            "yaml", "yml", "toml", "xml", "html", "css", "sql"
        )
        val ext = fileName.substringAfterLast(".", "")
        return ext in codeExtensions
    }

    fun getRelevantChunks(text: String, query: String, maxChars: Int = 3000): String {
        if (text.length <= maxChars) return text

        val queryTerms = query.lowercase().split(" ").filter { it.length > 2 }
        val paragraphs = text.split("\n\n").filter { it.isNotBlank() }

        val scoredParagraphs = paragraphs.map { para ->
            val score = queryTerms.sumOf { term ->
                para.lowercase().split(term).size - 1
            }
            score to para
        }

        val sorted = scoredParagraphs.sortedByDescending { it.first }
        val sb = StringBuilder()
        for ((_, para) in sorted) {
            if (sb.length + para.length + 2 <= maxChars) {
                sb.append(para).append("\n\n")
            }
        }

        return if (sb.isEmpty()) text.take(maxChars) else sb.toString()
    }
}
