package com.ui_letdown_react.BarCodeCamera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class BarCodeAsyncTask(
        private val _delegate: BarCodeAsyncTaskDelegate,
        private val _byteData: ByteArray,
        private val _height: Int,
        private val _width: Int

): AsyncTask<Void, Void, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
        _delegate.onPreBarCodeRead()
    }

    override fun doInBackground(vararg params: Void?): String {
        return (Random().nextInt(100 - 50 + 1) - 50).toString()
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        _delegate.onBarCodeRead(result)
    }
}

interface BarCodeAsyncTaskDelegate {
    fun onBarCodeRead(result: String)
    fun onPreBarCodeRead()
}