package com.kernelhelper.acex.data.repository

import com.kernelhelper.acex.data.model.RepoModule

interface ModuleRepoRepository {
    suspend fun fetchModules(): Result<List<RepoModule>>
}
