package digital.bauermeister.scantube

import android.content.Intent
import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import digital.bauermeister.scantube.googlevision.GoogleVision
import digital.bauermeister.scantube.youtube.YouTube
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("digital.bauermeister.scantube", appContext.packageName)
    }

    @Test
    fun accessGoogleVisionService() {
        val label = GoogleVision.getAnnotateWebDetectionFirstLabel(TEST_IMAGE_B64)
        assertNotNull("Label missing from response (see Logcat for HTTP body response)", label)
        assertEquals("Label value", "lenny kravitz 5", label)
        System.out.println(label)
    }

    @Test
    fun callYouTube() {
        val url = "https://www.youtube.com/results?search_query=lenny+kravitz+5+full+album"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)

        val appContext = InstrumentationRegistry.getTargetContext()
        appContext.startActivity(i)
    }

    @Test
    fun getPlayList() {
        val playListId = YouTube.getSearchPlaylistFirstId("lenny kravitz 5 and full album")
        assertNotNull(playListId)

        var playListFirstVideo: String? = null
        if (playListId != null) {
            playListFirstVideo = YouTube.getPlaylistItemsFirstVideoId(playListId)
        }

        System.out.println(playListId)
        System.out.println(playListFirstVideo)

    }

    @Test
    fun callYouTubePlayList() {
        val url = "https://www.youtube.com/watch?v=4GULTXv7o64&list=PLvVk0b3pDIKJckUuYpUBQL2K4BWjaHylb"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        InstrumentationRegistry.getTargetContext().startActivity(i)
    }

    @Test
    fun combo() {
        queryImageAndStartYouTube(InstrumentationRegistry.getTargetContext(), TEST_IMAGE_B64)
    }

}

// Base64-encoded jpeg image of a low-res (150x200), 90° right-rotated photo of
// the CD cover of album "5" by Lenny Kravitz:
val TEST_IMAGE_B64 = """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ht1JKM2ATjfTycnP0/nmqF825AO7AD8zR9cq9ylh6bewya8jK4EGCTj7x9qrGQzPuxjLKP0zUbtkj/gRH4k/4VLHjC/7zH8uKmpXnNWkzSFGEHeKL68j6k09OQtFFcpoSQjJqST+tFFPoS9xPSpQvymiihAxnU596ceB9KKKaBiA9B9aBxj6/wCNFFAhh6D/AD2qhe8SKPTn9KKKCo7lEAFoweyinE/KvqEY0UVTLP/Z/+EQfWh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8APD94cGFja2V0IGJlZ2luPSfvu78nIGlkPSdXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQnPz4KPHg6eG1wbWV0YSB4bWxuczp4PSdhZG9iZTpuczptZXRhLyc+CjxyZGY6UkRGIHhtbG5zOnJkZj0naHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyc+CgogPHJkZjpEZXNjcmlwdGlvbiB4bWxuczpleGlmPSdodHRwOi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyc+CiAgPGV4aWY6TWFrZT5zYW1zdW5nPC9leGlmOk1ha2U+CiAgPGV4aWY6TW9kZWw+U00tTjk1MEY8L2V4aWY6TW9kZWw+CiAgPGV4aWY6T3JpZW50YXRpb24+UmlnaHQtdG9wPC9leGlmOk9yaWVudGF0aW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjcyPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjcyPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6U29mdHdhcmU+Tjk1MEZYWFUzQ1JGNDwvZXhpZjpTb2Z0d2FyZT4KICA8ZXhpZjpEYXRlVGltZT4yMDE4OjA3OjE2IDE4OjA2OjE0PC9leGlmOkRhdGVUaW1lPgogIDxleGlmOllDYkNyUG9zaXRpb25pbmc+Q2VudGVyZWQ8L2V4aWY6WUNiQ3JQb3NpdGlvbmluZz4KICA8ZXhpZjpJbWFnZVdpZHRoPjQ5NjwvZXhpZjpJbWFnZVdpZHRoPgogIDxleGlmOkltYWdlTGVuZ3RoPjI0MDwvZXhpZjpJbWFnZUxlbmd0aD4KICA8ZXhpZjpDb21wcmVzc2lvbj5KUEVHIGNvbXByZXNzaW9uPC9leGlmOkNvbXByZXNzaW9uPgogIDxleGlmOk9yaWVudGF0aW9uPlJpZ2h0LXRvcDwvZXhpZjpPcmllbnRhdGlvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOk1ha2U+c2Ftc3VuZzwvZXhpZjpNYWtlPgogIDxleGlmOk1vZGVsPlNNLU45NTBGPC9leGlmOk1vZGVsPgogIDxleGlmOk9yaWVudGF0aW9uPlRvcC1sZWZ0PC9leGlmOk9yaWVudGF0aW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjcyPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjcyPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6U29mdHdhcmU+R0lNUCAyLjguMTY8L2V4aWY6U29mdHdhcmU+CiAgPGV4aWY6RGF0ZVRpbWU+MjAxODowNzoyMSAxMDowNzoyMTwvZXhpZjpEYXRlVGltZT4KICA8ZXhpZjpZQ2JDclBvc2l0aW9uaW5nPkNlbnRlcmVkPC9leGlmOllDYkNyUG9zaXRpb25pbmc+CiAgPGV4aWY6SW1hZ2VXaWR0aD40OTY8L2V4aWY6SW1hZ2VXaWR0aD4KICA8ZXhpZjpJbWFnZUxlbmd0aD4yNDA8L2V4aWY6SW1hZ2VMZW5ndGg+CiAgPGV4aWY6Q29tcHJlc3Npb24+SlBFRyBjb21wcmVzc2lvbjwvZXhpZjpDb21wcmVzc2lvbj4KICA8ZXhpZjpPcmllbnRhdGlvbj5SaWdodC10b3A8L2V4aWY6T3JpZW50YXRpb24+CiAgPGV4aWY6WFJlc29sdXRpb24+NzI8L2V4aWY6WFJlc29sdXRpb24+CiAgPGV4aWY6WVJlc29sdXRpb24+NzI8L2V4aWY6WVJlc29sdXRpb24+CiAgPGV4aWY6UmVzb2x1dGlvblVuaXQ+SW5jaDwvZXhpZjpSZXNvbHV0aW9uVW5pdD4KICA8ZXhpZjpFeHBvc3VyZVRpbWU+MS8xMCBzZWMuPC9leGlmOkV4cG9zdXJlVGltZT4KICA8ZXhpZjpGTnVtYmVyPmYvMS43PC9leGlmOkZOdW1iZXI+CiAgPGV4aWY6RXhwb3N1cmVQcm9ncmFtPk5vcm1hbCBwcm9ncmFtPC9leGlmOkV4cG9zdXJlUHJvZ3JhbT4KICA8ZXhpZjpJU09TcGVlZFJhdGluZ3M+CiAgIDxyZGY6U2VxPgogICAgPHJkZjpsaT4yMDA8L3JkZjpsaT4KICAgPC9yZGY6U2VxPgogIDwvZXhpZjpJU09TcGVlZFJhdGluZ3M+CiAgPGV4aWY6RXhpZlZlcnNpb24+RXhpZiBWZXJzaW9uIDIuMjwvZXhpZjpFeGlmVmVyc2lvbj4KICA8ZXhpZjpEYXRlVGltZU9yaWdpbmFsPjIwMTg6MDc6MTYgMTg6MDY6MTQ8L2V4aWY6RGF0ZVRpbWVPcmlnaW5hbD4KICA8ZXhpZjpEYXRlVGltZURpZ2l0aXplZD4yMDE4OjA3OjE2IDE4OjA2OjE0PC9leGlmOkRhdGVUaW1lRGlnaXRpemVkPgogIDxleGlmOkNvbXBvbmVudHNDb25maWd1cmF0aW9uPgogICA8cmRmOlNlcT4KICAgIDxyZGY6bGk+WSBDYiBDciAtPC9yZGY6bGk+CiAgIDwvcmRmOlNlcT4KICA8L2V4aWY6Q29tcG9uZW50c0NvbmZpZ3VyYXRpb24+CiAgPGV4aWY6U2h1dHRlclNwZWVkVmFsdWU+My4zMiBFViAoMS85IHNlYy4pPC9leGlmOlNodXR0ZXJTcGVlZFZhbHVlPgogIDxleGlmOkFwZXJ0dXJlVmFsdWU+MS41MyBFViAoZi8xLjcpPC9leGlmOkFwZXJ0dXJlVmFsdWU+CiAgPGV4aWY6QnJpZ2h0bmVzc1ZhbHVlPi0xLjI4IEVWICgxLjQxIGNkL21eMik8L2V4aWY6QnJpZ2h0bmVzc1ZhbHVlPgogIDxleGlmOkV4cG9zdXJlQmlhc1ZhbHVlPjAuMDAgRVY8L2V4aWY6RXhwb3N1cmVCaWFzVmFsdWU+CiAgPGV4aWY6TWF4QXBlcnR1cmVWYWx1ZT4xLjUzIEVWIChmLzEuNyk8L2V4aWY6TWF4QXBlcnR1cmVWYWx1ZT4KICA8ZXhpZjpNZXRlcmluZ01vZGU+Q2VudGVyLXdlaWdodGVkIGF2ZXJhZ2U8L2V4aWY6TWV0ZXJpbmdNb2RlPgogIDxleGlmOkZsYXNoIHJkZjpwYXJzZVR5cGU9J1Jlc291cmNlJz4KICA8L2V4aWY6Rmxhc2g+CiAgPGV4aWY6Rm9jYWxMZW5ndGg+NC4zIG1tPC9leGlmOkZvY2FsTGVuZ3RoPgogIDxleGlmOk1ha2VyTm90ZT45OCBieXRlcyB1bmRlZmluZWQgZGF0YTwvZXhpZjpNYWtlck5vdGU+CiAgPGV4aWY6VXNlckNvbW1lbnQgLz4KICA8ZXhpZjpTdWJzZWNUaW1lPjA5ODA8L2V4aWY6U3Vic2VjVGltZT4KICA8ZXhpZjpTdWJTZWNUaW1lT3JpZ2luYWw+MDk4MDwvZXhpZjpTdWJTZWNUaW1lT3JpZ2luYWw+CiAgPGV4aWY6U3ViU2VjVGltZURpZ2l0aXplZD4wOTgwPC9leGlmOlN1YlNlY1RpbWVEaWdpdGl6ZWQ+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpDb2xvclNwYWNlPnNSR0I8L2V4aWY6Q29sb3JTcGFjZT4KICA8ZXhpZjpQaXhlbFhEaW1lbnNpb24+MTAyNDwvZXhpZjpQaXhlbFhEaW1lbnNpb24+CiAgPGV4aWY6UGl4ZWxZRGltZW5zaW9uPjEzNjU8L2V4aWY6UGl4ZWxZRGltZW5zaW9uPgogIDxleGlmOkV4cG9zdXJlTW9kZT5BdXRvIGV4cG9zdXJlPC9leGlmOkV4cG9zdXJlTW9kZT4KICA8ZXhpZjpXaGl0ZUJhbGFuY2U+QXV0byB3aGl0ZSBiYWxhbmNlPC9leGlmOldoaXRlQmFsYW5jZT4KICA8ZXhpZjpGb2NhbExlbmd0aEluMzVtbUZpbG0+MjY8L2V4aWY6Rm9jYWxMZW5ndGhJbjM1bW1GaWxtPgogIDxleGlmOlNjZW5lQ2FwdHVyZVR5cGU+U3RhbmRhcmQ8L2V4aWY6U2NlbmVDYXB0dXJlVHlwZT4KICA8ZXhpZjpJbWFnZVVuaXF1ZUlEPkcxMkxMS0EwMVZNIEcxMkxMS0wwMUdNCjwvZXhpZjpJbWFnZVVuaXF1ZUlEPgogIDxleGlmOkdQU1ZlcnNpb25JRD4yLjIuMC4wPC9leGlmOkdQU1ZlcnNpb25JRD4KICA8ZXhpZjpJbnRlcm9wZXJhYmlsaXR5SW5kZXg+TjwvZXhpZjpJbnRlcm9wZXJhYmlsaXR5SW5kZXg+CiAgPGV4aWY6SW50ZXJvcGVyYWJpbGl0eVZlcnNpb24+NDYsIDMyLCAgNDwvZXhpZjpJbnRlcm9wZXJhYmlsaXR5VmVyc2lvbj4KICA8ZXhpZjpHUFNMb25naXR1ZGVSZWY+RTwvZXhpZjpHUFNMb25naXR1ZGVSZWY+CiAgPGV4aWY6R1BTTG9uZ2l0dWRlPiA2LCA0MywgMjU8L2V4aWY6R1BTTG9uZ2l0dWRlPgogIDxleGlmOkdQU0FsdGl0dWRlUmVmPlNlYSBsZXZlbDwvZXhpZjpHUFNBbHRpdHVkZVJlZj4KICA8ZXhpZjpHUFNBbHRpdHVkZT44NzU8L2V4aWY6R1BTQWx0aXR1ZGU+CiAgPGV4aWY6R1BTVGltZVN0YW1wPjE2OjA2OjEyLjAwPC9leGlmOkdQU1RpbWVTdGFtcD4KICA8ZXhpZjpHUFNEYXRlU3RhbXA+MjAxODowNzoxNjwvZXhpZjpHUFNEYXRlU3RhbXA+CiAgPGV4aWY6SW50ZXJvcGVyYWJpbGl0eUluZGV4PlI5ODwvZXhpZjpJbnRlcm9wZXJhYmlsaXR5SW5kZXg+CiAgPGV4aWY6SW50ZXJvcGVyYWJpbGl0eVZlcnNpb24+MDEwMDwvZXhpZjpJbnRlcm9wZXJhYmlsaXR5VmVyc2lvbj4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KPD94cGFja2V0IGVuZD0ncic/Pgr/2wBDAAEBAQEBAQEBAQEBAQECAgMCAgICAgQDAwIDBQQFBQUEBAQFBgcGBQUHBgQEBgkGBwgICAgIBQYJCgkICgcICAj/2wBDAQEBAQICAgQCAgQIBQQFCAgICAgICAgICAgI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"""