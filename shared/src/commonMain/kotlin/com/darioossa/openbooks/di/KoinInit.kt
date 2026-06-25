package com.darioossa.openbooks.di

import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes

fun initKoin(config: KoinAppDeclaration? = null): KoinApplication =
    startKoin {
        includes(config)
        modules(
            presentationModule,
            dataModule,
            domainModule,
            platformModule,
        )
    }
