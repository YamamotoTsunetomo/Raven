package com.tsunetomo.raven.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsunetomo.raven.data.Repo
import com.tsunetomo.raven.model.Book
import com.tsunetomo.raven.model.BookDetails
import com.tsunetomo.raven.model.DownloadLinkJobDoneException
import com.tsunetomo.raven.model.InvalidDownloadPageException
import com.tsunetomo.raven.util.Resource
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class SearchViewModel(private val repo: Repo) : ViewModel() {
    private val _books: MutableStateFlow<Resource<List<Book>>> =
        MutableStateFlow(Resource.Loading())
    private val booksHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("BOOKS LIST ERROR", throwable.toString())
        _books.value = Resource.Error(throwable)
    }
    val books = _books.asStateFlow()


    private val _bookDetails: MutableStateFlow<Resource<BookDetails>> =
        MutableStateFlow(Resource.Loading())
    private val bookDetailsHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("BOOK DETAILS ERROR", throwable.toString())
        _bookDetails.value = Resource.Error(throwable)
    }
    val bookDetails = _bookDetails.asStateFlow()

    private val _downloadLink: MutableStateFlow<Resource<String>> =
        MutableStateFlow(Resource.Loading())
    private val downloadLinkHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("DOWNLOAD LINK ERROR", throwable.toString())
        _downloadLink.value = Resource.Error(throwable)
    }
    val downloadLink = _downloadLink.asStateFlow()


    fun searchBook(
        query: String,
        fileExtensions: List<String>,
        sortBy: String,
    ) = viewModelScope.launch(booksHandler) {
        _books.emit(Resource.Loading())
        val resp = repo.searchBook(query, fileExtensions, sortBy)
        _books.emit(Resource.Success(resp))
    }

    fun getBookDetails(book: Book) = viewModelScope.launch(bookDetailsHandler) {
        _bookDetails.emit(Resource.Loading())
        val resp = repo.getBookInfo(book)
        resp.book = book
        _bookDetails.emit(Resource.Success(resp))
    }

    fun getDownloadLink() = viewModelScope.launch(downloadLinkHandler) {
        try {
            val mirrors = (bookDetails.value as? Resource.Success)?.data?.mirrors
            if (mirrors.isNullOrEmpty()) {
                _downloadLink.value = Resource.Error(Exception())
                return@launch
            }

            val links = mutableListOf<Deferred<String?>>()

            mirrors.forEach { mirror ->
                val deferredResult = async { tryMirror(mirror) }.apply {
                    select {
                        this@apply.onAwait { res ->
                            if (!res.isNullOrEmpty()) {
                                throw DownloadLinkJobDoneException(res)
                            }
                        }
                    }
                }
                links.add(deferredResult)
            }

            links.awaitAll().filterNotNull().ifEmpty {
                _downloadLink.value = Resource.Error(Exception())
            }
        } catch (d: DownloadLinkJobDoneException) {
            _downloadLink.value = Resource.Success(d.link)
        }
    }

    fun invalidateCaptchaPage() = viewModelScope.launch {
        _downloadLink.emit(Resource.Error(InvalidDownloadPageException()))
    }

    private suspend fun tryMirror(mirror: String): String? {
        return try {
            repo.getDownloadLink(mirror)
        } catch (_: Exception) {
            null
        }
    }

    fun clearDownloadLink() {
        _downloadLink.value = Resource.Loading()
    }
}
