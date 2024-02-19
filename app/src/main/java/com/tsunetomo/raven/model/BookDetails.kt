package com.tsunetomo.raven.model

data class BookDetails(
    var book: Book? = null,
    val description: String? = null,
    val mirrors: List<String>? = null,
)
