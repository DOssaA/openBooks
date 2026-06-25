package com.darioossa.openbooks.di

import com.darioossa.openbooks.data.BooksRepository
import com.darioossa.openbooks.data.local.BooksLocal
import com.darioossa.openbooks.data.local.BooksLocalSource
import com.darioossa.openbooks.data.remote.BooksRemote
import com.darioossa.openbooks.data.remote.BooksRemoteSource
import com.darioossa.openbooks.data.remote.provideHttpClient
import com.darioossa.openbooks.domain.ObserveFavoriteKeysUseCase
import com.darioossa.openbooks.domain.ObserveFavoritesUseCase
import com.darioossa.openbooks.domain.SearchBooksUseCase
import com.darioossa.openbooks.domain.ToggleFavoriteUseCase
import com.darioossa.openbooks.domain.dataSource.BooksDataSource
import com.darioossa.openbooks.presentation.bookList.BooksListViewModel
import com.darioossa.openbooks.presentation.favorites.FavoritesViewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val dataModule =
    module {
        includes(platformModule)
        // The compiler plugin can't construct a manually-configured HttpClient, so it is provided
        // through `create(::factory)` — which still registers the type in the plugin's DSL graph.
        single { create(::provideHttpClient) }
        single<BooksRemote>().bind(BooksRemoteSource::class)
        single<BooksLocal>().bind(BooksLocalSource::class)
        single<BooksRepository>().bind(BooksDataSource::class)
    }

val domainModule =
    module {
        includes(dataModule)
        factory<ObserveFavoriteKeysUseCase>()
        factory<ObserveFavoritesUseCase>()
        factory<SearchBooksUseCase>()
        factory<ToggleFavoriteUseCase>()
    }

val presentationModule =
    module {
        includes(domainModule)
        viewModel<BooksListViewModel>()
        viewModel<FavoritesViewModel>()
    }

expect val platformModule: Module
