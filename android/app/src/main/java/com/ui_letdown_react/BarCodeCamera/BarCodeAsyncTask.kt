package com.ui_letdown_react.BarCodeCamera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class BarCodeAsyncTask(
        private val _delegate: BarCodeAsyncTaskDelegate,
        private val _byteData: ByteArray,
        private val _height: Int,
        private val _width: Int,
        private val _barcodeReader: MultiFormatReader

): AsyncTask<Void, Void, String>() {

    override fun onPreExecute() {
        super.onPreExecute()

        //TODO: Weak Reference delegate
        _delegate.onPreBarCodeRead()
    }

    override fun doInBackground(vararg params: Void?): String? {

        var result: Result? = null

        try {
            val bitmap = generateBitmapFromImageData(
                    _byteData,
                    _width,
                    _height,
                    false
            )
            result = _barcodeReader.decodeWithState(bitmap)
        } catch (e: NotFoundException) {
            val bitmap = generateBitmapFromImageData(
                    rotateImage(_byteData, _width, _height),
                    _height,
                    _width,
                    false
            )
            try {
                result = _barcodeReader.decodeWithState(bitmap)
            } catch (e1: NotFoundException) {
                val invertedBitmap = generateBitmapFromImageData(
                        _byteData,
                        _width,
                        _height,
                        true
                )
                try {
                    result = _barcodeReader.decodeWithState(invertedBitmap)
                } catch (e2: NotFoundException) {
                    val invertedRotatedBitmap = generateBitmapFromImageData(
                            rotateImage(_byteData, _width, _height),
                            _height,
                            _width,
                            true
                    )
                    try {
                        result = _barcodeReader.decodeWithState(invertedRotatedBitmap)
                    } catch (e3: NotFoundException) {
                        //no barcode Found
                    }
                }
            }

        } catch (t: Throwable) {
            t.printStackTrace()
        }

        if (result == null) {
            // Log.e("@@", "NULL")
            return null
        }

        // Log.e("@@", result.text)

        return result.text
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        _delegate.onBarCodeRead(result)
    }

    private fun rotateImage(imageData: ByteArray, width: Int, height: Int): ByteArray {
        val rotated = ByteArray(imageData.size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                rotated[x * height + height - y - 1] = imageData[x + y * width]
            }
        }
        return rotated
    }

    private fun generateBitmapFromImageData(imageData: ByteArray, width: Int, height: Int, inverse: Boolean): BinaryBitmap {
        val source = PlanarYUVLuminanceSource(
                imageData, // byte[] yuvData
                width, // int dataWidth
                height, // int dataHeight
                0, // int left
                0, // int top
                width, // int width
                height, // int height
                false // boolean reverseHorizontal
        )
        return if (inverse) {
            BinaryBitmap(HybridBinarizer(source.invert()))
        } else {
            BinaryBitmap(HybridBinarizer(source))
        }
    }

}

interface BarCodeAsyncTaskDelegate {
    fun onBarCodeRead(result: String?)
    fun onPreBarCodeRead()
}