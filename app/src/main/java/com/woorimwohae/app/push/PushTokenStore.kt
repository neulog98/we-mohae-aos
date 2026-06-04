package com.woorimwohae.app.push

import android.content.Context

object PushTokenStore {
  private const val PREFERENCES = "push_token"
  private const val CURRENT_TOKEN = "current_token"
  private const val PREVIOUS_TOKEN = "previous_token"

  fun recordToken(context: Context, token: String): String? {
    val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    val current = preferences.getString(CURRENT_TOKEN, null)
    if (current == token) return preferences.getString(PREVIOUS_TOKEN, null)

    preferences
      .edit()
      .putString(CURRENT_TOKEN, token)
      .putString(PREVIOUS_TOKEN, current)
      .apply()
    return current
  }
}
