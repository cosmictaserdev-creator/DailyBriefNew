package com.example.dailybreafrewrite.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.dailybreafrewrite.core.common.AppConstants
import com.example.dailybreafrewrite.core.data.local.ArticleDao
import com.example.dailybreafrewrite.core.data.local.DailyBriefDatabase
import com.example.dailybreafrewrite.core.data.remote.NewsApi
import com.example.dailybreafrewrite.core.data.repository.ArticleRepositoryImpl
import com.example.dailybreafrewrite.core.domain.repository.ArticleRepository
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideApiKeyInterceptor(
        @NewsApiKey apiKey: String,
    ): Interceptor = Interceptor { chain ->
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("apiKey", apiKey)
            .build()
        val request = original.newBuilder()
            .url(url)
            .build()
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyInterceptor: Interceptor,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(AppConstants.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideNewsApi(retrofit: Retrofit): NewsApi {
        return retrofit.create(NewsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DailyBriefDatabase {
        return Room.databaseBuilder(
            context,
            DailyBriefDatabase::class.java,
            "dailybrief.db",
        ).build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: DailyBriefDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    @Singleton
    fun provideArticleRepository(impl: ArticleRepositoryImpl): ArticleRepository {
        return impl
    }
}
