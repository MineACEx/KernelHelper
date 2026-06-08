package com.kerneluser.ace.utils

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

    fun execRootCmd(cmd: String): String? = ShellUtils.execRoot(cmd)
    fun execSh(cmd: String): String? = ShellUtils.exec(cmd)

    fun getABStatus(): ABStatus {
        try {
            val abProp = execSh("getprop ro.build.ab_update") ?: "false"
            val isAB = abProp.trim().equals("true", ignoreCase = true)
            val slotSuffix = execSh("getprop ro.boot.slot_suffix")?.trim() ?: ""

            var slotA = SlotInfo(suffix = "_a", bootSuccessful = false, unbootable = false)
            var slotB = SlotInfo(suffix = "_b", bootSuccessful = false, unbootable = false)

            if (isAB) {
                val bootctlOutput = execRootCmd("bootctl") ?: ""
                val lines = bootctlOutput.lines()
                var currentSlotFromBootctl = ""
                var aSuccess = false; var aUnbootable = false
                var bSuccess = false; var bUnbootable = false

                for (line in lines) {
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("Current slot:") ->
                            currentSlotFromBootctl = trimmed.substringAfter(":").trim()
                        trimmed.startsWith("Slot _a:") -> {
                            val details = trimmed.substringAfter(":").trim()
                            aSuccess = details.contains("boot successful", ignoreCase = true)
                            aUnbootable = details.contains("unbootable", ignoreCase = true)
                        }
                        trimmed.startsWith("Slot _b:") -> {
                            val details = trimmed.substringAfter(":").trim()
                            bSuccess = details.contains("boot successful", ignoreCase = true)
                            bUnbootable = details.contains("unbootable", ignoreCase = true)
                        }
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
            return ABStatus(isABDevice = false, currentSlot = "",
                slotA = SlotInfo("_a", false, false), slotB = SlotInfo("_b", false, false))
        }
    }

    fun getPartitionList(): String {
        val primaryOutput = execRootCmd("ls -la /dev/block/by-name/")
        if (!primaryOutput.isNullOrBlank()) return primaryOutput
        val findOutput = execRootCmd("find /dev/block/platform -type d -name by-name 2>/dev/null | head -1")
        if (!findOutput.isNullOrBlank()) return execRootCmd("ls -la ${findOutput.trim()}") ?: ""
        return ""
    }

    fun flashPartition(name: String, imagePath: String): String {
        return execRootCmd("dd if=$imagePath of=/dev/block/by-name/$name bs=4096 && sync") ?: "刷写失败"
    }

    fun backupPartition(name: String, outputPath: String): String {
        return execRootCmd("dd if=/dev/block/by-name/$name of=$outputPath") ?: "备份失败"
    }

    fun formatSize(bytes: Long): String = when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "${"%.1f".format(bytes / 1024.0)} KB"
        bytes < 1024L * 1024L * 1024L -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
    }
}