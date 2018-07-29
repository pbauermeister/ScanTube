package digital.bauermeister.scantube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import digital.bauermeister.scantube.googlevision.GoogleVision
import digital.bauermeister.scantube.youtube.YouTube
import io.fotoapparat.result.BitmapPhoto

fun queryImageAndStartYouTube(context: Context, imageBase64: String, forAlbum: Boolean,
                              logger: (String) -> Unit, updateState: (State, Bitmap?) -> Unit) {
    val label = GoogleVision.getAnnotateWebDetectionFirstLabel(imageBase64)
    logger("Found label: " + label)
    updateState(State.EVALUATING, null)
    if (label == null) return

    // Playlist
    if (forAlbum) {
        val searchString = label + " and full album"
        val playlistId = YouTube.getSearchPlaylistFirstId(searchString)
        logger(if (playlistId == null) "No playlist found" else "Playlist found")
        if (playlistId == null) return
        // TODO: playlist failed: search for just label, w/o full album

        val videoId = YouTube.getPlaylistItemsFirstVideoId(playlistId)
        logger(if (playlistId == null) "No video found" else "First video of playlist found")
        if (videoId == null) return
        // TODO: video of playlist failed: search for video with label, no playlist

        val url = "https://www.youtube.com/watch?v=$videoId&list=$playlistId"
        Log.i("MainLogic", "*** url: " + url)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)

        updateState(State.READY, null)
        context.startActivity(i)
    }

    // Video
    else {
        val searchString = label + " and music"
        val videoId = YouTube.getSearchVideoFirstId(searchString)
        logger(if (videoId == null) "No video found" else "Video found")
        if (videoId == null) return

        val url = "https://www.youtube.com/watch?v=$videoId"
        Log.i("MainLogic", "*** url: " + url)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)

        updateState(State.READY, null)
        context.startActivity(i)
    }
}

fun processBitmap(activity: Activity, bitmapPhoto: BitmapPhoto, forAlbum: Boolean,
                  logger: (String) -> Unit, updateState: (State, Bitmap?) -> Unit) {
    logger("Image captured")

    val newBitmap = sizeBitmap(bitmapPhoto.bitmap, -bitmapPhoto.rotationDegrees)
    val imageBase64 = encodeBitmapTobase64(newBitmap)

    updateState(State.QUERYING, newBitmap)
    queryImageAndStartYouTube(activity, imageBase64, forAlbum, logger, updateState)
    newBitmap.recycle()
    bitmapPhoto.bitmap.recycle()
}