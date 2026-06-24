package com.example.dailybreafrewrite.core.common

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: UiText, val cachedData: Any? = null) : Resource<Nothing>
}
