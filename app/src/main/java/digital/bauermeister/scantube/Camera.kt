package digital.bauermeister.scantube

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.result.BitmapPhoto
import io.fotoapparat.selector.back
import io.fotoapparat.selector.front
import io.fotoapparat.selector.highestResolution
import io.fotoapparat.selector.lowestResolution
import io.fotoapparat.view.CameraRenderer

fun getLensPosition() = when (theConfig.whichCamera) {
    WhichCamera.FRONT -> front()
    WhichCamera.BACK -> back()
    else -> front()
}

fun getCameraConfiguration() = when (theConfig.cameraResolution) {
    CameraResolution.MAXIMUM -> CameraConfiguration(pictureResolution = highestResolution())
    CameraResolution.MINIMUM -> CameraConfiguration(pictureResolution = lowestResolution())
    else -> CameraConfiguration()
}

class Camera(private val activity: Activity,
             private val cameraView: CameraRenderer,
             private val permissionRequest: Int) {

    fun restart() {
        stop()
        start()
    }

    fun stop() {
        stopFotoapparat()
    }

    fun start() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.CAMERA)) {
                // TODO: explain in a dialog, then call start()
                start()
            } else {
                // No explanation needed, we can request the permission.
                Log.d(this.javaClass.name, "CAMERA: requesting perm")
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.CAMERA),
                        permissionRequest)
            }
        } else {
            // Permission has already been granted
            Log.d(this.javaClass.name, "CAMERA: perm already granted")
            startFotoapparat()
        }
    }

    fun takePicture(callback: (BitmapPhoto?) -> Unit) {
        if (fotoapparat == null) {
            callback(null)
        } else {
            val photoResult = fotoapparat!!.takePicture()
            // Asynchronously converts photo to bitmap and returns the result
            photoResult
                    .toBitmap()
                    .whenAvailable { callback(it) }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>,
                                   grantResults: IntArray) {
        when (requestCode) {
            permissionRequest -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d(this.javaClass.name, "CAMERA: perm newly granted")
                    startFotoapparat()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d(this.javaClass.name, "CAMERA: perm NOT granted")
                    // TODO: shout and freeze app
                }
                return
            }
        }
    }

    private var fotoapparat: Fotoapparat? = null

    private fun startFotoapparat() {
        stopFotoapparat()
        Thread.sleep(100)
        fotoapparat = Fotoapparat(
                context = activity,
                view = cameraView,
                scaleType = ScaleType.CenterCrop,
                lensPosition = getLensPosition(),
                cameraConfiguration = getCameraConfiguration(),
                logger = loggers(logcat()),
                cameraErrorCallback = { error ->
                    Log.e(this.javaClass.name, "Camera error: " + error)
                }
        )
        fotoapparat!!.start()
        Thread.sleep(100)
    }

    private fun stopFotoapparat() {
        fotoapparat?.stop()
        fotoapparat = null
    }
}
