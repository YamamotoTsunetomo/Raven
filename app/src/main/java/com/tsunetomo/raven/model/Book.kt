package com.tsunetomo.raven.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val title: String? = null,
    val author: String? = null,
    val image: String? = null,
    val detailsId: String? = null,
    val publisher: String? = null,
    val extension: String? = null,
) : Parcelable