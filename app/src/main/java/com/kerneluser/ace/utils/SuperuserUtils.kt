package com.kerneluser.ace.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class SuperuserEntry(
    val packageName: String,
    val manager: String,
    val uid: String,
    val policy: Int,
    val logging: Boolean,
    val rawConfig: String
)

object SuperuserUtils {

    private const val POLICY_ALLOW = 2
    private const val POLICY_DENY = 1
    private const val POLICY_ASK = 3

    private fun exec(cmd: String): String? {
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

    private fun detectRootType(): String {
        val ksuProfiles = File("/data/adb/ksu/profiles")
        if (ksuProfiles.exists() && ksuProfiles.isDirectory) return "KernelSU"

        val apProfiles = File("/data/adb/ap/profiles")
        if (apProfiles.exists() && apProfiles.isDirectory) return "APatch"

        val magiskDb = File("/data/adb/magisk.db")
        if (magiskDb.exists()) return "Magisk"

        return "Unknown"
    }

    fun getSuperuserList(): List<SuperuserEntry> {
        try {
            val rootType = detectRootType()
            return when (rootType) {
                "Magisk" -> getMagiskEntries(rootType)
                "KernelSU" -> getKernelSUEntries(rootType)
                "APatch" -> getAPatchEntries(rootType)
                else -> emptyList()
            }
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun getMagiskEntries(rootType: String): List<SuperuserEntry> {
        val entries = mutableListOf<SuperuserEntry>()
        val output = exec(
            "sqlite3 /data/adb/magisk.db \"SELECT package_name, policy, logging, uid FROM policies;\""
        ) ?: return entries

        for (line in output.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            val parts = trimmed.split('|')
            if (parts.size >= 4) {
                val pkg = parts[0].trim()
                val policy = parts[1].trim().toIntOrNull() ?: 0
                val logging = parts[2].trim() == "1"
                val uid = parts[3].trim()
                entries.add(
                    SuperuserEntry(
                        packageName = pkg, manager = rootType, uid = uid,
                        policy = policy, logging = logging, rawConfig = trimmed
                    )
                )
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
                val trimmed = line.trim()
                val split = trimmed.split('=', limit = 2)
                if (split.size == 2) {
                    configMap[split[0].trim()] = split[1].trim()
                }
            }
            val uid = configMap["uid"] ?: ""
            val policy = when (configMap["allow"]?.trim()) {
                "1", "true" -> POLICY_ALLOW
                "0", "false" -> POLICY_DENY
                else -> POLICY_ASK
            }
            val logging = configMap["logging"]?.trim() in listOf("1", "true")
            entries.add(
                SuperuserEntry(
                    packageName = pkgName, manager = rootType, uid = uid,
                    policy = policy, logging = logging, rawConfig = rawConfig
                )
            )
        }
    }

    private fun getKernelSUEntries(rootType: String): List<SuperuserEntry> {
        val entries = mutableListOf<SuperuserEntry>()
        val profilesDir = File("/data/adb/ksu/profiles")
        if (!profilesDir.exists() || !profilesDir.isDirectory) return entries
        getProfileEntries(rootType, profilesDir, entries)
        return entries
    }

    private fun getAPatchEntries(rootType: String): List<SuperuserEntry> {
        val entries = mutableListOf<SuperuserEntry>()
        val profilesDir = File("/data/adb/ap/profiles")
        if (!profilesDir.exists() || !profilesDir.isDirectory) return entries
        getProfileEntries(rootType, profilesDir, entries)
        return entries
    }

    fun revokeApp(pkg: String): Boolean {
        try {
            val rootType = detectRootType()
            return when (rootType) {
                "Magisk" -> {
                    val result = exec(
                        "sqlite3 /data/adb/magisk.db \"UPDATE policies SET policy=1 WHERE package_name='$pkg';\""
                    )
                    result != null
                }
                "KernelSU" -> {
                    exec("rm -f /data/adb/ksu/profiles/$pkg") != null
                }
                "APatch" -> {
                    exec("rm -f /data/adb/ap/profiles/$pkg") != null
                }
                else -> false
            }
        } catch (e: Exception) {
            return false
        }
    }

    fun grantApp(pkg: String): Boolean {
        try {
            val rootType = detectRootType()
            return when (rootType) {
                "Magisk" -> {
                    val result = exec(
                        "sqlite3 /data/adb/magisk.db " +
                            "\"INSERT OR REPLACE INTO policies (package_name, policy, logging, until) " +
                            "VALUES ('$pkg', 2, 1, 0);\""
                    )
                    result != null
                }
                "KernelSU" -> {
                    exec("mkdir -p /data/adb/ksu/profiles")
                    exec("echo 'allow=1\nlogging=0\n' > /data/adb/ksu/profiles/$pkg")
                    true
                }
                "APatch" -> {
                    exec("mkdir -p /data/adb/ap/profiles")
                    exec("echo 'allow=1\nlogging=0\n' > /data/adb/ap/profiles/$pkg")
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            return false
        }
    }
}