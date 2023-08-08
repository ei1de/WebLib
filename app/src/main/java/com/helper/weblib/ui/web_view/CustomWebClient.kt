package com.helper.weblib.ui.web_view

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class CustomWebClient(
    private val webClientHelper: WebClientHelper
) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        webClientHelper.clearHistory()
    }
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val titleCont = view?.title?.contains("Melancholic871") == true
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
        private const val TELEGRAM = "https://t.me/joinchat"
        private const val HTTP = "http://"
        private const val HTTPS = "https://"
        private const val MAILTO = "mailto:"
        private const val TEL = "tel:"
        private const val MAIL = "Mail"
        private const val CALL = "Call"
        private const val TYPE = "plain/text"
    }

    interface WebClientHelper{
        fun onFinishLoading(result: Boolean, url: String)
        fun clearHistory()
    }
}
