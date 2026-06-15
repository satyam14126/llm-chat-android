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
    val fileSize: Long,
    val pageCount: Int? = null,
    val errorMessage: String? = null
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
            android.util.Log.d("FileExtractor", "Extracting: $fileName ($mimeType), Size: $fileSize")

            var pageCount: Int? = null
            var extractionError: String? = null

            val text = try {
                when {
                    mimeType == "application/pdf" -> {
                        val pdfResult = extractPdf(uri)
                        pageCount = pdfResult.pageCount
                        pdfResult.text
                    }
                    mimeType.startsWith("text/") || isCodeFile(fileName) || 
                    mimeType == "application/json" || fileName.endsWith(".csv") -> extractText(uri)
                    else -> extractText(uri)
                }
            } catch (e: Exception) {
                extractionError = e.message
                "[Extraction failed: ${e.message}]"
            }

            Result.success(ExtractedFileContent(fileName, mimeType, text, fileSize, pageCount, extractionError))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractText(uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: ""
        } catch (e: Exception) {
            "Error reading text: ${e.message}"
        }
    }

    data class PdfResult(val text: String, val pageCount: Int?)

    private fun extractPdf(uri: Uri): PdfResult {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                com.tom_roush.pdfbox.pdmodel.PDDocument.load(stream).use { pdfDoc ->
                    if (pdfDoc.isEncrypted) {
                        return PdfResult("[PDF is password protected]", pdfDoc.numberOfPages)
                    }
                    val stripper = com.tom_roush.pdfbox.text.PDFTextStripper()
                    val text = stripper.getText(pdfDoc)
                    PdfResult(text, pdfDoc.numberOfPages)
                }
            } ?: PdfResult("", null)
        } catch (e: Exception) {
            android.util.Log.e("FileExtractor", "PDF extraction failed", e)
            PdfResult("[PDF extraction failed: ${e.message}]", null)
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
