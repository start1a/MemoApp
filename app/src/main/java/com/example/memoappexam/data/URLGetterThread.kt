package com.example.memoappexam.data

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.widget.Toast
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class URLGetterThread(context: Context): AsyncTask<String, Void, Int>() {

    private var conText: Context = context
    lateinit var urlImageSaveListener: () -> Unit

    override fun doInBackground(vararg params: String?): Int {
        lateinit var con: HttpsURLConnection

        var url = URL(params[0])
        con = url.openConnection() as HttpsURLConnection
        con.connect()

        val code = con.responseCode
        con.disconnect()
        return code
    }

    override fun onPostExecute(result: Int?) {
        super.onPostExecute(result)

        if (result == 200) {
            Toast.makeText(conText, "이미지 업로드 완료", Toast.LENGTH_SHORT).show()
            urlImageSaveListener()
        }
        else Toast.makeText(conText, "잘못된 URL입니다", Toast.LENGTH_SHORT).show()
    }


}