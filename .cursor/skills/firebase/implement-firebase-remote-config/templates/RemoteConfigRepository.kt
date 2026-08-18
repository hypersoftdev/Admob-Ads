package YOUR.PACKAGE.domain.repository.remoteConfig

interface RemoteConfigRepository {
    suspend fun fetchAndCache(): Boolean
}
