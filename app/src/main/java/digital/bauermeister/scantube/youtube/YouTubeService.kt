package digital.bauermeister.scantube.youtube

import digital.bauermeister.scantube.Callbacks
import digital.bauermeister.scantube.makeWebServiceClient
import digital.bauermeister.scantube.theConfig
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeService {
    @GET("search")
    fun searchPlaylist(
            @Query("q") q: String,
            @Query("type") type: String = "playlist",
            @Query("part") part: String = "id",
            @Query("key") key: String = theConfig.youTubeAppKey!!)
            : Observable<YouTubeSearchPlaylistResponse.Data>

    @GET("search")
    fun searchVideo(
            @Query("q") q: String,
            @Query("type") type: String = "video",
            @Query("part") part: String = "id",
            @Query("key") key: String = theConfig.youTubeAppKey!!)
            : Observable<YouTubeVideoItemsResponse.Data>

    @GET("playlistItems")
    fun getPlaylistItems(
            @Query("playlistId") playlistId: String,
            @Query("part") part: String = "contentDetails",
            @Query("key") key: String = theConfig.youTubeAppKey!!)
            : Observable<YouTubePlaylistItemsResponse.Data>

    companion object {
        fun create(): YouTubeService {
            return makeWebServiceClient("https://www.googleapis.com/youtube/v3/",
                    YouTubeService::class.java)
        }
    }
}

object YouTube {
    private val youTubeServe by lazy {
        YouTubeService.create()
    }

    /**
     * Asynchronously search for playlist
     */
    private fun searchPlayList(q: String,
                               onSuccess: (YouTubeSearchPlaylistResponse.Data) -> Unit,
                               onError: (Throwable) -> Unit) {
        youTubeServe.searchPlaylist(q)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError)
    }

    /**
     * Synchronously search for playlist, returning id of first match
     */
    fun getSearchPlaylistFirstId(q: String): String? {
        val callbacks = Callbacks<YouTubeSearchPlaylistResponse.Data>()
        searchPlayList(q, callbacks.onSuccess, callbacks.onError)
        callbacks.await()
        return callbacks.success?.items?.getOrNull(0)?.id?.playlistId
    }

    /**
     * Asynchronously get videos of a playlist
     */
    private fun getPlaylistItems(playlistId: String,
                                 onSuccess: (YouTubePlaylistItemsResponse.Data) -> Unit,
                                 onError: (Throwable) -> Unit) {
        youTubeServe.getPlaylistItems(playlistId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError)
    }

    /**
     * Synchronously get first video of a playlist
     */
    fun getPlaylistItemsFirstVideoId(playlistId: String): String? {
        val callbacks = Callbacks<YouTubePlaylistItemsResponse.Data>()
        getPlaylistItems(playlistId, callbacks.onSuccess, callbacks.onError)
        callbacks.await()
        return callbacks.success?.items?.getOrNull(0)?.contentDetails?.videoId
    }

    /**
     * Asynchronously search for video
     */
    private fun searchVideo(q: String,
                            onSuccess: (YouTubeVideoItemsResponse.Data) -> Unit,
                            onError: (Throwable) -> Unit) {
        youTubeServe.searchVideo(q)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError)
    }

    /**
     * Synchronously search for video, returning id of first match
     */
    fun getSearchVideoFirstId(q: String): String? {
        val callbacks = Callbacks<YouTubeVideoItemsResponse.Data>()
        searchVideo(q, callbacks.onSuccess, callbacks.onError)
        callbacks.await()
        return callbacks.success?.items?.getOrNull(0)?.id?.videoId
    }

}