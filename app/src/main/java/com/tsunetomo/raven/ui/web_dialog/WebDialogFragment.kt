package com.tsunetomo.raven.ui.web_dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tsunetomo.raven.databinding.FragmentWebDialogBinding
import com.tsunetomo.raven.di.ANNAS_ARCHIVE_BASE_URL
import com.tsunetomo.raven.ui.search.SearchViewModel
import com.tsunetomo.raven.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("SetJavaScriptEnabled")
class WebDialogFragment : BottomSheetDialogFragment() {
    private var binding: FragmentWebDialogBinding? = null
    private val vm: SearchViewModel by viewModel(
        ownerProducer = { requireActivity() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentWebDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
    }

    private fun setupUi() = binding?.apply {
        val link = arguments?.getString("link") ?: run {
            vm.invalidateCaptchaPage()
            dismiss()
            return@apply
        }

        webView.settings.apply {
            javaScriptEnabled = true
            CookieManager.getInstance().setAcceptThirdPartyCookies(binding?.webView, true)
        }
        webView.loadUrl(link)
        webView.setBackgroundColor(0)
    }

    private fun setupObservers() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.downloadLink.collectLatest { res ->
                when (res) {
                    is Resource.Success -> {
                        dismiss()
                    }

                    is Resource.Error -> {
                        dismiss()
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
