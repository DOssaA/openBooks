package com.darioossa.openbooks

import com.darioossa.openbooks.di.dataModule
import com.darioossa.openbooks.di.domainModule
import com.darioossa.openbooks.di.platformModule
import com.darioossa.openbooks.di.presentationModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.test.verify.verify
import kotlin.test.Test

class SharedCommonTest {
    @Test
    fun checkAllModules() {
        // HttpClient is provided via `create(::provideHttpClient)`. verify() can't read the lambda's
        // return type and otherwise drills into Ktor's own constructor, so both the client and its
        // engine are declared as externally-provided types here.
        val externalTypes = listOf(HttpClient::class, HttpClientEngine::class)
        // Throws MissingKoinDefinitionException if definitions are incomplete
        presentationModule.verify(extraTypes = externalTypes)
        dataModule.verify(extraTypes = externalTypes)
        domainModule.verify(extraTypes = externalTypes)
        platformModule.verify()
    }
}
