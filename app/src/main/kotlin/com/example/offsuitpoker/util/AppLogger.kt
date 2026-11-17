package com.example.offsuitpoker.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.offsuitpoker.data.PlayerProfile
import com.example.offsuitpoker.model.TableState
import com.example.offsuitpoker.network.NetworkUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object AppLogger {

    private const val TAG = "RRN_AppLogger"
    private const val MAX_LOG_ENTRIES = 600

    private val inMemoryLogs = ConcurrentLinkedQueue<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val fileTimestampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            e("CRASH", "Uncaught exception on thread: ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        i("AppLogger", "Runner Runner Nuts Logger Initialized on ${Build.MANUFACTURER} ${Build.MODEL}")
    }

    fun d(tag: String, msg: String) = log("DEBUG", tag, msg)
    fun i(tag: String, msg: String) = log("INFO", tag, msg)
    fun w(tag: String, msg: String) = log("WARN", tag, msg)
    fun e(tag: String, msg: String, tr: Throwable? = null) {
        val stackTrace = tr?.let { "\n" + Log.getStackTraceString(it) } ?: ""
        log("ERROR", tag, "$msg$stackTrace")
    }

    private fun log(level: String, tag: String, msg: String) {
        val timestamp = dateFormat.format(Date())
        val entry = "[$timestamp] [$level] [$tag] $msg"
        inMemoryLogs.add(entry)
        while (inMemoryLogs.size > MAX_LOG_ENTRIES) {
            inMemoryLogs.poll()
        }
        when (level) {
            "DEBUG" -> Log.d(tag, msg)
            "INFO" -> Log.i(tag, msg)
            "WARN" -> Log.w(tag, msg)
            "ERROR" -> Log.e(tag, msg)
            else -> Log.d(tag, msg)
        }
    }

    fun getLogs(): List<String> = inMemoryLogs.toList()

    fun clearLogs() {
        inMemoryLogs.clear()
        i("AppLogger", "In-memory logs cleared by user.")
    }

    /**
     * Builds comprehensive diagnostics content including system info, app state,
     * in-memory event logs, and logcat output.
     */
    fun buildDiagnosticsReport(
        context: Context,
        profile: PlayerProfile? = null,
        tableState: TableState? = null
    ): String {
        val sb = StringBuilder()
        val now = dateFormat.format(Date())

        sb.appendLine("==================================================")
        sb.appendLine(" RUNNER RUNNER NUTS (RRN) - SYSTEM DIAGNOSTIC LOG ")
        sb.appendLine(" Exported At: $now")
        sb.appendLine("==================================================")
        sb.appendLine()

        // 1. App & Device Information
        sb.appendLine("--- DEVICE & APP INFORMATION ---")
        sb.appendLine("Package: ${context.packageName}")
        sb.appendLine("App Version: 1.0 (Build Release)")
        sb.appendLine("Manufacturer: ${Build.MANUFACTURER}")
        sb.appendLine("Model: ${Build.MODEL} (${Build.DEVICE})")
        sb.appendLine("Product: ${Build.PRODUCT}")
        sb.appendLine("Android OS Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("Display Metrics: ${context.resources.displayMetrics.widthPixels}x${context.resources.displayMetrics.heightPixels} (${context.resources.displayMetrics.densityDpi} dpi)")
        sb.appendLine("Local LAN IP: ${NetworkUtils.getLocalIpAddress()}")
        sb.appendLine()

        // 2. Player Profile Information
        if (profile != null) {
            sb.appendLine("--- PLAYER IDENTITY & SYSTEM STATE ---")
            sb.appendLine("Player ID: ${profile.playerId}")
            sb.appendLine("Player Name: ${profile.playerName}")
            sb.appendLine("Stars Remaining: ${profile.starsRemaining}/5")
            sb.appendLine("Current Table Chips: ${CurrencyFormatter.formatIndian(profile.currentChips)}")
            sb.appendLine("Practice Bankroll: ${CurrencyFormatter.formatIndian(profile.practiceChips)}")
            sb.appendLine("Lifetime Chips Bought: ${CurrencyFormatter.formatIndian(profile.totalBoughtInAmount)}")
            sb.appendLine("Four Color Deck: ${profile.fourColorDeckEnabled}")
            sb.appendLine()
        }

        // 3. Table State
        if (tableState != null) {
            sb.appendLine("--- ACTIVE POKER TABLE STATE ---")
            sb.appendLine("Hand Count: #${tableState.handCount}")
            sb.appendLine("Phase: ${tableState.phase.name}")
            sb.appendLine("Main Pot: ${CurrencyFormatter.formatIndian(tableState.mainPot)}")
            sb.appendLine("Side Pots: ${tableState.sidePots.size}")
            sb.appendLine("Total Pot: ${CurrencyFormatter.formatIndian(tableState.totalPot)}")
            sb.appendLine("Current High Bet: ${tableState.currentHighBet}")
            sb.appendLine("Blinds: SB ${tableState.smallBlind} / BB ${tableState.bigBlind}")
            sb.appendLine("Community Cards: ${tableState.communityCards.joinToString { "${it.rank.display}${it.suit.symbol}" }}")
            sb.appendLine("Dealer Seat Index: ${tableState.dealerIndex}")
            sb.appendLine("Active Turn Index: ${tableState.activeTurnIndex}")
            sb.appendLine("Seats (${tableState.seats.size}):")
            for ((idx, s) in tableState.seats.withIndex()) {
                val status = when {
                    s.isSittingOut -> "OUT"
                    s.folded -> "FOLDED"
                    s.isAllIn -> "ALL-IN"
                    else -> "ACTIVE"
                }
                sb.appendLine("  [$idx] ${s.name} (${if (s.isAi) "BOT" else "HUMAN"}): chips=${s.chips}, bet=${s.roundBet}, totalBet=${s.totalHandBet}, state=$status")
            }
            sb.appendLine()
        }

        // 4. In-Memory RRN Event Logs
        sb.appendLine("--- IN-APP EVENT LOG (${inMemoryLogs.size} ENTRIES) ---")
        if (inMemoryLogs.isEmpty()) {
            sb.appendLine("(No in-app events logged yet)")
        } else {
            for (entry in inMemoryLogs) {
                sb.appendLine(entry)
            }
        }
        sb.appendLine()

        // 5. System Logcat Buffer (Filtered for App Process)
        sb.appendLine("--- LOGCAT BUFFER CAPTURE ---")
        val pid = android.os.Process.myPid()
        try {
            val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val lines = mutableListOf<String>()
            var line: String? = reader.readLine()
            while (line != null) {
                if (line.contains("$pid") || line.contains("runnerrunnernuts") || line.contains("offsuitpoker") || line.contains("FATAL") || line.contains("AndroidRuntime")) {
                    lines.add(line)
                    if (lines.size > 500) lines.removeAt(0)
                }
                line = reader.readLine()
            }
            reader.close()
            process.destroy()

            if (lines.isEmpty()) {
                sb.appendLine("(Logcat buffer empty or no matching process entries)")
            } else {
                for (l in lines) {
                    sb.appendLine(l)
                }
            }
        } catch (e: Exception) {
            sb.appendLine("Failed to capture system logcat: ${e.message}")
        }

        sb.appendLine()
        sb.appendLine("================ END OF DIAGNOSTIC LOG ================")
        return sb.toString()
    }

    /**
     * Writes diagnostics to a file in cacheDir and triggers Android's native share intent.
     */
    fun exportAndShareLogs(
        context: Context,
        profile: PlayerProfile? = null,
        tableState: TableState? = null
    ) {
        try {
            val report = buildDiagnosticsReport(context, profile, tableState)
            val fileName = "RRN_Log_${fileTimestampFormat.format(Date())}.txt"
            val logFile = File(context.cacheDir, fileName)
            logFile.writeText(report)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Runner Runner Nuts - Support Log (${Build.MODEL})")
                putExtra(Intent.EXTRA_TEXT, "Attached is the diagnostic log file for Runner Runner Nuts (RRN) to help resolve an issue.\nDevice: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Diagnostic Log File")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            i("AppLogger", "Successfully triggered share intent for: ${logFile.absolutePath}")
        } catch (e: Exception) {
            e("AppLogger", "Failed to export and share log file", e)
            Toast.makeText(context, "Failed to share logs: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Copies diagnostic report directly to clipboard.
     */
    fun copyLogsToClipboard(
        context: Context,
        profile: PlayerProfile? = null,
        tableState: TableState? = null
    ) {
        try {
            val report = buildDiagnosticsReport(context, profile, tableState)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("RRN Diagnostic Log", report)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Diagnostic logs copied to clipboard!", Toast.LENGTH_SHORT).show()
            i("AppLogger", "Diagnostics copied to clipboard (${report.length} chars)")
        } catch (e: Exception) {
            e("AppLogger", "Failed to copy logs to clipboard", e)
            Toast.makeText(context, "Copy failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
