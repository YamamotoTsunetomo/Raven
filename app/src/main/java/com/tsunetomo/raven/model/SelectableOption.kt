package com.tsunetomo.raven.model

data class SelectableOption<T>(
    val option: T,
    var selected: Boolean = false,
)
