package digital.bauermeister.scantube

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.math.min


val IMAGE_SIZE = theConfig.maxPhotoResolution

// https://stackoverflow.com/a/34917657
fun encodeBitmapTobase64(image: Bitmap): String {
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

private fun bitmapOp(cond: Boolean, src: Bitmap, op: () -> Bitmap): Bitmap {
    if (!cond) {
        Log.d(TAG, formatSize("> Nothing to do, returning input bitmap. ", src))
        return src
    }
    try {
        val res = op()
        src.recycle()
        Log.d(TAG, formatSize("> OK, created new bitmap and recycled input bitmap. ", res))
        return res
    } catch (e: OutOfMemoryError) {
        Log.d(TAG, formatSize("> Memory error, returning input bitmap. ", src))
        return src
    }
}

private fun formatSize(prefix: String, bm: Bitmap): String = prefix + bm.width + "x" + bm.height

fun sizeBitmap(source: Bitmap, imageRotation: Int, deviceRotation: Int): Bitmap {
    val w = source.width
    val h = source.height

    val margin = min(w, h) / theConfig.marginFraction
    val land = w >= h
    val posX = if (land) w / 2 - h / 2 + margin else margin
    val posY = if (land) margin else h / 2 - w / 2 + margin
    val size = if (land) h - margin * 2 else w - margin * 2

    Log.i(TAG, formatSize("Start image size: ", source) + ". Target square size: " + IMAGE_SIZE)

    // crop
    Log.d(TAG, "Cropping...")
    val squared = bitmapOp(/*size > IMAGE_SIZE*/ true, source,
            { Bitmap.createBitmap(source, posX, posY, size, size) })

    // resize
    Log.d(TAG, "Resizing...")
    val sized = bitmapOp(size > IMAGE_SIZE, squared,
            { Bitmap.createScaledBitmap(squared, IMAGE_SIZE, IMAGE_SIZE, false) })

    // rotate
    val rotation = (imageRotation + deviceRotation) % 360
    Log.d(TAG, "Rotating... img:$imageRotation dev:$deviceRotation ->$rotation")
    val matrix = Matrix()
    matrix.postRotate(rotation.toFloat())
    val rotated = bitmapOp(rotation != 0, sized,
            { Bitmap.createBitmap(sized, 0, 0, sized.width, sized.height, matrix, true) })

    Log.d(TAG, formatSize("Done. End image size: ", rotated))
    return rotated
}

const val TAG = "ImageProcessing"
