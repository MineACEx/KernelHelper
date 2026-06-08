package com.kerneluser.ace.utils

import java.io.File

data class Module(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: String,
    val author: String,
    val description: String,
    val enabled: Boolean,
    val remove: Boolean
)

object ModuleUtils {

    private fun readProps(file: File): Map<String, String> {
        val m = mutableMapOf<String, String>()
        if (!file.exists()) return m
        try {
            file.forEachLine { line ->
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) return@forEachLine
                val eq = t.indexOf('=')
                if (eq > 0) m[t.substring(0, eq).trim()] = t.substring(eq + 1).trim()
            }
        } catch (e: Exception) { /* ignore */ }
        return m
    }

    fun getModules(): List<Module> {
        val result = mutableListOf<Module>()
        val base = File("/data/adb/modules")
        if (!base.exists() || !base.isDirectory) return result

        val dirs = base.listFiles() ?: return result
        for (dir in dirs) {
            if (!dir.isDirectory) continue
            val propFile = File(dir, "module.prop")
            if (!propFile.exists()) continue

            val props = readProps(propFile)
            val id = dir.name
            val name = props["name"] ?: id
            val version = props["version"] ?: ""
            val versionCode = props["versionCode"] ?: ""
            val author = props["author"] ?: ""
            val description = props["description"] ?: ""
            val enabled = !File(dir, "disable").exists()
            val remove = File(dir, "remove").exists()

            result.add(Module(id, name, version, versionCode, author, description, enabled, remove))
        }
        return result
    }

    fun enableModule(id: String): Boolean {
        return try { File("/data/adb/modules/$id/disable").delete() } catch (e: Exception) { false }
    }

    fun disableModule(id: String): Boolean {
        return try { File("/data/adb/modules/$id/disable").createNewFile() } catch (e: Exception) { false }
    }

    fun removeModule(id: String): Boolean {
        return try { File("/data/adb/modules/$id/remove").createNewFile() } catch (e: Exception) { false }
    }

    fun forceDelete(id: String): Boolean {
        try {
            val d = File("/data/adb/modules/$id")
            if (!d.exists()) return true
            d.deleteRecursively()
            return true
        } catch (e: Exception) { return false }
    }
}