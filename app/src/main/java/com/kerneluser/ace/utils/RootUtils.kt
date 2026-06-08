package com.kerneluser.ace.utils

import java.io.File

object RootUtils {

    data class RootStatus(val isRooted: Boolean, val rootType: String, val version: String)

    fun execSu(cmd: String): String? = ShellUtils.execRoot(cmd)
    fun exec(cmd: String): String? = ShellUtils.exec(cmd)

    fun checkRoot(): RootStatus {
        var hasRoot = false
        val suPaths = arrayOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/data/local/xbin/su", "/data/local/bin/su",
            "/su/bin/su", "/magisk/.core/bin/su",
            "/data/adb/ksu/bin/su", "/data/adb/ap/bin/su"
        )
        for (p in suPaths) { if (File(p).exists()) { hasRoot = true; break } }
        if (!hasRoot) hasRoot = (execSu("id") != null)

        var type = "Unknown"
        var ver = ""

        val magiskVer = execSu("magisk -v")
        if (!magiskVer.isNullOrBlank()) {
            type = "Magisk"; ver = magiskVer.trim()
        } else {
            val ksuVer = execSu("ksud -V")
            if (!ksuVer.isNullOrBlank()) {
                type = "KernelSU"; ver = ksuVer.trim()
            } else {
                val apVer = execSu("apd -v")
                if (!apVer.isNullOrBlank()) {
                    type = "APatch"; ver = apVer.trim()
                } else {
                    if (File("/data/adb/magisk").exists()) { type = "Magisk"; ver = "-" }
                    else if (File("/data/adb/ksu").exists()) { type = "KernelSU"; ver = "-" }
                    else if (File("/data/adb/ap").exists()) { type = "APatch"; ver = "-" }
                }
            }
        }

        if (ver.isEmpty() || ver == "-") {
            val prop = exec("getprop ro.magisk.version") ?: exec("getprop persist.ksu.version")
            if (!prop.isNullOrBlank()) ver = prop.trim()
        }

        return RootStatus(hasRoot, type, ver)
    }

    // ──────── CPU / 内核参数读取 ────────

    fun getCpuGovernor(): String {
        val raw = exec("cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
        return raw?.trim() ?: "-"
    }

    fun getCpuFreqRange(): String {
        val min = exec("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq")?.trim()
        val max = exec("cat /sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")?.trim()
        return if (min != null && max != null) {
            "${min.toIntOrNull()?.div(1000) ?: "?"}MHz - ${max.toIntOrNull()?.div(1000) ?: "?"}MHz"
        } else "-"
    }

    fun getCpuCores(): String {
        val count = exec("cat /sys/devices/system/cpu/present")?.trim() ?: "-"
        return count
    }

    fun getThermalTemp(): String {
        val paths = arrayOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp"
        )
        for (p in paths) {
            val raw = exec("cat $p")?.trim()
            if (!raw.isNullOrBlank()) {
                val t = raw.toIntOrNull() ?: continue
                return "${t / 1000}°C"
            }
        }
        return "-"
    }

    fun getUptime(): String {
        val raw = exec("cat /proc/uptime")?.trim()
        if (raw.isNullOrBlank()) return "-"
        val secs = raw.split(" ").firstOrNull()?.toDoubleOrNull() ?: return "-"
        val h = (secs / 3600).toInt()
        val m = ((secs % 3600) / 60).toInt()
        return "${h}h ${m}m"
    }
}