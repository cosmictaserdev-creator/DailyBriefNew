package com.example.dailybreafrewrite.core.common

fun <T> Resource<T>.dataOrNull(): T? {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> null
    }
}

fun Resource<*>.cachedDataOrNull(): Any? {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> cachedData
    }
}
