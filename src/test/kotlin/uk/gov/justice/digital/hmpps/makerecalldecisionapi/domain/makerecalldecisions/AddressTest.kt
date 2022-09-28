package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressTest {

  private lateinit var address: Address

  @Test
  fun `format name and address with given separator`() {
    address = Address("line 1", null, "Brentwood", "CM13 1AB", false)
    val result = address.separatorFormattedAddress("\n", true, "Joe Bloggs")

    assertEquals(result, "Joe Bloggs\nline 1\nBrentwood\nCM13 1AB")
  }

  @Test
  fun `format address and no name with given separator`() {
    address = Address("line 1", null, "Brentwood", "CM13 1AB", false)
    val result = address.separatorFormattedAddress("\n", false, null)

    assertEquals(result, "line 1\nBrentwood\nCM13 1AB")
  }
}
