package com.managerbatching.batchmanager.ui.pres.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.managerbatching.batchmanager.BatchManagerAppsFlyerState
import com.managerbatching.batchmanager.MainApplication
import com.managerbatching.batchmanager.handlers.BatchManagerLocalStoreManager
import com.managerbatching.batchmanager.domain.data.BatchingSystemSerI
import com.managerbatching.batchmanager.domain.usecases.BatchManagerAllUseCaseInApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatchManagerLoadingSplashVM(
    private val batchManagerAllUseCaseInApplication: BatchManagerAllUseCaseInApplication,
    private val batchManagerLocalStoreManager: BatchManagerLocalStoreManager,
    private val batchingSystemSerI: BatchingSystemSerI
) : ViewModel() {

    private val _chickHealthHomeScreenState: MutableStateFlow<FeedMixHomeScreenState> =
        MutableStateFlow(FeedMixHomeScreenState.FeedMixLoading)
    val chickHealthHomeScreenState = _chickHealthHomeScreenState.asStateFlow()

    private var eggLabelGetApps = false

    init {
        viewModelScope.launch {
            when (batchManagerLocalStoreManager.batchAppState) {
                0 -> {
                    if (batchingSystemSerI.feedMixCheckInternetConnection()) {
                        MainApplication.BatchingManagerConversionFlow.collect {
                            when (it) {
                                BatchManagerAppsFlyerState.BatchManagerDefault -> {}
                                BatchManagerAppsFlyerState.BatchManagerError -> {
                                    batchManagerLocalStoreManager.batchAppState = 2
                                    _chickHealthHomeScreenState.value =
                                        FeedMixHomeScreenState.FeedMixError
                                    eggLabelGetApps = true
                                }

                                is BatchManagerAppsFlyerState.BatchManagerSuccess -> {
                                    if (!eggLabelGetApps) {
                                        feedMixGetData(it.feedMixxChickkData)
                                        eggLabelGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _chickHealthHomeScreenState.value =
                            FeedMixHomeScreenState.FeedMixNotInternet
                    }
                }

                1 -> {
                    if (batchingSystemSerI.feedMixCheckInternetConnection()) {
                        if (MainApplication.BATCH_MANAGER_LI != null) {
                            _chickHealthHomeScreenState.value =
                                FeedMixHomeScreenState.FeedMixSuccess(
                                    MainApplication.BATCH_MANAGER_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > batchManagerLocalStoreManager.batchExpired) {
                            Log.d(
                                MainApplication.BATCH_MANAGER_MAIN_TAG,
                                "Current time more then expired, repeat request"
                            )
                            MainApplication.BatchingManagerConversionFlow.collect {
                                when (it) {
                                    BatchManagerAppsFlyerState.BatchManagerDefault -> {}
                                    BatchManagerAppsFlyerState.BatchManagerError -> {
                                        _chickHealthHomeScreenState.value =
                                            FeedMixHomeScreenState.FeedMixSuccess(
                                                batchManagerLocalStoreManager.batchSavedUrl
                                            )
                                        eggLabelGetApps = true
                                    }

                                    is BatchManagerAppsFlyerState.BatchManagerSuccess -> {
                                        if (!eggLabelGetApps) {
                                            feedMixGetData(it.feedMixxChickkData)
                                            eggLabelGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(
                                MainApplication.BATCH_MANAGER_MAIN_TAG,
                                "Current time less then expired, use saved url"
                            )
                            _chickHealthHomeScreenState.value =
                                FeedMixHomeScreenState.FeedMixSuccess(
                                    batchManagerLocalStoreManager.batchSavedUrl
                                )
                        }
                    } else {
                        _chickHealthHomeScreenState.value =
                            FeedMixHomeScreenState.FeedMixNotInternet
                    }
                }

                2 -> {
                    _chickHealthHomeScreenState.value =
                        FeedMixHomeScreenState.FeedMixError
                }
            }
        }
    }


    private suspend fun feedMixGetData(conversation: MutableMap<String, Any>?) {
        val eggLabelData = batchManagerAllUseCaseInApplication.invoke(conversation)
        if (batchManagerLocalStoreManager.batchAppState == 0) {
            if (eggLabelData == null) {
                batchManagerLocalStoreManager.batchAppState = 2
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixError
            } else {
                batchManagerLocalStoreManager.batchAppState = 1
                batchManagerLocalStoreManager.apply {
                    batchExpired = eggLabelData.feedMixExpires
                    batchSavedUrl = eggLabelData.feedMixUrl
                }
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(eggLabelData.feedMixUrl)
            }
        } else {
            if (eggLabelData == null) {
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(batchManagerLocalStoreManager.batchSavedUrl)
            } else {
                batchManagerLocalStoreManager.apply {
                    batchExpired = eggLabelData.feedMixExpires
                    batchSavedUrl = eggLabelData.feedMixUrl
                }
                _chickHealthHomeScreenState.value =
                    FeedMixHomeScreenState.FeedMixSuccess(eggLabelData.feedMixUrl)
            }
        }
    }


    sealed class FeedMixHomeScreenState {
        data object FeedMixLoading : FeedMixHomeScreenState()
        data object FeedMixError : FeedMixHomeScreenState()
        data class FeedMixSuccess(val data: String) : FeedMixHomeScreenState()
        data object FeedMixNotInternet : FeedMixHomeScreenState()
    }
}