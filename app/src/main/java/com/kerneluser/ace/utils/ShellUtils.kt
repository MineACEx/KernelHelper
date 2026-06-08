package com.kerneluser.ace.utils

import java.io.File
import java.io.InputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * 统一 Shell 执行工具。
 *
 * 核心策略：
 *   1. su 管道 — 打开 su 进程，先设置 Magisk/KSU/AP 环境变量，再注入命令
 *   2. su -c  — 传统 -c 方式，附 Magisk 路径前缀
 *   3. /system/bin/sh — 非 root 直接执行
 *
 * 适配所有主流内核管理器：Magisk / KernelSU / APatch
 */
object ShellUtils {

    // ── 预扫描的 root 二进制路径 ──────────────────
    private val magiskPath: String? by lazy { findBin("magisk") }
    private val busyboxPath: String? by lazy {
        findBin("busybox")
            ?: "/data/adb/magisk/busybox".takeIf { File(it).exists() }
            ?: "/data/adb/ksu/bin/busybox".takeIf { File(it).exists() }
    }

    private val rootBinDirs: String by lazy {
        listOfNotNull(
            "/data/adb/magisk".takeIf { File(it).exists() },
            "/data/adb/ksu/bin".takeIf { File(it).exists() },
            "/data/adb/ap/bin".takeIf { File(it).exists() },
            "/data/adb/modules".takeIf { File(it).exists() },
            "/system/bin", "/system/xbin", "/sbin"
        ).joinToString(":")
    }

    /** 执行 root 命令 */
    fun execRoot(cmd: String, timeoutMs: Long = 25_000): String? {
        return execRootPipe(cmd, timeoutMs)
            ?: execRootDashC(cmd, timeoutMs)
            ?: execShDirect(cmd, timeoutMs)
    }

    /** 执行普通命令（无需 root） */
    fun exec(cmd: String, timeoutMs: Long = 12_000): String? {
        return execShDirect(cmd, timeoutMs)
    }

    // ─────────── 方法 1：su 管道（最兼容）──────────
    private fun execRootPipe(cmd: String, timeoutMs: Long): String? {
        try {
            val process = ProcessBuilder()
                .command("su")
                .redirectErrorStream(true)
                .start()

            process.outputStream.bufferedWriter().use { writer ->
                // 先设置环境，确保能找到 Magisk/KSU/AP 工具
                val paths = rootBinDirs
                if (paths.isNotEmpty()) {
                    writer.write("export PATH=\$PATH:$paths")
                    writer.newLine()
                }
                // 写入实际命令
                writer.write(cmd)
                writer.newLine()
                writer.write("exit")
                writer.newLine()
                writer.flush()
            }

            val output = readStreamWithTimeout(process.inputStream, timeoutMs)
            process.waitFor()
            val result = output.trim()
            return result.ifBlank { null }
        } catch (_: Exception) { return null }
    }

    // ─────────── 方法 2：su -c（传统）─────────────
    private fun execRootDashC(cmd: String, timeoutMs: Long): String? {
        try {
            // 在命令前拼接 PATH 导出
            val paths = rootBinDirs
            val fullCmd = if (paths.isNotEmpty())
                "export PATH=\$PATH:$paths; $cmd" else cmd

            val process = ProcessBuilder()
                .command("su", "-c", fullCmd)
                .redirectErrorStream(true)
                .start()

            val output = readStreamWithTimeout(process.inputStream, timeoutMs)
            process.waitFor()
            val result = output.trim()
            return result.ifBlank { null }
        } catch (_: Exception) { return null }
    }

    // ─────────── 方法 3：/system/bin/sh ────────────
    private fun execShDirect(cmd: String, timeoutMs: Long): String? {
        try {
            val shell = findShell()
            val process = ProcessBuilder()
                .command(shell, "-c", cmd)
                .redirectErrorStream(true)
                .start()

            val output = readStreamWithTimeout(process.inputStream, timeoutMs)
            process.waitFor()
            val result = output.trim()
            return result.ifBlank { null }
        } catch (_: Exception) { return null }
    }

    // ─────────── 辅助 ──────────────────────────────
    private fun findShell(): String {
        for (c in arrayOf("/system/bin/sh", "/vendor/bin/sh", "/sbin/sh", "/bin/sh")) {
            if (File(c).exists() && File(c).canExecute()) return c
        }
        return "/system/bin/sh"
    }

    private fun findBin(name: String): String? {
        for (dir in arrayOf(
            "/data/adb/magisk", "/data/adb/ksu/bin", "/data/adb/ap/bin",
            "/system/bin", "/system/xbin", "/sbin", "/vendor/bin"
        )) {
            val p = "$dir/$name"
            if (File(p).exists() && File(p).canExecute()) return p
        }
        return null
    }

    private fun readStreamWithTimeout(input: InputStream, timeoutMs: Long): String {
        val sb = StringBuilder()
        val completed = AtomicBoolean(false)
        val reader = thread {
            try {
                input.bufferedReader().use { r ->
                    var line: String?
                    while (r.readLine().also { line = it } != null) sb.appendLine(line)
                }
            } catch (_: Exception) {}
            finally { completed.set(true) }
        }
        reader.join(timeoutMs)
        if (!completed.get()) { reader.interrupt(); sb.append("\n[timeout]") }
        return sb.toString()
    }
}