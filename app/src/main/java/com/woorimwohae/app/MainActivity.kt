package com.woorimwohae.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.woorimwohae.app.push.NotificationHelper
import com.woorimwohae.app.push.PushTokenStore
import com.woorimwohae.app.web.NativePushBridge
import com.woorimwohae.app.web.WooriWebApp

class MainActivity : ComponentActivity() {
  private var webView: WebView? = null

  private val notificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      deliverPushToken(granted)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    NotificationHelper.createChannel(this)

    onBackPressedDispatcher.addCallback(
      this,
      object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          val currentWebView = webView
          if (currentWebView?.canGoBack() == true) currentWebView.goBack() else finish()
        }
      },
    )

    setContent {
      WooriWebApp(
        startUrl = NotificationHelper.resolveStartUrl(intent),
        onWebViewReady = { webView = it },
        onRequestPushToken = ::handlePushTokenRequest,
      )
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    webView?.loadUrl(NotificationHelper.resolveStartUrl(intent))
  }

  override fun onDestroy() {
    webView?.destroy()
    webView = null
    super.onDestroy()
  }

  private fun handlePushTokenRequest(requestPermission: Boolean) {
    if (
      requestPermission &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      return
    }
    deliverPushToken(isNotificationPermissionGranted())
  }

  private fun deliverPushToken(permissionGranted: Boolean) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
      val currentWebView = webView ?: return@addOnCompleteListener
      if (!task.isSuccessful || task.result.isNullOrBlank()) {
        NativePushBridge.deliverError(currentWebView, "FCM 토큰을 발급받지 못했어요.")
        return@addOnCompleteListener
      }

      val previousToken = PushTokenStore.recordToken(this, task.result)
      NativePushBridge.deliverToken(
        webView = currentWebView,
        token = task.result,
        previousToken = previousToken,
        permissionGranted = permissionGranted,
      )
    }
  }

  private fun isNotificationPermissionGranted(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
      ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
