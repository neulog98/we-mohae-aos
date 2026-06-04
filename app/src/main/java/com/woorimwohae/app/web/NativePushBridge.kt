package com.woorimwohae.app.web

import android.webkit.WebView
import org.json.JSONObject

object NativePushBridge {
  const val OBJECT_NAME = "WooriNativeBridge"
  private const val EVENT_NAME = "woori-native-push-token"

  fun deliverToken(
    webView: WebView,
    token: String,
    previousToken: String?,
    permissionGranted: Boolean,
  ) {
    val payload =
      JSONObject()
        .put("type", "pushToken")
        .put("token", token)
        .put("previousToken", previousToken ?: JSONObject.NULL)
        .put("permissionGranted", permissionGranted)
    dispatch(webView, payload)
  }

  fun deliverError(webView: WebView, message: String) {
    dispatch(webView, JSONObject().put("type", "error").put("message", message))
  }

  private fun dispatch(webView: WebView, payload: JSONObject) {
    val script =
      """
      window.dispatchEvent(new CustomEvent('$EVENT_NAME', { detail: $payload }));
      """.trimIndent()
    webView.post { webView.evaluateJavascript(script, null) }
  }
}
