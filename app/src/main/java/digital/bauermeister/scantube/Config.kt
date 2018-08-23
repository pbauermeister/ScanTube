package digital.bauermeister.scantube

enum class CameraResolution { MINIMUM, MAXIMUM, DEFAULT }
enum class WhichCamera { FRONT, BACK, DEFAULT }

data class Config(
        // To look good on screen. For very low-mem devices, use MINIMUM
        val cameraResolution: CameraResolution = CameraResolution.MAXIMUM,

        // Camera
        val whichCamera: WhichCamera = WhichCamera.FRONT,
        val resetCameraBeforeShooting: Boolean = false,
        val delayBeforeShooting: Int = 0,

        // BW friendly, with still enough resolution for recognition
        val maxPhotoResolution: Int = 800,

        // Shave out borders
        val marginFraction: Int = 8,

        // Raise odds to have music contents
        val albumSearchTemplate: String = "%s and full album",
        val videoSearchTemplate: String = "%s and music",

        // Shown buttons
        val showAlbumButton: Boolean = true,
        val showSongButton: Boolean = false,
        val showReplayButton: Boolean = true,

        // To let YouTube settle before we restart our camera
        val delayBeforeReadyAgain: Int = 1000 * 1
)

val theConfig = Config()
