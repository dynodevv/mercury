package com.dynodevv.mercury.ui.components

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dynodevv.mercury.service.adblock.AdBlockService

class MercuryWebViewClient(
    private val adBlockService: AdBlockService,
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String, String?) -> Unit,
    private val onShouldOverrideUrlLoading: (String) -> Boolean
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        url?.let { onPageStarted(it) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        url?.let { onPageFinished(it, view?.title) }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        return onShouldOverrideUrlLoading(url)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        return if (adBlockService.isBlocked(url)) {
            WebResourceResponse("text/plain", "UTF-8", java.io.ByteArrayInputStream(byteArrayOf()))
        } else {
            null
        }
    }
}
