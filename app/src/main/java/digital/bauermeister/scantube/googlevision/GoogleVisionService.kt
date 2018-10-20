package digital.bauermeister.scantube.googlevision

import digital.bauermeister.scantube.Callbacks
import digital.bauermeister.scantube.makeWebServiceClient
import digital.bauermeister.scantube.theConfig
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GoogleVisionService {
    @POST("https://vision.googleapis.com/v1/images:annotate")
    fun annotateWebDetection(@Body body: GoogleVisionRequest.Data,
                             @Query("key") key: String = theConfig.googleVisionAppKey!!)
            : Observable<GoogleVisionWebDetectionResponse.Data>

    companion object {
        fun create(): GoogleVisionService {
            return makeWebServiceClient("https://vision.googleapis.com/v1/",
                    GoogleVisionService::class.java)
        }
    }
}

object GoogleVision {
    private val googleVisionServe by lazy {
        GoogleVisionService.create()
    }

    /**
     * Asynchronously get annotations of an image
     */
    private fun annotateWebDetection(imageBase64: String,
                             onSuccess: (GoogleVisionWebDetectionResponse.Data) -> Unit,
                             onError: (Throwable) -> Unit) {
        googleVisionServe.annotateWebDetection(makeGoogleVisionWebDetectionRequest(imageBase64))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError)
    }

    /**
     * Synchronously get first label of an image
     */
    fun getAnnotateWebDetectionFirstLabel(imageBase64: String): String? {
        val callbacks = Callbacks<GoogleVisionWebDetectionResponse.Data>()
        annotateWebDetection(imageBase64, callbacks.onSuccess, callbacks.onError)
        callbacks.await()
        return callbacks.success
                ?.responses
                ?.getOrNull(0)
                ?.webDetection
                ?.bestGuessLabels
                ?.getOrNull(0)
                ?.label
    }
}