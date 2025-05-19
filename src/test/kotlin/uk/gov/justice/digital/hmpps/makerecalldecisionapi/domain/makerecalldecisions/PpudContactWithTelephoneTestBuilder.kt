package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

/**
 * Helper functions for generating instances of PpudContactWithTelephone with
 * their fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun ppudContactWithTelephone(
  name: String = randomString(),
  faxEmail: String = randomString(),
  telephone: String = randomString(),
) = PpudContactWithTelephone(name, faxEmail, telephone)

internal fun PpudContactWithTelephone.toJson() = json(
  """
      {
        "name": "$name",
        "faxEmail": "$faxEmail",
        "telephone": "$telephone"
      }
  """.trimIndent(),
)
