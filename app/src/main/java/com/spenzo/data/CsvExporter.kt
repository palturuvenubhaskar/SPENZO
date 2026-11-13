package com.spenzo.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV export — the app had no way to get data out, so uninstalling or clearing
 * app data meant losing everything.
 */
object CsvExporter {

    fun write(rows: List<TransactionEntity>, dir: File): File {
        dir.mkdirs()
        val file = File(dir, "spenzo-${fileStamp()}.csv")
        file.bufferedWriter(Charsets.UTF_8).use { out ->
            out.write("\uFEFF") // BOM so Excel opens UTF-8 notes correctly
            out.write("date,name,category,type,amount,note\n")
            rows.sortedBy { it.timestamp }.forEach { row ->
                out.write(
                    listOf(
                        row.dayLabel,
                        row.name,
                        categoryLabel(row.category),
                        row.type,
                        String.format(Locale.ROOT, "%.2f", Money.fromCents(Money.cents(row.amount))),
                        row.note,
                    ).joinToString(",") { escape(it) }
                )
                out.write("\n")
            }
        }
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Spenzo export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export transactions"))
    }

    private fun categoryLabel(name: String): String =
        Category.entries.firstOrNull { it.name == name }?.label ?: name

    private fun escape(value: String): String {
        val needsQuotes = value.indexOfAny(charArrayOf(',', '"', '\n', '\r')) >= 0
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }

    private fun fileStamp(): String =
        SimpleDateFormat("yyyyMMdd-HHmm", Locale.ROOT).format(Date())
}
