package com.tsunetomo.raven.ui.details

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tsunetomo.raven.databinding.FragmentDetailsBinding
import com.tsunetomo.raven.ui.search.SearchViewModel
import com.tsunetomo.raven.ui.web_dialog.WebDialogFragment
import com.tsunetomo.raven.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class DetailsFragment : Fragment() {
    private var binding: FragmentDetailsBinding? = null
    private val vm: SearchViewModel by viewModel(
        ownerProducer = { requireActivity() }
    )
    private val args: DetailsFragmentArgs by navArgs()
    private val permissionLauncher = registerForActivityResult(RequestMultiplePermissions()) { mp ->
        if (mp.containsValue(false)) {
            Toast.makeText(requireContext(), "Needs permission", Toast.LENGTH_SHORT).show()
        } else {
            startDownload((vm.downloadLink.value as? Resource.Success)?.data)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupListeners()
        setupObservers()
    }

    private fun setupUi() = binding?.apply {
        tvTitle.text = args.book?.title
        tvAuthor.text = args.book?.author
        tvPublisher.text = args.book?.publisher

        Glide.with(requireContext())
            .load(args.book?.image)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(ivCover)

        btnDownload.apply {
            this.buttonModeText = "Download"
            this.progressBarModeText = "Installing..."
            switchMode(buttonMode = true)
        }
    }

    private fun setupListeners() = binding?.apply {
        btnDownload.addClickListener {
            vm.getDownloadLink()
            Toast.makeText(requireContext(), "DOWNLOAD", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch { observeBookDetails() }
            launch { observeDownloadLink() }
        }
    }

    private suspend fun observeBookDetails() = vm.bookDetails.collectLatest { res ->
        when (res) {
            is Resource.Success -> {
                binding?.tvDescription?.text = res.data?.description
            }

            is Resource.Error -> {
                Toast.makeText(requireContext(), "${res.error}", Toast.LENGTH_SHORT).show()
            }

            is Resource.Loading -> Unit
        }
    }

    private suspend fun observeDownloadLink() = vm.downloadLink.collectLatest { res ->
        when (res) {
            is Resource.Success -> {
                val link = res.data
                when {
                    VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 ||
                            checkSelfPermission(
                                requireContext(),
                                WRITE_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED -> {
                        startDownload(link)
                    }

                    else -> {
                        permissionLauncher.launch(arrayOf(WRITE_EXTERNAL_STORAGE))
                    }
                }
            }

            is Resource.Error -> {
                Toast.makeText(requireContext(), "${res.error}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

            is Resource.Loading -> Unit
        }
    }

    private fun openCaptchaPage() {
        vm.clearDownloadLink()
        WebDialogFragment().apply { arguments = bundleOf("link" to args.book?.detailsId) }
            .show(childFragmentManager, null)
    }

    private fun startDownload(link: String?) {
        try {

            link ?: return

            val title = args.book?.title.orEmpty()
            val fileTitle = title.split(" ").joinToString("_")
            val fileType = args.book?.extension

            val request = DownloadManager.Request(Uri.parse(link))
                .setTitle(title)
                .setDescription("Downloading..")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "$fileTitle.${fileType?.lowercase()}"
                )
                .setAllowedOverRoaming(true)
                .setAllowedOverMetered(true)

            val dm = requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        } catch (e: Exception) {
            Log.e("DOWNLOAD EXCEPTION", e.toString())
            openCaptchaPage()
        }
    }

    override fun onStop() {
        super.onStop()
        vm.clearDownloadLink()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
