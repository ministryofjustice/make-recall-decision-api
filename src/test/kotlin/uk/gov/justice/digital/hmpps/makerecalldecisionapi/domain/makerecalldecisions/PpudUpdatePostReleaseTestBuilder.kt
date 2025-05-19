package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

/**
 * Helper functions for generating instances of PpudUpdatePostRelease with their
 * fields pre-filled with random values. Intended for use in unit tests.
 */

internal fun ppudUpdatePostRelease(
  assistantChiefOfficer: PpudContact = ppudContact(),
  offenderManager: PpudContactWithTelephone = ppudContactWithTelephone(),
  probationService: String = randomString(),
  spoc: PpudContact = ppudContact(),
) = PpudUpdatePostRelease(
  assistantChiefOfficer,
  offenderManager,
  probationService,
  spoc,
)

internal fun PpudUpdatePostRelease.toJson() = json(
  """
      {
        "assistantChiefOfficer": ${assistantChiefOfficer.toJson()},
        "offenderManager": ${offenderManager.toJson()},
        "probationService": "$probationService",
        "spoc": ${spoc.toJson()}
      }
  """.trimIndent(),
)
