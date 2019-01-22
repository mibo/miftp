package de.mirb.project.miftp

import org.junit.Ignore
import org.junit.Test
import org.springframework.security.crypto.factory.PasswordEncoderFactories

class ApplicationHelper {
  @Test
  @Ignore("just for manual password encoding")
  fun encodedPasswords() {
    val encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
    println(encoder.encode("b0lzM!CH"))
  }
}