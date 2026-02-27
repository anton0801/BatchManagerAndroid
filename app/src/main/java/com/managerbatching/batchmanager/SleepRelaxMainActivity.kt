package com.managerbatching.batchmanager

import android.R
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.managerbatching.batchmanager.databinding.ActivitySleepRelaxMainBinding
import com.managerbatching.batchmanager.handlers.BatchManagerNotifPushHandler
import com.relaxationapp.relaxsleep.ui.theme.layout.FeedMixGlobalLayoutUtils
import com.relaxationapp.relaxsleep.ui.theme.layout.chickHealthSetupSystemBars
import org.koin.android.ext.android.inject

class SleepRelaxMainActivity : AppCompatActivity() {

    private val feedMixPushHandler by inject<BatchManagerNotifPushHandler>()

    private lateinit var binding: ActivitySleepRelaxMainBinding


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            chickHealthSetupSystemBars()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chickHealthSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivitySleepRelaxMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val feedMixRootView = findViewById<View>(R.id.content)
        FeedMixGlobalLayoutUtils().feedMixAssistActivity(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        ViewCompat.setOnApplyWindowInsetsListener(feedMixRootView) { feedMixView, feedMixInsets ->
            val feedMixSystemBars = feedMixInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val feedMixDisplayCutout =
                feedMixInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val feedMixIme = feedMixInsets.getInsets(WindowInsetsCompat.Type.ime())
            val feedMixTopPadding =
                kotlin.comparisons.maxOf(feedMixSystemBars.top, feedMixDisplayCutout.top)
            val feedMixLeftPadding =
                kotlin.comparisons.maxOf(feedMixSystemBars.left, feedMixDisplayCutout.left)
            val feedMixRightPadding =
                kotlin.comparisons.maxOf(feedMixSystemBars.right, feedMixDisplayCutout.right)
            window.setSoftInputMode(MainApplication.batchiungInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                val feedMixBottomInset =
                    kotlin.comparisons.maxOf(feedMixSystemBars.bottom, feedMixDisplayCutout.bottom)
                feedMixView.setPadding(
                    feedMixLeftPadding,
                    feedMixTopPadding,
                    feedMixRightPadding,
                    0
                )
                feedMixView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = feedMixBottomInset
                }
            } else {
                val feedMixBottomInset =
                    kotlin.comparisons.maxOf(
                        feedMixSystemBars.bottom,
                        feedMixDisplayCutout.bottom,
                        feedMixIme.bottom
                    )
                feedMixView.setPadding(
                    feedMixLeftPadding,
                    feedMixTopPadding,
                    feedMixRightPadding,
                    0
                )
                feedMixView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = feedMixBottomInset
                }
            }
            WindowInsetsCompat.CONSUMED
        }
        feedMixPushHandler.batchAppHandlePush(intent.extras)
    }

    override fun onResume() {
        super.onResume()
        chickHealthSetupSystemBars()
    }

}