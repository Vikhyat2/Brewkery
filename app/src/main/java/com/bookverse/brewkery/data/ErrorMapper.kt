package com.bookverse.brewkery.data


import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/** Turns low-level exceptions into something we can show a customer. */
fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Couldn't reach Brewkery. Check your internet connection and try again."
    is HttpException -> "The menu server returned an error (${code()}). Please try again."
    is SerializationException -> "We got an unexpected response from the server."
    else -> "Something went wrong. Please try again."
}
