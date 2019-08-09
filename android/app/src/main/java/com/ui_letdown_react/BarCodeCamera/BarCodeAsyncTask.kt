package com.ui_letdown_react.BarCodeCamera

import android.os.AsyncTask
import java.util.*

class BarCodeAsyncTask(private val _delegate: BarCodeAsyncTaskDelegate): AsyncTask<Void, Void, String>() {

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
}