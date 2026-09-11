package com.jarvis.assistant

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.settings.javaScriptEnabled = true

        webView.settings.domStorageEnabled = true

        webView.webViewClient = WebViewClient()

        /*
         * JavaScript -> Android bridge
         */

        webView.addJavascriptInterface(
            JarvisBridge(this),
            "JARVIS_ANDROID"
        )

        webView.loadUrl(
            "http://10.0.2.2:3000"
        )

        setContentView(webView)
    }
}
