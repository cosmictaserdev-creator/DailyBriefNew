package com.example.dailybreafrewrite.di

import com.example.dailybreafrewrite.BuildConfig
import com.example.dailybreafrewrite.core.data.di.NewsApiKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @NewsApiKey
    fun provideNewsApiKey(): String {
        return BuildConfig.NEWS_API_KEY
    }
}
