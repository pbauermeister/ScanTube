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
    logger(context.getString(R.string.toast_found_label_fmt).format(label))
    updateState(State.EVALUATING, null)
    if (label == null) return

    // Playlist
    if (forAlbum) {
        val searchString = theConfig.albumSearchTemplate.format(label)
        val playlistId = YouTube.getSearchPlaylistFirstId(searchString)
        logger(context.getString(
                if (playlistId == null) R.string.toast_no_playlist_found
                else R.string.toast_playlist_found)
        )
        if (playlistId == null) return
        // TODO: playlist failed: search for just label, w/o full album

        val videoId = YouTube.getPlaylistItemsFirstVideoId(playlistId)
        logger(context.getString(
                if (videoId == null) R.string.toast_no_video_found
                else R.string.toast_video_found)
        )
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
        val searchString = theConfig.videoSearchTemplate.format(label)
        val videoId = YouTube.getSearchVideoFirstId(searchString)
        logger(context.getString(
                if (videoId == null) R.string.toast_no_video_found
                else R.string.toast_video_found)
        )
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
                  deviceRotation: Int,
                  logger: (String) -> Unit, updateState: (State, Bitmap?) -> Unit) {
    logger(activity.getString(R.string.toast_image_captured))

    val newBitmap = sizeBitmap(bitmapPhoto.bitmap, -bitmapPhoto.rotationDegrees, deviceRotation)
    val imageBase64 = encodeBitmapTobase64(newBitmap)

    updateState(State.QUERYING, newBitmap)
    queryImageAndStartYouTube(activity, imageBase64, forAlbum, logger, updateState)
    newBitmap.recycle()
    bitmapPhoto.bitmap.recycle()
}