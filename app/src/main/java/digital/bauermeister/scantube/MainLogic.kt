package digital.bauermeister.scantube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import digital.bauermeister.scantube.googlevision.GoogleVision
import digital.bauermeister.scantube.youtube.YouTube

fun queryImageAndStartYouTube(context: Context, imageBase64: String, logger: (String) -> Unit) {
    val label = GoogleVision.getAnnotateWebDetectionFirstLabel(imageBase64)
    logger("Found label: " + label)
    if (label == null) return

    val playlistId = YouTube.getSearchPlaylistFirstId(label + " and full album")
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

    context.startActivity(i)
}

var TheBitmap: Bitmap? = null

fun processBitmap(activity: Activity, bitmap: Bitmap, logger: (String) -> Unit) {
    logger("Image captured")

    val newBitmap = sizeBitmap(bitmap)
    val imageBase64 = encodeBitmapTobase64(newBitmap)

    if (false) {
        // display to image activity
        TheBitmap = newBitmap
        val intent = Intent(activity, ImageActivity::class.java)
        activity.startActivity(intent)
    } else {
        queryImageAndStartYouTube(activity, imageBase64, logger)
        bitmap.recycle()
        newBitmap.recycle()
    }
}