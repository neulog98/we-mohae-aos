package com.woorimwohae.app.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WooriFirebaseMessagingService : FirebaseMessagingService() {
  override fun onNewToken(token: String) {
    PushTokenStore.recordToken(this, token)
  }

  override fun onMessageReceived(message: RemoteMessage) {
    val title = message.notification?.title ?: "우리모해"
    val body = message.notification?.body ?: "새로운 알림이 있어요."
    NotificationHelper.showNotification(this, title, body, message.data)
  }
}
