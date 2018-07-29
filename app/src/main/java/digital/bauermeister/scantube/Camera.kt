package digital.bauermeister.scantube

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import io.fotoapparat.Fotoapparat
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.front
import io.fotoapparat.view.CameraRenderer

class Camera(val activity: Activity,
             val cameraView: CameraRenderer,
             val permissionRequest: Int) {
    val fotoapparat = Fotoapparat(
            context = activity,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = front(),
            //cameraConfiguration = configuration,
            logger = loggers(logcat()),
            cameraErrorCallback = { error ->
                Log.e(this.javaClass.name, "Camera error: " + error)
            }
    )

    fun stop() {
        fotoapparat.stop()
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
            fotoapparat?.start()
        }
    }

    fun takePicture(callback: (Bitmap) -> Unit) {
        val photoResult = fotoapparat.takePicture()

        // Asynchronously converts photo to bitmap and returns the result on the main thread
        photoResult
                .toBitmap()
                .whenAvailable { bitmapPhoto -> callback(bitmapPhoto!!.bitmap) }
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
                    fotoapparat?.start()
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
}