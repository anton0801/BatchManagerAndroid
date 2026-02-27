package com.managerbatching.batchmanager.ui.pres.views

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class BatchManagerDataStore : ViewModel(){
    val batchManagerMainApplicationViList: MutableList<BatchManagerMainApplicationVi> = mutableListOf()
    var feedMixIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var feedMixContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var batchManagerMainApplicationView: BatchManagerMainApplicationVi

}