package digital.bauermeister.scantube

import android.content.Context
import android.content.Intent
import android.net.Uri
import digital.bauermeister.scantube.googlevision.GoogleVision
import digital.bauermeister.scantube.youtube.YouTube

fun queryImageAndStartYouTube(context: Context, imageBase64: String) {
    val label = GoogleVision.getAnnotateWebDetectionFirstLabel(imageBase64)
    if (label == null) return

    val playlistId = YouTube.getSearchPlaylistFirstId(label + " and full album")
    if (playlistId == null) return
    // TODO: playlist failed: search for just label, w/o full album

    val videoId = YouTube.getPlaylistItemsFirstVideoId(playlistId)
    if (videoId == null) return
    // TODO: video of playlist failed: search for video with label, no playlist

    val url = "https://www.youtube.com/watch?v=$videoId&list=$playlistId"
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)

    context.startActivity(i)
}