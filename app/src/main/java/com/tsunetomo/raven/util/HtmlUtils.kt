package com.tsunetomo.raven.util

import kotlinx.coroutines.flow.MutableStateFlow

fun String.filterHtml(tag: String, className: String?): List<String> {
    val tagStart = "<$tag"
    val tagEnd = "</$tag>"

    val result = mutableListOf<String>()
    var searchIndex = -1

    while (true) {
        val tagStartIndex = indexOf(tagStart, searchIndex)
        val tagEndIndex = indexOf(tagEnd, tagStartIndex)

        if (tagStartIndex == -1 || tagEndIndex == -1) break
        val element = substring(tagStartIndex, tagEndIndex + tagEnd.length)

        if (className == null || element.contains("class=\"$className")) {
            result.add(element)
        }
        searchIndex = tagEndIndex + tagEnd.length
    }


    return result
}

fun String.getFirstAttr(attr: String): String? {
    val pat = "$attr=\""
    val startIndex = indexOf(pat)
    if (startIndex == -1) {
        return null
    }
    val endIndex = indexOf("\"", startIndex + pat.length)
    if (endIndex == -1) return null
    return substring(startIndex + pat.length until endIndex)
}