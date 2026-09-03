cat app/src/main/java/com/example/data/provider/ItunesSearchProvider.kt | \
sed -e 's/title = "${track.trackName} (30s ÖNİZLEME)",/title = track.trackName ?: "Bilinmeyen Başlık",/g' \
    -e 's/duration = 30000L, \/\/ iTunes previews are exactly 30 seconds, not the full track time/duration = track.trackTimeMillis ?: 0L,/g' \
> new_ItunesSearchProvider.kt
mv new_ItunesSearchProvider.kt app/src/main/java/com/example/data/provider/ItunesSearchProvider.kt
