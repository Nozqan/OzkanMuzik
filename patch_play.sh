awk '
/val pipedApi = com.example.di.DependencyProvider.pipedApi/ {
    print "                    val pipedApi = com.example.di.DependencyProvider.pipedApi"
    print "                    if (pipedApi != null) {"
    print "                        val searchQuery = \"${song.artist} ${song.title}\""
    print "                        val instances = listOf("
    print "                            \"https://pipedapi.nosebs.ru\","
    print "                            \"https://api.piped.privacydev.net\","
    print "                            \"https://pipedapi.kavin.rocks\","
    print "                            \"https://pipedapi.smnz.de\""
    print "                        )"
    print "                        var success = false"
    print "                        for (instance in instances) {"
    print "                            try {"
    print "                                val searchResponse = kotlinx.coroutines.withContext(Dispatchers.IO) { pipedApi.search(\"${instance}/search\", searchQuery) }"
    print "                                val firstVideo = searchResponse.items?.firstOrNull { it.url?.contains(\"/watch?v=\") == true }"
    print "                                if (firstVideo != null) {"
    print "                                    val videoId = firstVideo.url?.substringAfter(\"/watch?v=\") ?: \"\""
    print "                                    if (videoId.isNotBlank()) {"
    print "                                        val streamResponse = kotlinx.coroutines.withContext(Dispatchers.IO) { pipedApi.getStreams(\"${instance}/streams/${videoId}\") }"
    print "                                        val bestAudio = streamResponse.audioStreams?.maxByOrNull { it.bitrate ?: 0 } ?: streamResponse.audioStreams?.firstOrNull()"
    print "                                        if (bestAudio?.url != null) {"
    print "                                            finalStreamUri = bestAudio.url"
    print "                                            finalDuration = (firstVideo.duration ?: 0) * 1000L"
    print "                                            success = true"
    print "                                            break"
    print "                                        }"
    print "                                    }"
    print "                                }"
    print "                            } catch (e: Exception) {"
    print "                                Log.e(TAG, \"Piped API Error on ${instance}: ${e.message}\")"
    print "                            }"
    print "                        }"
    print "                    }"
    skip=1
    next
}
/                } catch \(e: Exception\) {/ {
    if (skip) {
        # we are skipping the old catch block
        next
    }
}
/                    Log.e\(TAG, "Piped API Error: \$\{e.message\}"\)/ {
    if (skip) {
        next
    }
}
/                }/ {
    if (skip) {
        # this is the closing bracket of catch
        skip=0
        next
    }
}
skip { next }
{ print }
' app/src/main/java/com/example/presentation/player/PlayerStateHolder.kt > new_PlayerStateHolder.kt
mv new_PlayerStateHolder.kt app/src/main/java/com/example/presentation/player/PlayerStateHolder.kt
