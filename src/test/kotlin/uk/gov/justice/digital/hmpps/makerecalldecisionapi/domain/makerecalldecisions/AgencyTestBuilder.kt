package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.toJsonNullableStringField

/**
 * Helper functions for generating instances of Agency with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun agency(
  agencyId: String? = randomString(),
  description: String? = randomString(),
  longDescription: String? = randomString(),
  agencyType: String? = randomString(),
) = Agency(
  agencyId,
  description,
  longDescription,
  agencyType,
)

internal fun Agency.toJson() =
  json(
    """
      {
        "agencyId": ${toJsonNullableStringField(agencyId)},
        "description": ${toJsonNullableStringField(description)},
        "longDescription": ${toJsonNullableStringField(longDescription)},
        "agencyType": ${toJsonNullableStringField(agencyType)}
      }
    """.trimIndent(),
  )
