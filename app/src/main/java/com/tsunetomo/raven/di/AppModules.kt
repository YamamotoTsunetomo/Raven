package com.tsunetomo.raven.di

import com.tsunetomo.raven.data.Api
import com.tsunetomo.raven.data.NetworkInterceptor
import com.tsunetomo.raven.data.Repo
import com.tsunetomo.raven.ui.search.SearchViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

const val ANNAS_ARCHIVE_BASE_URL = "https://annas-archive.org/"

private val apiModule = module {
    val f = { rb: Retrofit.Builder ->
        rb.build().create(Api::class.java)
    }

    factory { f(get()) }
}

private val okHttpClientModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(NetworkInterceptor(get()))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .build()
    }
}

private val retrofitBuilderModule = module {
    factory {
        Retrofit.Builder()
            .client(get())
            .baseUrl(ANNAS_ARCHIVE_BASE_URL)
    }
}

private val repoModule = module {
    single { Repo(get()) }
}

private val searchVMModule = module {
    viewModel { SearchViewModel(get()) }
}

val appModules = arrayOf(
    apiModule,
    okHttpClientModule,
    retrofitBuilderModule,
    repoModule,
    searchVMModule,
)