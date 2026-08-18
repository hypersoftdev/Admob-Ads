package YOUR.PACKAGE.domain.usecase.remoteConfig

import YOUR.PACKAGE.domain.repository.remoteConfig.RemoteConfigRepository

class FetchRemoteConfigUseCase(
    private val remoteConfigRepository: RemoteConfigRepository,
) {
    suspend operator fun invoke(): Boolean = remoteConfigRepository.fetchAndCache()
}
