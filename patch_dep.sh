cat app/src/main/java/com/example/di/DependencyProvider.kt | \
sed -e 's/import com.example.data.remote.ItunesApi/import com.example.data.remote.ItunesApi\nimport com.example.data.remote.PipedApi/g' \
    -e 's/var repository: MusicRepository? = null/var repository: MusicRepository? = null\n    var pipedApi: PipedApi? = null/g' \
    -e 's/val itunesProvider = ItunesSearchProvider(itunesApi)/val itunesProvider = ItunesSearchProvider(itunesApi)\n\n            val pipedRetrofit = Retrofit.Builder()\n                .baseUrl("https:\/\/pipedapi.kavin.rocks\/")\n                .client(okHttpClient)\n                .addConverterFactory(MoshiConverterFactory.create(moshi))\n                .build()\n            pipedApi = pipedRetrofit.create(PipedApi::class.java)/g' \
> new_DependencyProvider.kt
mv new_DependencyProvider.kt app/src/main/java/com/example/di/DependencyProvider.kt
