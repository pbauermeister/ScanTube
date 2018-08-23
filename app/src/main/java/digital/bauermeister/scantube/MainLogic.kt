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

fun queryImageAndStartYouTube(context: Context, imageBase64: String, forAlbum: Boolean,
                              message: (String?, Boolean) -> Unit, updateState: (State, Bitmap?) -> Unit) {
    message(context.getString(R.string.message_query_google), true)
    val label = GoogleVision.getAnnotateWebDetectionFirstLabel(imageBase64)
    message(context.getString(R.string.toast_found_label_fmt).format(label), false)
    updateState(State.EVALUATING, null)
    if (label == null) return

    // Playlist
    if (forAlbum) {
        val searchString = theConfig.albumSearchTemplate.format(label)
        message(context.getString(R.string.message_query_youtube_pl), true)
        val playlistId = YouTube.getSearchPlaylistFirstId(searchString)
        message(context.getString(
                if (playlistId == null) R.string.toast_no_playlist_found
                else R.string.toast_playlist_found),
                false)
        if (playlistId == null) return
        // TODO: playlist failed: search for just label, w/o full album

        message("Querying Youtube for playlist items", true)
        message(context.getString(R.string.message_query_youtube_pl_info), true)
        val videoId = YouTube.getPlaylistItemsFirstVideoId(playlistId)
        message(context.getString(
                if (videoId == null) R.string.toast_first_video_not_found
                else R.string.toast_first_video_found),
                false)
        // TODO: video of playlist failed: search for video with label, no playlist

        message(null, true)
        youTubeUrl = "https://www.youtube.com/watch?v=$videoId&list=$playlistId"
        launchYouTube(context, youTubeUrl)
        updateState(State.READY, null)
    }

    // Video
    else {
        val searchString = theConfig.videoSearchTemplate.format(label)
        val videoId = YouTube.getSearchVideoFirstId(searchString)
        message(context.getString(
                if (videoId == null) R.string.toast_no_video_found
                else R.string.toast_video_found),
                false)
        if (videoId == null) return

        youTubeUrl = "https://www.youtube.com/watch?v=$videoId"
        launchYouTube(context, youTubeUrl)
        updateState(State.READY, null)
    }
}

fun launchYouTube(context: Context, url: String?) {
    if (url == null) return
    Log.i("MainLogic", "*** url: " + url)
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    context.startActivity(i)
}

fun processBitmap(activity: Activity, bitmapPhoto: BitmapPhoto, forAlbum: Boolean,
                  deviceRotation: Int,
                  message: (String?, Boolean) -> Unit,
                  updateState: (State, Bitmap?) -> Unit) {
    message(activity.getString(R.string.toast_image_captured), false)

    val newBitmap = sizeBitmap(bitmapPhoto.bitmap, -bitmapPhoto.rotationDegrees, deviceRotation)
    val imageBase64 = encodeBitmapTobase64(newBitmap)
    updateState(State.QUERYING, newBitmap)
    try {
        queryImageAndStartYouTube(activity, imageBase64, forAlbum, message, updateState)
    } catch (e: Throwable) {
        message(activity.getString(R.string.toast_error), false)
        message(activity.getString(R.string.toast_error_fmt).format(e.message), true)
        updateState(State.ERROR, null)
    }

    newBitmap.recycle()
    bitmapPhoto.bitmap.recycle()
}