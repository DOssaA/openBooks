package com.darioossa.openbooks.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org"

private val openLibraryConfig: HttpClientConfig<*>.() -> Unit = {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }
    defaultRequest { url(OPEN_LIBRARY_BASE_URL) }
}

/**
 * Builds the OpenLibrary Ktor client for dependency injection. The engine is resolved from the
 * classpath (OkHttp on Android). Kept parameterless so the Koin compiler plugin can wire it via
 * `single { create(::provideHttpClient) }`.
 */
fun provideHttpClient(): HttpClient = HttpClient(openLibraryConfig)

/** Same client over an explicit engine — used by tests to inject a [io.ktor.client.engine.mock.MockEngine]. */
fun openLibraryHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine, openLibraryConfig)
