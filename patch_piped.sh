cat app/src/main/java/com/example/data/remote/PipedApi.kt | \
sed -e 's/@GET("search")/@GET\n    suspend fun search(@retrofit2.http.Url url: String, @Query("q") query: String, @Query("filter") filter: String = "music_songs"): PipedSearchResponse/g' \
    -e 's/@GET("streams\/{videoId}")/@GET\n    suspend fun getStreams(@retrofit2.http.Url url: String): PipedStreamResponse/g' \
> new_PipedApi.kt
mv new_PipedApi.kt app/src/main/java/com/example/data/remote/PipedApi.kt
