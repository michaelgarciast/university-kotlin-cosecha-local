package com.garcia.cosechalocal.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.LruCache
import android.widget.ImageView
import androidx.exifinterface.media.ExifInterface
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.Product
import com.garcia.cosechalocal.data.repository.ProductImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Las fotos de producto se guardan como BLOB en SQLite/Room, solo en el
// dispositivo donde se cargaron: Firebase Storage exige plan de facturación.
object ProductImageStore {

    const val MAX_DIMENSION = 1024
    const val THUMB_DIMENSION = 512
    private const val JPEG_QUALITY = 82

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var repository: ProductImageRepository? = null

    private val cache = object : LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    ) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    private fun repository(context: Context): ProductImageRepository =
        repository ?: ProductImageRepository(context.applicationContext).also { repository = it }

    // Lee la imagen apuntada por la URI, la escala a un máximo de MAX_DIMENSION
    // px por lado, respeta la orientación EXIF y la devuelve como JPEG.
    fun compress(context: Context, uri: Uri): ByteArray? {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) > MAX_DIMENSION) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: return null

        val orientation = resolver.openInputStream(uri)?.use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
        val rotated = decoded.rotate(orientation)

        val scale = min(1f, MAX_DIMENSION.toFloat() / max(rotated.width, rotated.height))
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                rotated,
                (rotated.width * scale).roundToInt(),
                (rotated.height * scale).roundToInt(),
                true
            )
        } else {
            rotated
        }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        return output.toByteArray()
    }

    // Decodifica con inSampleSize para no cargar en memoria más píxeles de los
    // que se van a mostrar (las listas usan THUMB_DIMENSION).
    fun decode(imageData: ByteArray, maxDimension: Int = MAX_DIMENSION): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size, bounds)
        var sampleSize = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
    }

    fun bind(imageView: ImageView, product: Product, maxDimension: Int = THUMB_DIMENSION) =
        bind(imageView, product.id, product.hasLocalImage, maxDimension)

    // Pinta la foto local del producto en el ImageView. Si el producto no tiene
    // imagen o el BLOB no existe en este dispositivo, muestra el placeholder.
    fun bind(imageView: ImageView, productId: String, hasImage: Boolean, maxDimension: Int = THUMB_DIMENSION) {
        imageView.setTag(R.id.tag_product_image_id, productId)
        if (!hasImage) {
            showPlaceholder(imageView)
            return
        }
        val key = "$productId@$maxDimension"
        cache.get(key)?.let {
            showBitmap(imageView, it)
            return
        }
        showPlaceholder(imageView)
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                runCatching { repository(imageView.context).getImage(productId) }
                    .getOrNull()
                    ?.let { decode(it.imageData, maxDimension) }
            }
            if (bitmap != null) cache.put(key, bitmap)
            if (imageView.getTag(R.id.tag_product_image_id) == productId) {
                if (bitmap != null) showBitmap(imageView, bitmap) else showPlaceholder(imageView)
            }
        }
    }

    fun invalidate(productId: String) {
        cache.snapshot().keys.filter { it.startsWith("$productId@") }.forEach(cache::remove)
    }

    fun clearCache() = cache.evictAll()

    fun showBitmap(imageView: ImageView, bitmap: Bitmap) {
        rememberPadding(imageView)
        imageView.setPadding(0, 0, 0, 0)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageBitmap(bitmap)
    }

    fun showPlaceholder(imageView: ImageView) {
        restorePadding(imageView)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageResource(R.drawable.ic_product_placeholder)
    }

    private fun rememberPadding(imageView: ImageView) {
        if (imageView.getTag(R.id.tag_product_image_padding) == null) {
            imageView.setTag(
                R.id.tag_product_image_padding,
                intArrayOf(
                    imageView.paddingLeft,
                    imageView.paddingTop,
                    imageView.paddingRight,
                    imageView.paddingBottom
                )
            )
        }
    }

    private fun restorePadding(imageView: ImageView) {
        (imageView.getTag(R.id.tag_product_image_padding) as? IntArray)?.let { p ->
            imageView.setPadding(p[0], p[1], p[2], p[3])
        }
    }

    private fun Bitmap.rotate(orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return this
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}
