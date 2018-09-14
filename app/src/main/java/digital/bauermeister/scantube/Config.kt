package digital.bauermeister.scantube

import android.preference.PreferenceManager
import android.util.Log

enum class CameraResolution { MINIMUM, MAXIMUM, DEFAULT }
enum class WhichCamera { FRONT, BACK, DEFAULT }

class Config {

    // GENERAL
    // =======

    // Shown buttons
    val showAlbumButton: Boolean get() = ((getPrefButtons() and 1) > 0)
    val showSongButton: Boolean get() = ((getPrefButtons() and 2) > 0)
    val showReplayButton: Boolean get() = true

    // To let YouTube settle before we restart our camera
    val delayBeforeReadyAgain: Int get() = 1000 * 1

    // Boot
    val startAtBoot: Boolean get() = getPrefAutoStart()

    // CAMERA
    // ======

    // To look good on screen. For very low-mem devices, use MINIMUM
    val cameraResolution: CameraResolution get() = CameraResolution.MAXIMUM

    // Camera
    val whichCamera: WhichCamera get() = if (getPrefCamera() == "front") WhichCamera.FRONT else WhichCamera.BACK
    val resetCameraBeforeShooting: Boolean get() = false
    val delayBeforeShooting: Int get() = 0

    // BW friendly, with still enough resolution for recognition
    val maxPhotoResolution: Int get() = 800

    // Shave out borders
    val marginFraction: Int get() = 8

    // Raise odds to have music contents
    val albumSearchTemplate: String get() = "%s and full album"
    val videoSearchTemplate: String get() = "%s and music"

    // APP KEYS
    // ========
    val googleVisionAppKey: String get() = getPrefGoogleVisionAppkey()!!
    val youTubeAppKey: String get() = getPrefYouTubeAppKey()!!
}

val prefs = PreferenceManager.getDefaultSharedPreferences(getAppContext())
val context = getAppContext()

fun getPrefButtons(): Int {
    return prefs.getString(context.getString(R.string.key_pref_buttons), "3").toInt()
}

fun getPrefAutoStart(): Boolean {
    return prefs.getBoolean(context.getString(R.string.key_pref_autostart), false)
}

fun getPrefGoogleVisionAppkey(): String? {
    return prefs.getString(context.getString(R.string.key_pref_googlevision_appkey), null)
}

fun getPrefYouTubeAppKey(): String? {
    return prefs.getString(context.getString(R.string.key_pref_youtube_appkey), null)
}

fun getPrefCamera(): String {
    return prefs.getString(context.getString(R.string.key_pref_camera), "front")
}

val theConfig = Config()
