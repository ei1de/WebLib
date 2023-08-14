package com.helper.wbm.ui

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.ValueCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import com.helper.wbm.dec.dec
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class WebViewModel:ViewModel() {
    private var valueCallback: ValueCallback<Array<Uri>>? = null

    fun getImage(result:ActivityResult){
        val data = result.data
        val resultUri = mutableListOf<Uri>()

        if (result.resultCode != Activity.RESULT_OK) {
            valueCallback?.onReceiveValue(null)
            valueCallback = null

        } else {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    resultUri.add(uri)

                } else {
                    @Suppress("DEPRECATION")
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data.extras?.getParcelable("data", Bitmap::class.java)
                    } else {
                        data.extras?.get("data") as Bitmap
                    }
                    bitmap?.let {
                        resultUri.add(getUri(bitmap))
                    }
                }
                valueCallback?.onReceiveValue(resultUri.toTypedArray())
                valueCallback = null
            }
        }
    }
    private fun getUri(bitmap: Bitmap): Uri {
        val tempFile = File.createTempFile(
            "unq".dec(),
            ".kqh".dec()
        )
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        val fileOutPut = FileOutputStream(tempFile)
        fileOutPut.write(bitmapData)
        fileOutPut.flush()
        fileOutPut.close()
        return Uri.fromFile(tempFile)
    }

    fun handleFileChoose(callback: ValueCallback<Array<Uri>>?, permissionRequest: ActivityResultLauncher<String>) {
        valueCallback = callback
        permissionRequest.launch(Manifest.permission.CAMERA)
    }
}