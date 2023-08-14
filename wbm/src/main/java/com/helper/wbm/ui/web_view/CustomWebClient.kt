package com.helper.wbm.ui.web_view

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.helper.wbm.dec.dec

class CustomWebClient(
    private val webClientHelper: WebClientHelper
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        webClientHelper.clearHistory()
    }
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val titleCont = view?.title?.contains("Nfmbodipmjd871".dec()) == true
        webClientHelper.onFinishLoading(titleCont, url ?: "")
    }
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        request?.url?.toString()?.let { url ->
            val intent = when {
                url.startsWith(TELEGRAM) -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
                url.startsWith(HTTP) || url.startsWith(HTTPS) -> null
                url.startsWith(MAILTO) -> Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = TYPE
                    putExtra(Intent.EXTRA_EMAIL, url.replace(MAILTO, ""))
                }, MAIL)
                url.startsWith(TEL) -> Intent.createChooser(Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse(url)
                }, CALL)
                else -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            intent?.let {
                try {
                    view?.context?.startActivity(it)
                    return true
                } catch (e: Exception) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        private val TELEGRAM = "iuuqt://u.nf/kpjodibu".dec()
        private val HTTP = "iuuq://".dec()
        private val HTTPS = "iuuqt://".dec()
        private val MAILTO = "nbjmup:".dec()
        private val TEL = "ufm:".dec()
        private val MAIL = "Nbjm".dec()
        private val CALL = "Dbmm".dec()
        private val TYPE = "qmbjo/ufyu".dec()
    }

    interface WebClientHelper{
        fun onFinishLoading(result: Boolean, url: String)
        fun clearHistory()
    }
}
