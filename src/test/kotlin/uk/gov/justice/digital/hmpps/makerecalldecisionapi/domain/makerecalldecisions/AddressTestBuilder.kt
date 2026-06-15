package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun address(
  line1: String? = randomString(),
  line2: String? = randomString(),
  town: String? = randomString(),
  postcode: String? = randomString(),
  noFixedAbode: Boolean = randomBoolean(),
) = Address(
  line1 = line1,
  line2 = line2,
  town = town,
  postcode = postcode,
  noFixedAbode = noFixedAbode,
)
