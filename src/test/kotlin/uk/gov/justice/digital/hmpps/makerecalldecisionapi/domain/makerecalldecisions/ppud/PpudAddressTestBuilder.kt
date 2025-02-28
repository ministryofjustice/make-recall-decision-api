package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.ppud

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.toJsonNullableStringField

internal fun ppudAddress(
  premises: String? = randomString(),
  line1: String? = randomString(),
  line2: String? = randomString(),
  postcode: String? = randomString(),
  phoneNumber: String? = randomString(),
) = PpudAddress(
  premises,
  line1,
  line2,
  postcode,
  phoneNumber,
)

internal fun PpudAddress.toJsonBody() = json(toJsonString())

internal fun PpudAddress.toJsonString() =
  """
      {
          "premises" : ${toJsonNullableStringField(premises)},
          "line1" : ${toJsonNullableStringField(line1)},
          "line2" : ${toJsonNullableStringField(line2)},
          "postcode" : ${toJsonNullableStringField(postcode)},
          "phoneNumber" : ${toJsonNullableStringField(phoneNumber)}
        }
  """.trimIndent()
