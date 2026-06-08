package com.kerneluser.ace.utils

import java.io.File

data class SuperuserEntry(
    val packageName: String, val manager: String, val uid: String,
    val policy: Int, val logging: Boolean, val rawConfig: String
)

object SuperuserUtils {

    private const val POLICY_ALLOW = 2
    private const val POLICY_DENY = 1
    private const val POLICY_ASK = 3

    private fun exec(cmd: String): String? = ShellUtils.execRoot(cmd)

    private fun detectRootType(): String {
        if (File("/data/adb/ksu/profiles").let { it.exists() && it.isDirectory }) return "KernelSU"
        if (File("/data/adb/ap/profiles").let { it.exists() && it.isDirectory }) return "APatch"
        if (File("/data/adb/magisk.db").exists()) return "Magisk"
        return "Unknown"
    }

    fun getSuperuserList(): List<SuperuserEntry> = try {
        when (detectRootType()) {
            "Magisk" -> getMagiskEntries("Magisk")
            "KernelSU" -> getKernelSUEntries("KernelSU")
            "APatch" -> getAPatchEntries("APatch")
            else -> emptyList()
        }
    } catch (e: Exception) { emptyList() }

    private fun getMagiskEntries(rootType: String): List<SuperuserEntry> {
        val entries = mutableListOf<SuperuserEntry>()
        val output = exec("sqlite3 /data/adb/magisk.db \"SELECT package_name, policy, logging, uid FROM policies;\"")
            ?: return entries
        for (line in output.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parts = trimmed.split('|')
            if (parts.size >= 4) {
                entries.add(SuperuserEntry(
                    packageName = parts[0].trim(), manager = rootType,
                    uid = parts[3].trim(), policy = parts[1].trim().toIntOrNull() ?: 0,
                    logging = parts[2].trim() == "1", rawConfig = trimmed
                ))
            }
        }
        return entries
    }

    private fun getProfileEntries(rootType: String, profilesDir: File, entries: MutableList<SuperuserEntry>) {
        val profileFiles = profilesDir.listFiles() ?: return
        for (profileFile in profileFiles) {
            if (!profileFile.isFile) continue
            val pkgName = profileFile.name
            val rawConfig = try { profileFile.readText().trim() } catch (e: Exception) { "" }
            val configMap = mutableMapOf<String, String>()
            for (line in rawConfig.lines()) {
                val split = line.trim().split('=', limit = 2)
                if (split.size == 2) configMap[split[0].trim()] = split[1].trim()
            }
            val uid = configMap["uid"] ?: ""
            val policy = when (configMap["allow"]?.trim()) {
                "1", "true" -> POLICY_ALLOW; "0", "false" -> POLICY_DENY; else -> POLICY_ASK
            }
            entries.add(SuperuserEntry(
                packageName = pkgName, manager = rootType, uid = uid,
                policy = policy, logging = configMap["logging"]?.trim() in listOf("1", "true"),
                rawConfig = rawConfig
            ))
        }
    }

    private fun getKernelSUEntries(rootType: String): List<SuperuserEntry> {
        val entries = mutableListOf<SuperuserEntry>()
        val dir = File("/data/adb/ksu/profiles")
        if (dir.exists() && dir.isDirectory) getProfileEntries(rootType, dir, entries)
        return entries
    }

    private fun getAPatchEntries(rootType: String): List<SuperuserEntry> {
        val entries = mutableListOf<SuperuserEntry>()
        val dir = File("/data/adb/ap/profiles")
        if (dir.exists() && dir.isDirectory) getProfileEntries(rootType, dir, entries)
        return entries
    }

    fun revokeApp(pkg: String): Boolean = try {
        when (detectRootType()) {
            "Magisk" -> exec("sqlite3 /data/adb/magisk.db \"UPDATE policies SET policy=1 WHERE package_name='$pkg';\"").let { true }
            "KernelSU" -> { exec("rm -f /data/adb/ksu/profiles/$pkg"); true }
            "APatch" -> { exec("rm -f /data/adb/ap/profiles/$pkg"); true }
            else -> false
        }
    } catch (e: Exception) { false }

    fun grantApp(pkg: String): Boolean = try {
        when (detectRootType()) {
            "Magisk" -> {
                exec("sqlite3 /data/adb/magisk.db \"INSERT OR REPLACE INTO policies (package_name, policy, logging, until) VALUES ('$pkg', 2, 1, 0);\"")
                true
            }
            "KernelSU" -> { exec("mkdir -p /data/adb/ksu/profiles && echo 'allow=1\nlogging=0\n' > /data/adb/ksu/profiles/$pkg"); true }
            "APatch" -> { exec("mkdir -p /data/adb/ap/profiles && echo 'allow=1\nlogging=0\n' > /data/adb/ap/profiles/$pkg"); true }
            else -> false
        }
    } catch (e: Exception) { false }
}