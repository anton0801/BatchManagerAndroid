package com.managerbatching.batchmanager.domain.usecases

import android.util.Log
import com.managerbatching.batchmanager.MainApplication
import com.managerbatching.batchmanager.domain.data.BatchingNotificationsPushTokenUseCase
import com.managerbatching.batchmanager.domain.data.BatchManagerRepImpl
import com.managerbatching.batchmanager.domain.data.BatchingSystemSerI
import com.managerbatching.batchmanager.domain.model.BatchManagerEntity
import com.managerbatching.batchmanager.domain.model.BatchManagerMainParam

class BatchManagerAllUseCaseInApplication(
    private val batchManagerRepImpl: BatchManagerRepImpl,
    private val batchingSystemSerI: BatchingSystemSerI,
    private val batchingNotificationsPushTokenUseCase: BatchingNotificationsPushTokenUseCase,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?): BatchManagerEntity? {
        val params = BatchManagerMainParam(
            feedMixLocale = batchingSystemSerI.getLocaleOfUserFeedMix(),
            feedMixPushToken = batchingNotificationsPushTokenUseCase.batchManagerGetToken(),
            feedMixAfId = batchingSystemSerI.getAppsflyerIdForApp()
        )
        Log.d(MainApplication.BATCH_MANAGER_MAIN_TAG, "Params for request: $params")
        return batchManagerRepImpl.batchManagerAppObtainClient(params, conversion)
    }


}