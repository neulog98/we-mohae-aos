package com.woorimwohae.app.web

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebUrlPolicyTest {
  @Test
  fun trustedHostingUrlsStayInsideWebView() {
    assertTrue(WebUrlPolicy.isTrusted("https://we-mohae.web.app/calendar/"))
  }

  @Test
  fun externalAndCleartextUrlsAreRejected() {
    assertFalse(WebUrlPolicy.isTrusted("https://example.com/"))
    assertFalse(WebUrlPolicy.isTrusted("http://we-mohae.web.app/"))
  }
}
