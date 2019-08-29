package com.ui_letdown_react.BarCodeCamera

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.AsyncTask
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.io.File
import java.io.FileOutputStream

class BarCodeRGBAsyncTask(
        private val _rawCropRect: Rect,
        private val _delegate: BarCodeAsyncTaskDelegate,
        private val _rawBmp: Bitmap,
        private val _barcodeReader: MultiFormatReader

) : AsyncTask<Void, Void, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
        _delegate.onPreBarCodeRead()
    }

    override fun doInBackground(vararg params: Void?): String? {

        var result: Result? = null

        try {
            result = _barcodeReader.decodeWithState(
                    BinaryBitmap(
                            HybridBinarizer(
                                    RGBLuminanceSource(
                                            _rawCropRect.width(), // int dataWidth
                                            _rawCropRect.height(), // int dataHeight
                                            rgbToLuminance(_rawBmp) // byte[] yuvData
                                    ))))
        } catch (e3: NotFoundException) {
            //no barcode Found
            _rawBmp.recycle()
        }

        if (!_rawBmp.isRecycled)
            _rawBmp.recycle()

        if (result == null) return null

        Log.e("@@", "BARCODE READ: " + result.text)

        return result.text
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        _delegate.onBarCodeRead(result)
    }

    private fun rgbToLuminance(argb8888: Bitmap): IntArray {
        val size = argb8888.width * argb8888.height
        val pixels = IntArray(size)
        val luminance = IntArray(size)
        argb8888.getPixels(pixels, 0, argb8888.width, _rawCropRect.left, _rawCropRect.top, _rawCropRect.right, _rawCropRect.bottom)

        for (offset in 0 until size) {
            val pixel = pixels[offset]
            val r = pixel shr 16 and 0xff // red
            val g2 = pixel shr 7 and 0x1fe // 2 * green
            val b = pixel shr 0xff // blue
            // Calculate green-favouring average cheaply
            luminance[offset] = ((r + g2 + b) / 4)
        }

        return luminance
    }
}