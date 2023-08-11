package com.helper.wbm.ui.web_view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import com.helper.wbm.ui.WebActivity

class CustomChromeClient (private val dataHandler: ChromeClientHelper): WebChromeClient() {
    private lateinit var req: PermissionRequest
    private val requestPermission = (dataHandler as WebActivity).registerForActivityResult(
        ActivityResultContracts.RequestPermission()){
        if(it) req.grant(req.resources)
    }
    override fun onPermissionRequest(request: PermissionRequest?) {
        request?.resources?.forEach {
            req = request
            val permission = Manifest.permission.CAMERA
            if((dataHandler as WebActivity).checkSelfPermission(permission)== PackageManager.PERMISSION_GRANTED){
                req.grant((req.resources))
            }else
                requestPermission.launch(permission)
        }
    }
    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        dataHandler.handleFileChoose(filePathCallback)
        return true
    }

    interface ChromeClientHelper {
        fun handleFileChoose(valueCallback: ValueCallback<Array<Uri>>?)
    }
}