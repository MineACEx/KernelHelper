package com.kerneluser.ace.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object RootUtils {

    data class RootStatus(val isRooted: Boolean, val rootType: String, val version: String)

    fun execSu(cmd: String): String? {
        try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(p.inputStream)).use { r ->
                var line: String? = null
                while (r.readLine().also { line = it } != null) sb.appendLine(line)
            }
            p.waitFor()
            val out = sb.toString().trim()
            return if (out.isEmpty()) null else out
        } catch (e: Exception) { return null }
    }

    fun exec(cmd: String): String? {
        try {
            val p = Runtime.getRuntime().exec(cmd)
            val sb = StringBuilder()
            BufferedReader(InputStreamReader(p.inputStream)).use { r ->
                var line: String? = null
                while (r.readLine().also { line = it } != null) sb.appendLine(line)
            }
            p.waitFor()
            val out = sb.toString().trim()
            return if (out.isEmpty()) null else out
        } catch (e: Exception) { return null }
    }

    fun checkRoot(): RootStatus {
        // check su binary
        var hasRoot = false
        val suPaths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/su/bin/su", "/magisk/.core/bin/su",
            "/data/adb/ksu/bin/su", "/data/adb/ap/bin/su"
        )
        for (p in suPaths) { if (File(p).exists()) { hasRoot = true; break } }
        if (!hasRoot) hasRoot = (execSu("id") != null)

        // detect type: try shell commands first, then file existence
        var type = "Unknown"
        var ver = ""

        // Magisk
        val magiskVer = execSu("magisk -v")
        if (!magiskVer.isNullOrBlank()) {
            type = "Magisk"
            ver = magiskVer.trim()
        } else {
            // KernelSU
            val ksuVer = execSu("ksud -V")
            if (!ksuVer.isNullOrBlank()) {
                type = "KernelSU"
                ver = ksuVer.trim()
            } else {
                // APatch
                val apVer = execSu("apd -v")
                if (!apVer.isNullOrBlank()) {
                    type = "APatch"
                    ver = apVer.trim()
                } else {
                    // fallback: filesystem check
                    if (File("/data/adb/magisk").exists()) { type = "Magisk"; ver = "-" }
                    else if (File("/data/adb/ksu").exists()) { type = "KernelSU"; ver = "-" }
                    else if (File("/data/adb/ap").exists()) { type = "APatch"; ver = "-" }
                }
            }
        }

        // try getprop as last resort for version
        if (ver.isEmpty() || ver == "-") {
            val prop = execSu("getprop ro.magisk.version") ?: execSu("getprop persist.ksu.version")
            if (!prop.isNullOrBlank()) ver = prop.trim()
        }

        return RootStatus(hasRoot, type, ver)
    }
}