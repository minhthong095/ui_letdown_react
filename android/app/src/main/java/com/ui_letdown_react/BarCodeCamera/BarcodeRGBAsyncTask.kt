package com.ui_letdown_react.BarCodeCamera

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.AsyncTask
import android.util.Log
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer

class BarCodeRGBAsyncTask(
//        private val _file: File,
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

        val croppedBmp = Bitmap.createBitmap(_rawBmp, _rawCropRect.left, _rawCropRect.top, _rawCropRect.right, _rawCropRect.bottom)

//        val byteOutputStream = ByteArrayOutputStream()
//        croppedBmp.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)
//        val byteJpeg = byteOutputStream.toByteArray()
//
//        val out = FileOutputStream(_file)
//        out.write(byteJpeg)
//        out.flush()
//        out.close()


        try {

            result = _barcodeReader.decodeWithState(
                    BinaryBitmap(
                            HybridBinarizer(
                                    RGBLuminanceSource(
                                            croppedBmp.width, // int dataWidth
                                            croppedBmp.height, // int dataHeight
                                            rgbToLuminance(croppedBmp) // byte[] yuvData
                                    ))))

        } catch (e3: NotFoundException) {
            //no barcode Found
            _rawBmp.recycle()
            croppedBmp.recycle()
        }

        if (!_rawBmp.isRecycled)
            _rawBmp.recycle()

        if (!croppedBmp.isRecycled)
            croppedBmp.recycle()

        if (result == null) return null

        Log.e("@@", "BARCODE READ: " + result.text)

        return result.text
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        _delegate.onBarCodeRead(result)
    }

    private fun rgbToLuminance(cropBmp: Bitmap): IntArray {
        val size = cropBmp.width * cropBmp.height
        val pixels = IntArray(size)
        val luminance = IntArray(size)
        cropBmp.getPixels(pixels, 0, cropBmp.width, 0, 0, cropBmp.width, cropBmp.height)

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

interface BarCodeAsyncTaskDelegate {
    fun onBarCodeRead(result: String?)
    fun onPreBarCodeRead()
}