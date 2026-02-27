package com.managerbatching.batchmanager

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkResult
import com.managerbatching.batchmanager.domain.data.BatchingSystemSerI
import com.managerbatching.batchmanager.domain.data.BatchingNotificationsPushTokenUseCase
import com.managerbatching.batchmanager.domain.data.BatchManagerRepImpl
import com.managerbatching.batchmanager.domain.usecases.BatchManagerAllUseCaseInApplication
import com.managerbatching.batchmanager.handlers.BatchManagerLocalStoreManager
import com.managerbatching.batchmanager.handlers.BatchManagerNotifPushHandler
import com.managerbatching.batchmanager.ui.pres.views.BatchManagerMainViFun
import com.managerbatching.batchmanager.ui.pres.ui.BatchManagerLoadingSplashVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import kotlin.apply
import kotlin.collections.iterator
import kotlin.collections.set
import kotlin.let


val feedMixModule = module {
    factory {
        BatchManagerNotifPushHandler()
    }
    single {
        BatchManagerRepImpl()
    }
    single {
        BatchManagerLocalStoreManager(get())
    }
    factory {
        BatchingNotificationsPushTokenUseCase()
    }
    factory {
        BatchingSystemSerI(get())
    }
    factory {
        BatchManagerAllUseCaseInApplication(
            get(), get(), get()
        )
    }
    factory {
        BatchManagerMainViFun(get())
    }
    viewModel {
        BatchManagerLoadingSplashVM(get(), get(), get())
    }
}

class MainApplication : Application() {

    private var batchingChIsResumed = false
    private var batchingConvTimeoutJob: Job? = null
    private var batchingManagerDeepLinksMap: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        batchingDebugLoggerMode(appsflyer)
        feedMix(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink { p0 ->
            when (p0.status) {
                DeepLinkResult.Status.FOUND -> {
                    batchManagerDDExtractDeepLinksData(p0.deepLink)
                }

                DeepLinkResult.Status.NOT_FOUND -> {
                }

                DeepLinkResult.Status.ERROR -> {
                }
            }
        }

        appsflyer.init(
            BATCH_MANAGER_APPSFLYER_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    batchingConvTimeoutJob?.cancel()

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val api = feedMixGetApiMethodsForAppsflyer(
                                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                    null
                                )
                                val response = api.eggLabelGetClient(
                                    devkey = BATCH_MANAGER_APPSFLYER_DEV,
                                    deviceId = batchingGetAppserId()
                                ).awaitResponse()

                                val resp = response.body()
                                if (resp?.get("af_status") == "Organic") {
                                    feedMixChickResume(BatchManagerAppsFlyerState.BatchManagerError)
                                } else {
                                    feedMixChickResume(
                                        BatchManagerAppsFlyerState.BatchManagerSuccess(resp)
                                    )
                                }
                            } catch (d: Exception) {
                                feedMixChickResume(BatchManagerAppsFlyerState.BatchManagerError)
                            }
                        }
                    } else {
                        feedMixChickResume(BatchManagerAppsFlyerState.BatchManagerSuccess(p0))
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    batchingConvTimeoutJob?.cancel()
                    feedMixChickResume(BatchManagerAppsFlyerState.BatchManagerError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                }

                override fun onAttributionFailure(p0: String?) {
                }
            },
            this
        )

        appsflyer.start(this, BATCH_MANAGER_APPSFLYER_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
            }

            override fun onError(p0: Int, p1: String) {
                feedMixChickResume(BatchManagerAppsFlyerState.BatchManagerError)
            }
        })
        feedMixStartConvTimeot()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApplication)
            modules(
                listOf(
                    feedMixModule
                )
            )
        }
    }

    private fun batchingDebugLoggerMode(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun batchManagerDDExtractDeepLinksData(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(BATCH_MANAGER_MAIN_TAG, "Extracted DeepLink data: $map")
        batchingManagerDeepLinksMap = map
    }

    private fun batchingGetAppserId(): String =
        AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""


    companion object {
        var batchiungInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val BatchingManagerConversionFlow: MutableStateFlow<BatchManagerAppsFlyerState> =
            MutableStateFlow(
                BatchManagerAppsFlyerState.BatchManagerDefault
            )
        var BATCH_MANAGER_LI: String? = null
        const val BATCH_MANAGER_MAIN_TAG = "SLEEP_RELAX_MainTag"
    }

    private fun feedMixGetApiMethodsForAppsflyer(
        url: String,
        client: OkHttpClient?
    ): BatchManagerAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    private fun feedMixStartConvTimeot() {
        batchingConvTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (!batchingChIsResumed) {
                feedMixChickResume(BatchManagerAppsFlyerState.BatchManagerError)
            }
        }
    }

    private fun feedMixChickResume(state: BatchManagerAppsFlyerState) {
        batchingConvTimeoutJob?.cancel()
        if (state is BatchManagerAppsFlyerState.BatchManagerSuccess) {
            val convData = state.feedMixxChickkData ?: mutableMapOf()
            val deepData = batchingManagerDeepLinksMap ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!batchingChIsResumed) {
                batchingChIsResumed = true
                BatchingManagerConversionFlow.value = BatchManagerAppsFlyerState.BatchManagerSuccess(merged)
            }
        } else {
            if (!batchingChIsResumed) {
                batchingChIsResumed = true
                BatchingManagerConversionFlow.value = state
            }
        }
    }

    private fun feedMix(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }
}



interface BatchManagerAppsApi {
    @Headers("Content-Type: application/json")
    @GET(BATCH_MANAGER_MIX_LIN)
    fun eggLabelGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val BATCH_MANAGER_APPSFLYER_DEV = "R8tzxxvZ2CFuyMEuz9r6SC"
private const val BATCH_MANAGER_MIX_LIN = "com.managerbatching.batchmanager"

sealed interface BatchManagerAppsFlyerState {
    data object BatchManagerDefault : BatchManagerAppsFlyerState
    data class BatchManagerSuccess(val feedMixxChickkData: MutableMap<String, Any>?) :
        BatchManagerAppsFlyerState

    data object BatchManagerError : BatchManagerAppsFlyerState
}