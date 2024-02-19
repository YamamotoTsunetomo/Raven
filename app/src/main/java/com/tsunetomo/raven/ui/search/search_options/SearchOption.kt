package com.tsunetomo.raven.ui.search.search_options

import com.tsunetomo.raven.ui.search.search_options.SearchOption.*

val FILE_EXTENSIONS = listOf(PDF, EPUB, CBR, MOBI, FB2, CBZ, AZW3, DJVU, FB2_ZIP)
val SORT_TYPES = listOf(NEWEST, OLDEST, LARGEST, SMALLEST)

enum class SearchOption(val query: String?, val displayName: String) {
    // file extensions
    ANY_FILE_TYPE(null, "Any"),
    PDF("pdf", "PDF"),
    EPUB("epub", "EPUB"),
    CBR("cbr", "CBR"),
    MOBI("mobi", "MOBI"),
    FB2("fb2", "FB2"),
    CBZ("cbz", "CBZ"),
    AZW3("azw3", "AZW3"),
    DJVU("djvu", "DJVU"),
    FB2_ZIP("fb2.zip", "FB2.ZIP"),

    // sort types
    MOST_RELEVANT("", "Most relevant"),
    NEWEST("newest", "Newest"),
    OLDEST("oldest", "Oldest"),
    LARGEST("largest", "Largest"),
    SMALLEST("smallest", "Smallest");
}
