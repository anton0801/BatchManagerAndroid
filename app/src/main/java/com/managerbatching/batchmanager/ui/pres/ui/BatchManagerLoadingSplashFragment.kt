package com.managerbatching.batchmanager.ui.pres.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.managerbatching.batchmanager.MainActivity
import com.managerbatching.batchmanager.R
import com.managerbatching.batchmanager.databinding.FragmentLoadFeedMixBinding
import com.managerbatching.batchmanager.handlers.BatchManagerLocalStoreManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class BatchManagerLoadingSplashFragment : Fragment(R.layout.fragment_load_feed_mix) {
    private lateinit var batchManagerLoadingBinding: FragmentLoadFeedMixBinding

    private val batchManagerLoadingSplashVM by viewModel<BatchManagerLoadingSplashVM>()

    private val batchManagerLocalStoreManager by inject<BatchManagerLocalStoreManager>()

    private var batchManagerUrl = ""

    private val chickHealthRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            eggLabelNavigateToSuccess(batchManagerUrl)
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                batchManagerLocalStoreManager.batchNotificationRequest =
                    (System.currentTimeMillis() / 1000) + 2592000000
                eggLabelNavigateToSuccess(batchManagerUrl)
            } else {
                eggLabelNavigateToSuccess(batchManagerUrl)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 999 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            eggLabelNavigateToSuccess(batchManagerUrl)
        } else {
            // твой код на отказ
            eggLabelNavigateToSuccess(batchManagerUrl)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        batchManagerLoadingBinding = FragmentLoadFeedMixBinding.bind(view)

        batchManagerLoadingBinding.feedMixGrandButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val eggLabelPermission = Manifest.permission.POST_NOTIFICATIONS
                chickHealthRequestNotificationPermission.launch(eggLabelPermission)
                batchManagerLocalStoreManager.batchNotificationRequestedBefore = true
            } else {
                eggLabelNavigateToSuccess(batchManagerUrl)
                batchManagerLocalStoreManager.batchNotificationRequestedBefore = true
            }
        }

        batchManagerLoadingBinding.feedMixSkipButton.setOnClickListener {
            batchManagerLocalStoreManager.batchNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            eggLabelNavigateToSuccess(batchManagerUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                batchManagerLoadingSplashVM.chickHealthHomeScreenState.collect {
                    when (it) {
                        is BatchManagerLoadingSplashVM.FeedMixHomeScreenState.FeedMixLoading -> {
                        }

                        is BatchManagerLoadingSplashVM.FeedMixHomeScreenState.FeedMixError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is BatchManagerLoadingSplashVM.FeedMixHomeScreenState.FeedMixSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val eggLabelPermission = Manifest.permission.POST_NOTIFICATIONS
                                val eggLabelPermissionRequestedBefore =
                                    batchManagerLocalStoreManager.batchNotificationRequestedBefore

                                if (ContextCompat.checkSelfPermission(
                                        requireContext(),
                                        eggLabelPermission
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    eggLabelNavigateToSuccess(it.data)
                                } else if (!eggLabelPermissionRequestedBefore && (System.currentTimeMillis() / 1000 > batchManagerLocalStoreManager.batchNotificationRequest)) {
                                    // первый раз — показываем UI для запроса
                                    batchManagerLoadingBinding.feedMixNotiGroup.visibility =
                                        View.VISIBLE
                                    batchManagerLoadingBinding.feedMixLoadingGroup.visibility =
                                        View.GONE
                                    batchManagerUrl = it.data
                                } else if (shouldShowRequestPermissionRationale(eggLabelPermission)) {
                                    // временный отказ — через 3 дня можно показать
                                    if (System.currentTimeMillis() / 1000 > batchManagerLocalStoreManager.batchNotificationRequest) {
                                        batchManagerLoadingBinding.feedMixNotiGroup.visibility =
                                            View.VISIBLE
                                        batchManagerLoadingBinding.feedMixLoadingGroup.visibility =
                                            View.GONE
                                        batchManagerUrl = it.data
                                    } else {
                                        eggLabelNavigateToSuccess(it.data)
                                    }
                                } else {
                                    eggLabelNavigateToSuccess(it.data)
                                }
                            } else {
                                eggLabelNavigateToSuccess(it.data)
                            }
                        }

                        BatchManagerLoadingSplashVM.FeedMixHomeScreenState.FeedMixNotInternet -> {
                            batchManagerLoadingBinding.feedMixStateGroup.visibility = View.VISIBLE
                            batchManagerLoadingBinding.feedMixLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun eggLabelNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_batchManagerLoadingSplashFragment_to_batchManagerMainApplicationV,
            bundleOf(FEED_MIX_D to data)
        )
    }


    companion object {
        const val FEED_MIX_D = "eggLabelData"
    }
}