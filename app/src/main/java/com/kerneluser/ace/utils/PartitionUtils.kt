package com.kerneluser.ace.utils

import java.io.BufferedReader
import java.io.InputStreamReader

data class SlotInfo(
    val suffix: String,
    val bootSuccessful: Boolean,
    val unbootable: Boolean
)

data class ABStatus(
    val isABDevice: Boolean,
    val currentSlot: String,
    val slotA: SlotInfo,
    val slotB: SlotInfo
)

object PartitionUtils {

    // Run shell command as root, return combined output or null
    fun exec(cmd: String): String? {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            reader.useLines { lines -> lines.forEach { output.appendLine(it) } }
            errorReader.useLines { lines -> lines.forEach { output.appendLine(it) } }
            process.waitFor()
            val result = output.toString().trim()
            return if (result.isEmpty()) null else result
        } catch (e: Exception) {
            return null
        }
    }

    // Get A/B partition status
    fun getABStatus(): ABStatus {
        try {
            val abProp = exec("getprop ro.build.ab_update") ?: "false"
            val isAB = abProp.trim().equals("true", ignoreCase = true)
            val slotSuffix = exec("getprop ro.boot.slot_suffix")?.trim() ?: ""

            var slotA = SlotInfo(suffix = "_a", bootSuccessful = false, unbootable = false)
            var slotB = SlotInfo(suffix = "_b", bootSuccessful = false, unbootable = false)

            if (isAB) {
                val bootctlOutput = exec("bootctl") ?: ""
                val lines = bootctlOutput.lines()
                var currentSlotFromBootctl = ""
                var aSuccess = false
                var aUnbootable = false
                var bSuccess = false
                var bUnbootable = false

                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("Current slot:")) {
                        currentSlotFromBootctl = trimmed.substringAfter(":").trim()
                    } else if (trimmed.startsWith("Slot _a:")) {
                        val details = trimmed.substringAfter(":").trim()
                        aSuccess = details.contains("boot successful", ignoreCase = true)
                        aUnbootable = details.contains("unbootable", ignoreCase = true)
                    } else if (trimmed.startsWith("Slot _b:")) {
                        val details = trimmed.substringAfter(":").trim()
                        bSuccess = details.contains("boot successful", ignoreCase = true)
                        bUnbootable = details.contains("unbootable", ignoreCase = true)
                    }
                }

                val effectiveSlot = if (currentSlotFromBootctl.isNotEmpty()) currentSlotFromBootctl else slotSuffix

                slotA = SlotInfo(suffix = "_a", bootSuccessful = aSuccess, unbootable = aUnbootable)
                slotB = SlotInfo(suffix = "_b", bootSuccessful = bSuccess, unbootable = bUnbootable)

                return ABStatus(isABDevice = true, currentSlot = effectiveSlot, slotA = slotA, slotB = slotB)
            } else {
                return ABStatus(isABDevice = false, currentSlot = slotSuffix, slotA = slotA, slotB = slotB)
            }
        } catch (e: Exception) {
            return ABStatus(
                isABDevice = false, currentSlot = "",
                slotA = SlotInfo("_a", false, false),
                slotB = SlotInfo("_b", false, false)
            )
        }
    }

    // List all partitions by-name
    fun getPartitionList(): String {
        val primaryOutput = exec("ls -la /dev/block/by-name/")
        if (!primaryOutput.isNullOrBlank()) {
            return primaryOutput
        }
        val findOutput = exec("find /dev/block/platform -type d -name by-name 2>/dev/null | head -1")
        if (!findOutput.isNullOrBlank()) {
            return exec("ls -la ${findOutput.trim()}") ?: ""
        }
        return ""
    }

    // Flash image to partition
    fun flashPartition(name: String, imagePath: String): String {
        return exec("dd if=$imagePath of=/dev/block/by-name/$name bs=4096 && sync") ?: "Flash failed."
    }

    // Backup partition to output path
    fun backupPartition(name: String, outputPath: String): String {
        return exec("dd if=/dev/block/by-name/$name of=$outputPath") ?: "Backup failed."
    }

    // Format bytes to human-readable
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024L -> "${"%.1f".format(bytes / 1024.0)} KB"
            bytes < 1024L * 1024L * 1024L -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
}