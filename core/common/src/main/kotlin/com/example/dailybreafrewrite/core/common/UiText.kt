package com.example.dailybreafrewrite.core.common

sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class StringResource(val id: Int, val args: List<Any> = emptyList()) : UiText
}
