package com.kernelhelper.acex.data.repository

import com.kernelhelper.acex.data.model.Module
import com.kernelhelper.acex.data.model.ModuleUpdateInfo

interface ModuleRepository {
    suspend fun getModules(): Result<List<Module>>
    suspend fun checkUpdate(module: Module): Result<ModuleUpdateInfo>
}
