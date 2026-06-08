package com.kerneluser.ace.utils

import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object DeviceInfo {
    fun get(): Map<String, String> {
        val m = linkedMapOf<String, String>()
        m["brand"] = Build.BRAND
        m["manufacturer"] = Build.MANUFACTURER
        m["model"] = Build.MODEL
        m["device"] = Build.DEVICE
        m["hardware"] = Build.HARDWARE
        m["product"] = Build.PRODUCT
        m["board"] = Build.BOARD
        m["fingerprint"] = Build.FINGERPRINT
        m["display"] = Build.DISPLAY
        m["androidVersion"] = Build.VERSION.RELEASE
        m["sdkVersion"] = Build.VERSION.SDK_INT.toString()
        m["securityPatch"] = Build.VERSION.SECURITY_PATCH ?: "-"
        m["kernelVersion"] = ShellUtils.exec("uname -r")?.trim() ?: "-"
        m["selinuxStatus"] = (ShellUtils.execRoot("getenforce") ?: ShellUtils.exec("getenforce"))?.trim() ?: "-"
        m["cpuInfo"] = readProc("/proc/cpuinfo", 30)
        m["memInfo"] = readProc("/proc/meminfo", 10)
        m["buildType"] = Build.TYPE
        m["buildUser"] = Build.USER
        m["buildHost"] = Build.HOST
        m["bootloader"] = detectBootloader()
        m["baseband"] = Build.getRadioVersion() ?: "-"
        m["buildTags"] = Build.TAGS
        m["buildTime"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(Build.TIME))
        return m
    }

    fun getMountPoints(): String = ShellUtils.exec("cat /proc/mounts")?.trim() ?: "无法获取"
    fun getRunningServices(): String = ShellUtils.execRoot("service list")?.trim() ?: "无法获取"
    fun getNetworkStats(): String = ShellUtils.exec("cat /proc/net/dev")?.trim() ?: "无法获取"

    /** 从多种来源检测 Bootloader 状态 */
    private fun detectBootloader(): String {
        // 1. 尝试读取 Android Verified Boot 状态
        val avb = ShellUtils.execRoot("cat /proc/cmdline")
        if (!avb.isNullOrBlank()) {
            val cmdline = avb.lowercase()
            if (cmdline.contains("androidboot.verifiedbootstate=orange") ||
                cmdline.contains("androidboot.verifiedbootstate=yellow"))
                return "已解锁 (来自 cmdline)"
            if (cmdline.contains("androidboot.verifiedbootstate=green"))
                return "已锁定 (来自 cmdline)"
        }

        // 2. 尝试 sysfs
        for (path in listOf(
            "/sys/security/verified_bootstate",
            "/proc/bootloader_lock"
        )) {
            try {
                val v = File(path).readText().trim()
                if (v in listOf("orange", "yellow")) return "已解锁"
                if (v == "green") return "已锁定"
            } catch (_: Exception) {}
        }

        // 3. 尝试 getprop
        val sp = ShellUtils.exec("getprop ro.boot.flash.locked")
            ?: ShellUtils.exec("getprop ro.boot.verifiedbootstate")
            ?: ShellUtils.exec("getprop ro.bootloader")

        if (!sp.isNullOrBlank()) {
            val s = sp.trim().lowercase()
            if (s in listOf("0", "false", "orange", "yellow")) return "已解锁"
            if (s in listOf("1", "true", "green")) return "已锁定"
            return sp.trim().replaceFirstChar { it.uppercase() }
        }

        // 4. 最后的 fallback: Build.BOOTLOADER (modern Android usually returns "unknown")
        return Build.BOOTLOADER.ifBlank { "无法检测" }.takeIf { it != "unknown" } ?: "无法检测"
    }

    private fun readProc(path: String, maxLines: Int): String {
        return try {
            val f = File(path)
            if (!f.exists()) "-"
            else {
                val sb = StringBuilder()
                f.bufferedReader().use { r ->
                    var line: String?; var i = 0
                    while (r.readLine().also { line = it } != null && i++ < maxLines) sb.appendLine(line)
                }
                sb.toString().trim()
            }
        } catch (_: Exception) { "-" }
    }
}