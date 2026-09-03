package com.hypersoft.ads.practice.data.remoteConfig.repository

interface RemoteConfigRepository {
    suspend fun fetchAndCache(): Boolean
}