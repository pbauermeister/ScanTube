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

var youTubeUrl: String? = null

fun processBitmap(activity: Activity, bitmapPhoto: BitmapPhoto, forAlbum: Boolean,
                  deviceRotation: Int,
                  message: (String?, Boolean) -> Unit,
                  updateState: (State, Bitmap?) -> Unit) {
    val newBitmap = sizeBitmap(bitmapPhoto.bitmap, -bitmapPhoto.rotationDegrees, deviceRotation)
    val imageBase64 = encodeBitmapTobase64(newBitmap)

    updateState(State.QUERYING, newBitmap)
    var errorMessage: String? = null
    try {
        errorMessage = queryImageAndStartYouTube(activity, imageBase64, forAlbum, message, updateState)
    } catch (e: Throwable) {
        message(activity.getString(R.string.message_error), false)
        message(activity.getString(R.string.message_error_fmt).format(e.message), true)
        updateState(State.ERROR, null)
    }
    if (errorMessage != null) {
        message(errorMessage, true)
        updateState(State.ERROR, null)
    }

    newBitmap.recycle()
    bitmapPhoto.bitmap.recycle()
}

private fun queryImageAndStartYouTube(context: Context, imageBase64: String, forAlbum: Boolean,
                                      message: (String?, Boolean) -> Unit,
                                      updateState: (State, Bitmap?) -> Unit): String? {
    // Image recognition on Google
    message(context.getString(R.string.message_query_google), true)
    val label = GoogleVision.getAnnotateWebDetectionFirstLabel(imageBase64)
    if (label == null)
        return context.getString(R.string.message_label_not_found)
    else
        message(context.getString(R.string.message_found_label_fmt).format(label), false)
    updateState(State.EVALUATING, null)

    // Playlist search on YouTube
    if (forAlbum) {
        val searchString = theConfig.albumSearchTemplate.format(label)
        message(context.getString(R.string.message_query_youtube_pl), true)
        val playlistId = YouTube.getSearchPlaylistFirstId(searchString)
        if (playlistId == null)
            return context.getString(R.string.message_playlist_not_found)
        // TODO: playlist failed: search for just label, w/o full album
        else
            message(context.getString(R.string.message_playlist_found), false)

        message("Querying Youtube for playlist items", true)
        message(context.getString(R.string.message_query_youtube_pl_info), true)
        val videoId = YouTube.getPlaylistItemsFirstVideoId(playlistId)
        // TODO: video of playlist failed: search for video with label, no playlist
        if (videoId == null)
            return context.getString(R.string.message_video_not_found)
        else
            message(context.getString(R.string.message_first_video_found), false)

        message(null, true)
        youTubeUrl = "https://www.youtube.com/watch?v=$videoId&list=$playlistId"
        launchYouTube(context, youTubeUrl)
        updateState(State.READY, null)
    }

    // Video search on YouTube
    else {
        val searchString = theConfig.videoSearchTemplate.format(label)
        val videoId = YouTube.getSearchVideoFirstId(searchString)
        if (videoId == null)
            return context.getString(R.string.message_video_not_found)
        else
            message(context.getString(R.string.message_video_found), false)

        youTubeUrl = "https://www.youtube.com/watch?v=$videoId"
        launchYouTube(context, youTubeUrl)
        updateState(State.READY, null)
    }
    return null
}

fun launchYouTube(context: Context, url: String?) {
    if (url == null) return
    Log.i("MainLogic", "*** url: " + url)
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    context.startActivity(i)
}