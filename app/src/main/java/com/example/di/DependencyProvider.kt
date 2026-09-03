package com.example.di

import android.content.Context
import androidx.room.Room
import coil.Coil
import com.example.data.local.AppDatabase
import com.example.data.provider.ItunesSearchProvider
import com.example.data.remote.ItunesApi
import com.example.data.remote.PipedApi
import com.example.data.repository.MusicRepository
import com.example.data.source.RoomLocalDataSource
import com.example.utils.CacheManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object DependencyProvider {
    private var database: AppDatabase? = null
    var repository: MusicRepository? = null
    var pipedApi: PipedApi? = null

    fun initialize(context: Context) {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "akrep_music.db"
            )
            .fallbackToDestructiveMigration()
            .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val itunesRetrofit = Retrofit.Builder()
                .baseUrl("https://itunes.apple.com/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            val itunesApi = itunesRetrofit.create(ItunesApi::class.java)
            val itunesProvider = ItunesSearchProvider(itunesApi)

            val pipedRetrofit = Retrofit.Builder()
                .baseUrl("https://pipedapi.kavin.rocks/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            pipedApi = pipedRetrofit.create(PipedApi::class.java)

            val localDataSource = RoomLocalDataSource(database!!.musicDao())

            repository = MusicRepository(
                localDataSource, 
                listOf(itunesProvider)
            )

            val imageLoader = CacheManager.initializeCoil(context)
            Coil.setImageLoader(imageLoader)

            com.example.presentation.player.PlayerStateHolder.initialize(context)
        }
    }
}
