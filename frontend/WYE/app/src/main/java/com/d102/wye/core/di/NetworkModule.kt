package com.d102.wye.core.di

import com.d102.wye.core.app.Constants
import com.d102.wye.core.network.AuthTokenInterceptor
import com.d102.wye.core.network.TokenRefreshInterceptor
import com.d102.wye.data.remote.api.AlertApiService
import com.d102.wye.data.remote.api.AuthApiService
import com.d102.wye.data.remote.api.EtfApiService
import com.d102.wye.data.remote.api.NewsApiService
import com.d102.wye.data.remote.api.PortfolioApiService
import com.d102.wye.data.remote.api.SimulationApiService
import com.d102.wye.data.remote.api.UserApiService
import com.d102.wye.data.remote.api.IndexApiService
import com.d102.wye.data.remote.api.StockApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * OkHttpClient 제공
     *
     * AuthTokenInterceptor는 DataStore를 주입받아 자동 생성됨
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authTokenInterceptor: AuthTokenInterceptor,
        tokenRefreshInterceptor: TokenRefreshInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authTokenInterceptor)
            .addInterceptor(tokenRefreshInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideEtfApiService(retrofit: Retrofit): EtfApiService {
        return retrofit.create(EtfApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAlertApiService(retrofit: Retrofit): AlertApiService {
        return retrofit.create(AlertApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSimulationService(retrofit: Retrofit): SimulationApiService =
        retrofit.create(SimulationApiService::class.java)

    @Provides
    @Singleton
    fun providePortfolioApiService(retrofit: Retrofit): PortfolioApiService =
        retrofit.create(PortfolioApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideStockApiService(retrofit: Retrofit): StockApiService =
        retrofit.create(StockApiService::class.java)

    @Provides
    @Singleton
    fun provideIndexApiService(retrofit: Retrofit): IndexApiService =
        retrofit.create(IndexApiService::class.java)
}
