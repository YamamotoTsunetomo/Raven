package com.tsunetomo.raven.data

import com.tsunetomo.raven.model.Book
import com.tsunetomo.raven.model.BookDetails
import com.tsunetomo.raven.ui.search.search_options.FILE_EXTENSIONS
import com.tsunetomo.raven.util.filterHtml
import com.tsunetomo.raven.util.getFirstAttr
import org.jsoup.Jsoup


class Repo(private val api: Api) {
    suspend fun searchBook(
        query: String,
        fileExtensions: List<String>,
        sortBy: String,
    ): List<Book> {
        val response = api.getBook(query, fileExtensions, sortBy)
        val body = response.body()
        val filtered = body?.string().toString().filterHtml(BOOK_SELECTOR_TAG, BOOK_SELECTOR_CLASS)

        val books = filtered.mapNotNull {
            val htmlBook = Jsoup.parse(it)

            val title = htmlBook.select(TITLE_SELECTOR).firstOrNull()?.text()
            val author = htmlBook.select(AUTHOR_SELECTOR).firstOrNull()?.text()
            val extension = FILE_EXTENSIONS.firstOrNull { fileType ->
                htmlBook.select(DESCRIPTION_SELECTOR).firstOrNull()?.text()
                    ?.indexOf(fileType.query.orEmpty()) != -1
            }
            val image = htmlBook.select("img").attr("src")
            val detailsLink = it.getFirstAttr("href")
            val publisher = htmlBook.select(PUBLISHER_SELECTOR).firstOrNull()?.text()

            if (extension == null) {
                null
            } else {
                Book(
                    title = title,
                    author = author,
                    image = image,
                    detailsId = detailsLink,
                    publisher = publisher,
                    extension = extension.displayName,
                )
            }
        }
        return books
    }

    suspend fun getBookInfo(book: Book): BookDetails {
        val response = api.getBookInfo(book.detailsId.orEmpty())
        val body = response.body()
        val mainTag = body?.string().toString().filterHtml("main", "main").first()

        val parsedHtml = Jsoup.parse(mainTag)
        val description =
            parsedHtml.select("div[class=\"mt-4 line-clamp-[5] js-md5-top-box-description\"]")
                .firstOrNull()?.text()

        val mirrors = mainTag
            .filterHtml("a", "js-download-link")
            .asSequence()
            .map { it.replace("'", "\"") }
            .sortedBy { it.length }
            .mapNotNull { it.getFirstAttr("href") }
            .filter { it.startsWith("/slow_download/") }
            .toList()

        return BookDetails(description = description, mirrors = mirrors)
    }

    suspend fun getDownloadLink(mirror: String): String? {
        val response = api.getMirrorPage(mirror)
        val body = response.body()

        return body?.string().toString().filterHtml("a", "font-bold").first()
            .getFirstAttr("href")
    }

}

private const val BOOK_SELECTOR_TAG = "a"
private const val BOOK_SELECTOR_CLASS = "js-vim-focus"
private const val TITLE_SELECTOR = "h3"
private const val AUTHOR_SELECTOR =
    "div[class=\"max-lg:line-clamp-[2] lg:truncate leading-[1.2] lg:leading-[1.35] max-lg:text-sm italic\"]"
private const val DESCRIPTION_SELECTOR =
    "div[class=\"line-clamp-[2] leading-[1.2] text-[10px] lg:text-xs text-gray-500\"]"
private const val PUBLISHER_SELECTOR =
    "div[class=\"truncate leading-[1.2] lg:leading-[1.35] max-lg:text-xs\"]"
