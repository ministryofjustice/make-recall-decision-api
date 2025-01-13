package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

/**
 * Helper functions for generating instances of PpudContact with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun ppudContact(
  name: String = randomString(),
  faxEmail: String = randomString(),
) = PpudContact(name, faxEmail)

internal fun PpudContact.toJson() =
  json(
    """
      {
        "name": "$name",
        "faxEmail": "$faxEmail"
      }
    """.trimIndent(),
  )
