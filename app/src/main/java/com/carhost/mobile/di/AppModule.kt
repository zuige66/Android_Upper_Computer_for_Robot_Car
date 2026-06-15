package com.carhost.mobile.di

import android.content.Context
import androidx.room.Room
import com.carhost.mobile.data.local.db.AppDatabase
import com.carhost.mobile.data.local.db.LogRecordDao
import com.carhost.mobile.data.repository.TcpVehicleRepository
import com.carhost.mobile.data.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindings {
    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        repository: TcpVehicleRepository,
    ): VehicleRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "car_host.db",
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideLogRecordDao(
        database: AppDatabase,
    ): LogRecordDao = database.logRecordDao()
}
