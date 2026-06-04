package com.woorimwohae.app.web

import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import org.json.JSONObject

@Composable
fun WooriWebApp(
  startUrl: String,
  onWebViewReady: (WebView) -> Unit,
  onRequestPushToken: (requestPermission: Boolean) -> Unit,
) {
  var loading by remember { mutableStateOf(true) }

  Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F7F4)).safeDrawingPadding()) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context ->
        WebView(context).apply {
          setBackgroundColor(android.graphics.Color.rgb(247, 247, 244))
          settings.javaScriptEnabled = true
          settings.domStorageEnabled = true
          settings.allowFileAccess = false
          settings.allowContentAccess = false
          settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
          settings.setSupportMultipleWindows(false)
          settings.mediaPlaybackRequiresUserGesture = true
          settings.userAgentString = "${settings.userAgentString} WooriMohaeAndroid/1.0"

          CookieManager.getInstance().setAcceptCookie(true)
          CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)

          webChromeClient = WebChromeClient()
          webViewClient =
            object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                loading = true
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                loading = false
              }

              override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (WebUrlPolicy.isTrusted(uri.toString())) return false
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                return true
              }
            }

          setupNativeBridge(this, onRequestPushToken)
          onWebViewReady(this)
          loadUrl(startUrl)
        }
      },
    )

    if (loading) {
      CircularProgressIndicator(
        modifier = Modifier.align(Alignment.Center),
        color = Color(0xFFB75D69),
      )
    }
  }
}

private fun setupNativeBridge(webView: WebView, onRequestPushToken: (Boolean) -> Unit) {
  if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) return

  WebViewCompat.addWebMessageListener(
    webView,
    NativePushBridge.OBJECT_NAME,
    setOf(WebUrlPolicy.TRUSTED_ORIGIN),
  ) { _, message, sourceOrigin, isMainFrame, _ ->
    if (!isMainFrame || sourceOrigin.toString() != WebUrlPolicy.TRUSTED_ORIGIN) return@addWebMessageListener
    val data = message.data ?: return@addWebMessageListener
    val payload = runCatching { JSONObject(data) }.getOrNull() ?: return@addWebMessageListener
    if (payload.optString("type") == "requestPushToken") {
      onRequestPushToken(payload.optBoolean("requestPermission", false))
    }
  }
}

object WebUrlPolicy {
  const val TRUSTED_ORIGIN = "https://we-mohae.web.app"
  const val HOME_URL = "$TRUSTED_ORIGIN/home/"
  const val CALENDAR_URL = "$TRUSTED_ORIGIN/calendar/"
  const val BUDGET_URL = "$TRUSTED_ORIGIN/budget/"

  fun isTrusted(url: String): Boolean {
    val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return false
    return uri.scheme == "https" && uri.host == "we-mohae.web.app"
  }
}
