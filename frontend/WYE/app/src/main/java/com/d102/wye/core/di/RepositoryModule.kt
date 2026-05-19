package com.d102.wye.core.di

import com.d102.wye.data.repository.AlertRepositoryImpl
import com.d102.wye.data.repository.AuthRepositoryImpl
import com.d102.wye.data.repository.EtfRepositoryImpl
import com.d102.wye.data.repository.NewsRepositoryImpl
import com.d102.wye.data.repository.PortfolioRepositoryImpl
import com.d102.wye.data.repository.SimulationRepositoryImpl
import com.d102.wye.data.repository.IndexRepositoryImpl
import com.d102.wye.data.repository.StockRepositoryImpl
import com.d102.wye.data.repository.UserRepositoryImpl
import com.d102.wye.domain.repository.AlertRepository
import com.d102.wye.domain.repository.AuthRepository
import com.d102.wye.domain.repository.EtfRepository
import com.d102.wye.domain.repository.NewsRepository
import com.d102.wye.domain.repository.PortfolioRepository
import com.d102.wye.domain.repository.SimulationRepository
import com.d102.wye.domain.repository.IndexRepository
import com.d102.wye.domain.repository.StockRepository
import com.d102.wye.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 인터페이스 ↔ 구현체 연결 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEtfRepository(
        impl: EtfRepositoryImpl
    ): EtfRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        impl: NewsRepositoryImpl
    ): NewsRepository

    @Binds
    @Singleton
    abstract fun bindStockRepository(
        impl: StockRepositoryImpl
    ): StockRepository

    @Binds
    @Singleton
    abstract fun bindSimulationRepository(
        impl: SimulationRepositoryImpl
    ): SimulationRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(
        impl: PortfolioRepositoryImpl
    ): PortfolioRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl
    ): AlertRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindIndexRepository(
        impl: IndexRepositoryImpl
    ): IndexRepository
}
