package com.gmail.hamedvakhide.runningtracker.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.gmail.hamedvakhide.runningtracker.data.RunDao
import com.gmail.hamedvakhide.runningtracker.data.RunDatabase
import com.gmail.hamedvakhide.runningtracker.util.Constants
import com.gmail.hamedvakhide.runningtracker.util.Constants.KEY_FIRST_TIME_TOGGLE
import com.gmail.hamedvakhide.runningtracker.util.Constants.KEY_NAME
import com.gmail.hamedvakhide.runningtracker.util.Constants.KEY_WEIGHT
import com.gmail.hamedvakhide.runningtracker.util.Constants.SHARED_PREF_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunDatabase(
        @ApplicationContext context: Context
    ): RunDatabase {
        return Room.databaseBuilder(context, RunDatabase::class.java, "db_run")
            .build()
    }

    @Provides
    @Singleton
    fun provideRunDao(runDatabase: RunDatabase): RunDao {
        return runDatabase.getRunDao()
    }

    @Provides
    @Singleton
    fun provideSharedPrefs(@ApplicationContext context: Context) =
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideName(sharedPrefs: SharedPreferences) = sharedPrefs.getString(KEY_NAME, "") ?: ""

    @Provides
    @Singleton
    fun provideWeight(sharedPrefs: SharedPreferences) = sharedPrefs.getFloat(KEY_WEIGHT, 75F)

    @Provides
    @Singleton
    fun provideIsFirstTime(sharedPrefs: SharedPreferences) = sharedPrefs.getBoolean(
        KEY_FIRST_TIME_TOGGLE, true
    )
}