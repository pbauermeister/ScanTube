package digital.bauermeister.scantube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import digital.bauermeister.scantube.googlevision.GoogleVision
import digital.bauermeister.scantube.youtube.YouTube

fun queryImageAndStartYouTube(context: Context, imageBase64: String) {
    val label = GoogleVision.getAnnotateWebDetectionFirstLabel(imageBase64)
    Log.i("MainLogic", "*** label: " + label)
    if (label == null) return

    val playlistId = YouTube.getSearchPlaylistFirstId(label + " and full album")
    Log.i("MainLogic", "*** playlistId: " + playlistId)
    if (playlistId == null) return
    // TODO: playlist failed: search for just label, w/o full album

    val videoId = YouTube.getPlaylistItemsFirstVideoId(playlistId)
    Log.i("MainLogic", "*** videoId: " + videoId)
    if (videoId == null) return
    // TODO: video of playlist failed: search for video with label, no playlist

    val url = "https://www.youtube.com/watch?v=$videoId&list=$playlistId"
    Log.i("MainLogic", "*** url: " + url)
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)

    context.startActivity(i)
}

var TheBitmap: Bitmap? = null

fun processBitmap(activity: Activity, bitmap: Bitmap) {
    val newBitmap = sizeBitmap(bitmap)
    val imageBase64 = encodeBitmapTobase64(newBitmap)

    if (false) {
        // display to image activity
        TheBitmap = newBitmap
        val intent = Intent(activity, ImageActivity::class.java)
        activity.startActivity(intent)
    } else {
        queryImageAndStartYouTube(activity, imageBase64)
        bitmap.recycle()
        newBitmap.recycle()
    }
}