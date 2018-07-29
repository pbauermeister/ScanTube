package digital.bauermeister.scantube

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.min


const val IMAGE_SIZE = 1600

// https://stackoverflow.com/a/34917657
fun encodeBitmapTobase64(image: Bitmap): String {
    val baos = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val bytes = baos.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

fun sizeBitmap(source: Bitmap, rotation: Int): Bitmap {
    val w = source.getWidth()
    val h = source.getHeight()

    val margin = min(w, h) / 8
    val land = w >= h
    val posX = if (land) w / 2 - h / 2 + margin else margin
    val posY = if (land) margin else h / 2 - w / 2 + margin
    val size = if (land) h - margin * 2 else w - margin * 2

    // crop
    val squared = Bitmap.createBitmap(source, posX, posY, size, size)
    source.recycle()

    // resize
    val sized = Bitmap.createScaledBitmap(squared, IMAGE_SIZE, IMAGE_SIZE, false)
    squared.recycle()

    // rotate
    val matrix = Matrix()
    matrix.postRotate(rotation.toFloat())
    val rotated = Bitmap.createBitmap(sized, 0, 0, sized.width, sized.height, matrix, true)
    sized.recycle()

    // TODO: first resize, then crop

    return rotated
}
