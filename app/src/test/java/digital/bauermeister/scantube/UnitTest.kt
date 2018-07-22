package digital.bauermeister.scantube

import com.google.gson.Gson
import digital.bauermeister.scantube.googlevision.makeGoogleVisionWebDetectionRequest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTest {
    @Test
    fun googleVisionRequestModel_isCorrect() {
        val data = makeGoogleVisionWebDetectionRequest("--img--")
        val gson = Gson()
        val json = gson.toJson(data)
        System.out.println(json)
        assertEquals(
                "JSONized request: " + json,
                """{"requests":[{"image":{"content":"--img--"},"features":[{"type":"WEB_DETECTION","maxResults":5}]}]}""",
                json)
    }
}
