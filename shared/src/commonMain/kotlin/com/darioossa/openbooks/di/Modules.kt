package com.darioossa.openbooks.di

import com.darioossa.openbooks.data.BooksRepository
import com.darioossa.openbooks.data.local.BooksLocal
import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemote
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.domain.SearchBooksUseCase
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.presentation.bookList.BooksListViewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val dataModule =
    module {
        single<BooksRemote>().bind(BooksRemoteSource::class)
        single<BooksLocal>().bind(BooksLocalSource::class)
        single<BooksRepository>().bind(BooksDataSource::class)
    }

val domainModule =
    module {
        includes(dataModule)
        factory<SearchBooksUseCase>()
    }

val presentationModule =
    module {
        includes(domainModule)
        viewModel<BooksListViewModel>()
    }

expect val platformModule: Module
