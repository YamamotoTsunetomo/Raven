package com.tsunetomo.raven.ui.search

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.tsunetomo.raven.R
import com.tsunetomo.raven.databinding.FragmentSearchBinding
import com.tsunetomo.raven.model.Book
import com.tsunetomo.raven.ui.search.search_options.FILE_EXTENSIONS
import com.tsunetomo.raven.ui.search.search_options.SORT_TYPES
import com.tsunetomo.raven.ui.search.search_options.SearchOption
import com.tsunetomo.raven.util.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {
    private var binding: FragmentSearchBinding? = null
    private val vm: SearchViewModel by viewModel(
        ownerProducer = { requireActivity() }
    )
    private val booksAdapter by lazy { BooksAdapter(::onBookClicked) }
    private var isSearchExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupListeners()
        setupObservers()
    }

    private fun setupUi() = binding?.apply {
        rvItems.adapter = booksAdapter
        rvItems.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
        }

        sovFileType.apply {
            multipleChoiceEnabled = true
            setTitle("File extensions")
            setAvailableOptions(
                FILE_EXTENSIONS,
                listOf(),
                SearchOption.ANY_FILE_TYPE
            )
        }


        sovSortType.apply {
            multipleChoiceEnabled = false
            setTitle("Sort by")
            setAvailableOptions(
                SORT_TYPES,
                listOf(SearchOption.MOST_RELEVANT),
                SearchOption.MOST_RELEVANT
            )
        }
    }

    private fun setupListeners() = binding?.apply {
        btnSearch.setOnClickListener {
            vm.searchBook(
                etSearch.text.toString(),
                sovFileType.getSelectedOptions().map { it.query.orEmpty() },
                sovSortType.getSelectedOptions().firstOrNull()?.query.orEmpty()
            )

            if (isSearchExpanded) {
                isSearchExpanded = false
                ivFilter.setBackgroundResource(R.drawable.ic_filter)
                sovFileType.hideOptions()
                sovSortType.hideOptions()
                toggleSearchView(false)
            }

            hideKeyboard()
        }

        btnFilter.setOnClickListener {
            isSearchExpanded = !isSearchExpanded

            if (!isSearchExpanded) {
                sovSortType.clearFilter()
                sovFileType.clearFilter()
                Toast.makeText(requireContext(), "Filters removed", Toast.LENGTH_SHORT).show()
            }

            ivFilter.setBackgroundResource(if (isSearchExpanded) R.drawable.ic_clear else R.drawable.ic_filter)
            toggleSearchView(isSearchExpanded)
        }
    }

    private fun setupObservers() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.books.collectLatest { res ->
                binding?.bookLoadingView?.isVisible = res is Resource.Loading
                binding?.rvItems?.isVisible = res is Resource.Success

                when (res) {
                    is Resource.Success -> {
                        val items = res.data.orEmpty()
                        booksAdapter.submit(items)
                        if (items.isEmpty()) binding?.bookLoadingView?.isVisible = true
                    }

                    is Resource.Error -> {
                        booksAdapter.submit(emptyList())
                        Toast.makeText(requireContext(), "${res.error}", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun onBookClicked(book: Book) {
        val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(book)
        vm.getBookDetails(book)
        findNavController().navigate(action)
    }

    private fun toggleSearchView(maximize: Boolean) {
        val unwrappedHeight = requireContext().toPx(66)

        val itemsWrapped = resources.displayMetrics.heightPixels.toFloat()
        val searchWrapped = itemsWrapped / 2

        binding?.bookLoadingView?.isVisible = !maximize
        binding?.sovFileType?.isVisible = maximize
        binding?.sovSortType?.isVisible = maximize

        val itemsValues = if (maximize) {
            floatArrayOf(unwrappedHeight, itemsWrapped)
        } else {
            floatArrayOf(itemsWrapped, unwrappedHeight)
        }

        val searchValues = if (maximize) {
            floatArrayOf(unwrappedHeight, searchWrapped)
        } else {
            floatArrayOf(searchWrapped, unwrappedHeight)
        }

        val animatorItems = ValueAnimator.ofFloat(*itemsValues).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 300L
            addUpdateListener {
                binding?.itemsGuideline?.setGuidelineBegin((it.animatedValue as Float).toInt())
            }
        }


        val animatorSearch = ValueAnimator.ofFloat(*searchValues).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = 300L
            addUpdateListener {
                binding?.searchGuideline?.setGuidelineBegin((it.animatedValue as Float).toInt())
            }
        }

        animatorItems.start()
        animatorSearch.start()
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun Context.toPx(dp: Int): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        resources.displayMetrics
    )

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}