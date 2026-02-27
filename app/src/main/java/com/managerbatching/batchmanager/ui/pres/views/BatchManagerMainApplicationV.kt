package com.managerbatching.batchmanager.ui.pres.views

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.managerbatching.batchmanager.MainApplication
import com.managerbatching.batchmanager.ui.pres.ui.BatchManagerLoadingSplashFragment
import org.koin.android.ext.android.inject

class BatchManagerMainApplicationV : Fragment() {

    private lateinit var eggLabelPhoto: Uri
    private var eggLabelFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val eggLabelTakeFile: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            eggLabelFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
            eggLabelFilePathFromChrome = null
        }

    private val eggLabelTakePhoto: ActivityResultLauncher<Uri> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                eggLabelFilePathFromChrome?.onReceiveValue(arrayOf(eggLabelPhoto))
                eggLabelFilePathFromChrome = null
            } else {
                eggLabelFilePathFromChrome?.onReceiveValue(null)
                eggLabelFilePathFromChrome = null
            }
        }

    private val batchManagerDataStore by activityViewModels<BatchManagerDataStore>()


    private val batchManagerMainViFun by inject<BatchManagerMainViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MainApplication.BATCH_MANAGER_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (batchManagerDataStore.batchManagerMainApplicationView.canGoBack()) {
                        batchManagerDataStore.batchManagerMainApplicationView.goBack()
                    } else if (batchManagerDataStore.batchManagerMainApplicationViList.size > 1) {
                        batchManagerDataStore.batchManagerMainApplicationViList.removeAt(batchManagerDataStore.batchManagerMainApplicationViList.lastIndex)
                        batchManagerDataStore.batchManagerMainApplicationView.destroy()
                        val previousWebView = batchManagerDataStore.batchManagerMainApplicationViList.last()
                        eggLabelAttachWebViewToContainer(previousWebView)
                        batchManagerDataStore.batchManagerMainApplicationView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (batchManagerDataStore.feedMixIsFirstCreate) {
            batchManagerDataStore.feedMixIsFirstCreate = false
            batchManagerDataStore.feedMixContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return batchManagerDataStore.feedMixContainerView
        } else {
            return batchManagerDataStore.feedMixContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (batchManagerDataStore.batchManagerMainApplicationViList.isEmpty()) {
            batchManagerDataStore.batchManagerMainApplicationView = BatchManagerMainApplicationVi(requireContext(), object :
                BatchManagerCallBack {
                override fun feedMixHandleCreateWebWindowRequest(batchManagerMainApplicationVi: BatchManagerMainApplicationVi) {
                    batchManagerDataStore.batchManagerMainApplicationViList.add(batchManagerMainApplicationVi)
                    batchManagerDataStore.batchManagerMainApplicationView = batchManagerMainApplicationVi
                    batchManagerMainApplicationVi.eggLabelSetFileChooserHandler { callback ->
                        eggLabelHandleFileChooser(callback)
                    }
                    eggLabelAttachWebViewToContainer(batchManagerMainApplicationVi)
                }

            }, eggLabelWindow = requireActivity().window).apply {
                eggLabelSetFileChooserHandler { callback ->
                    eggLabelHandleFileChooser(callback)
                }
            }
            batchManagerDataStore.batchManagerMainApplicationView.eggLabelFLoad(
                arguments?.getString(BatchManagerLoadingSplashFragment.FEED_MIX_D) ?: ""
            )
            batchManagerDataStore.batchManagerMainApplicationViList.add(batchManagerDataStore.batchManagerMainApplicationView)
            eggLabelAttachWebViewToContainer(batchManagerDataStore.batchManagerMainApplicationView)
        } else {
            batchManagerDataStore.batchManagerMainApplicationViList.forEach { webView ->
                webView.eggLabelSetFileChooserHandler { callback ->
                    eggLabelHandleFileChooser(callback)
                }
            }
            batchManagerDataStore.batchManagerMainApplicationView = batchManagerDataStore.batchManagerMainApplicationViList.last()

            eggLabelAttachWebViewToContainer(batchManagerDataStore.batchManagerMainApplicationView)
        }
    }

    private fun eggLabelHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        eggLabelFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    eggLabelTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }

                1 -> {
                    eggLabelPhoto = batchManagerMainViFun.eggLabelSavePhoto()
                    eggLabelTakePhoto.launch(eggLabelPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                callback?.onReceiveValue(null)
                eggLabelFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun eggLabelAttachWebViewToContainer(w: BatchManagerMainApplicationVi) {
        batchManagerDataStore.feedMixContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            batchManagerDataStore.feedMixContainerView.removeAllViews()
            batchManagerDataStore.feedMixContainerView.addView(w)
        }
    }


}