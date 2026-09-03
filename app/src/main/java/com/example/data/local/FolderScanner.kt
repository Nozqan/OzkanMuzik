package com.example.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class FolderScanner(private val context: Context) {

    suspend fun scanFolder(folderUri: Uri): List<SongEntity> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<SongEntity>()
        try {
            val rootFolder = DocumentFile.fromTreeUri(context, folderUri)
            if (rootFolder != null && rootFolder.isDirectory) {
                scanDocumentFile(rootFolder, songs)
            }
        } catch (e: Exception) {
            Log.e("FolderScanner", "Error scanning folder", e)
        }
        songs
    }

    private fun scanDocumentFile(folder: DocumentFile, songs: MutableList<SongEntity>) {
        val files = folder.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                scanDocumentFile(file, songs)
            } else {
                if (file.type?.startsWith("audio/") == true || file.name?.endsWith(".mp3") == true || file.name?.endsWith(".m4a") == true || file.name?.endsWith(".wav") == true) {
                    extractMetadata(file)?.let { songs.add(it) }
                }
            }
        }
    }

    private fun extractMetadata(file: DocumentFile): SongEntity? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, file.uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.name ?: "Bilinmeyen Başlık"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Bilinmeyen Sanatçı"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L

            SongEntity(
                id = "saf_${file.uri.toString().hashCode()}",
                title = title,
                artist = artist,
                album = album,
                thumbnail = null,
                duration = duration,
                provider = "local_folder",
                sourceId = file.uri.toString(),
                streamAvailable = true
            )
        } catch (e: Exception) {
            Log.e("FolderScanner", "Error extracting metadata for ${file.uri}", e)
            null
        } finally {
            try { retriever.release() } catch (e: Exception) {}
        }
    }
}
