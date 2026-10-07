package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AnonymousPersona
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MilesAre", appName)
  }

  @Test
  fun `anonymous persona generation produces valid alias and avatar`() {
    val persona = AnonymousPersona.generateRandom()
    assertNotNull(persona.alias)
    assertTrue(persona.alias.contains("#"))
    assertTrue(persona.avatarKey in AnonymousPersona.AVATAR_KEYS)
  }
}
