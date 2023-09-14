package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class NumberGeneratorTest {
  @Test
  fun `generate random numbers`() {

    val secureRandom = SecureRandom()

    val generator = NumberGenerator(secureRandom)

    val number1 = generator.generateToken()
    val number2 = generator.generateToken()

    assertThat(number1).isNotNull()
    assertThat(number1.length).isEqualTo(24)

    assertThat(number2).isNotNull()
    assertThat(number2.length).isEqualTo(24)

    assertThat(number1).isNotEqualTo(number2)
  }
}

