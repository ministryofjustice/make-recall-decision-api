package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class NumberGenerator(private val secureRandom: SecureRandom) {

  private val TOKEN_SIZE_BYTES: Int = 128 / 8;

  fun generateToken(): String {
    val buffer = ByteArray(TOKEN_SIZE_BYTES)
    secureRandom.nextBytes(buffer)
    return Base64.getEncoder().encodeToString(buffer)
  }
}