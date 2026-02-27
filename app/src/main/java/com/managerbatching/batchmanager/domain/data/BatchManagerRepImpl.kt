package com.managerbatching.batchmanager.domain.data

import android.util.Log
import com.managerbatching.batchmanager.MainApplication.Companion.BATCH_MANAGER_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.managerbatching.batchmanager.domain.model.BatchManagerEntity
import com.managerbatching.batchmanager.domain.model.BatchManagerMainParam
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.lang.Exception

interface BatchManagerLabelApiInterface {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun feedMixGetClient(
        @Body jsonString: JsonObject,
    ): Call<BatchManagerEntity>
}


private const val BATCH_MANAGER_MAIN_L = "https://battchmanager.com/"

class BatchManagerRepImpl {

    suspend fun batchManagerAppObtainClient(
        batchManagerMainParam: BatchManagerMainParam,
        eggLabelConversion: MutableMap<String, Any>?
    ): BatchManagerEntity? {
        val gson = Gson()
        val api = batchManagerAppAGetApi(BATCH_MANAGER_MAIN_L, null)

        val eggLabelJsonObject = gson.toJsonTree(batchManagerMainParam).asJsonObject
        eggLabelConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            eggLabelJsonObject.add(key, element)
        }
        return try {
            val eggLabelRequest: Call<BatchManagerEntity> = api.feedMixGetClient(
                jsonString = eggLabelJsonObject,
            )
            val eggLabelResult = eggLabelRequest.awaitResponse()
            if (eggLabelResult.code() == 200) {
                eggLabelResult.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d(BATCH_MANAGER_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun batchManagerAppAGetApi(url: String, client: OkHttpClient?): BatchManagerLabelApiInterface {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
