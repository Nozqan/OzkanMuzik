package com.example.data.local

import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScanner(private val context: Context) {
    suspend fun scanLocalAudio(): List<SongEntity> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<SongEntity>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol).toString()
                val title = cursor.getString(titleCol) ?: "Unknown Title"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val album = cursor.getString(albumCol)
                val duration = cursor.getLong(durationCol)
                val data = cursor.getString(dataCol)

                songs.add(
                    SongEntity(
                        id = "local_$id",
                        title = title,
                        artist = artist,
                        album = album,
                        thumbnail = null,
                        duration = duration,
                        provider = "local_device",
                        sourceId = data,
                        streamAvailable = true
                    )
                )
            }
        }
        
        songs
    }
}
