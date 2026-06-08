package com.kerneluser.ace.utils

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
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
        m["kernelVersion"] = RootUtils.exec("uname -r") ?: "-"
        m["selinuxStatus"] = RootUtils.execSu("getenforce") ?: "-"
        m["cpuInfo"] = readProc("/proc/cpuinfo", 30)
        m["memInfo"] = readProc("/proc/meminfo", 10)
        m["buildType"] = Build.TYPE
        m["buildUser"] = Build.USER
        m["buildHost"] = Build.HOST
        m["bootloader"] = Build.BOOTLOADER
        m["baseband"] = Build.getRadioVersion() ?: "-"
        m["buildTags"] = Build.TAGS
        m["buildTime"] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .format(Date(Build.TIME))
        return m
    }

    fun getMountPoints(): String {
        return RootUtils.execSu("cat /proc/mounts") ?: "无法获取"
    }

    fun getRunningServices(): String {
        return RootUtils.execSu("service list") ?: "无法获取"
    }

    fun getNetworkStats(): String {
        return RootUtils.exec("cat /proc/net/dev") ?: "无法获取"
    }

    private fun readProc(path: String, maxLines: Int): String {
        return try {
            val f = File(path)
            if (!f.exists()) return "-"
            val sb = StringBuilder()
            f.bufferedReader().use { r ->
                var line: String?
                var i = 0
                while (r.readLine().also { line = it } != null && i++ < maxLines)
                    sb.appendLine(line)
            }
            sb.toString().trim()
        } catch (_: Exception) { "-" }
    }
}