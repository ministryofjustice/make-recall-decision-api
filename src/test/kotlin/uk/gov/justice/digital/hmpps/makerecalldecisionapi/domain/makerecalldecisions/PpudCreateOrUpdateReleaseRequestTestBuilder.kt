package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import org.mockserver.model.JsonBody.json
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

/**
 * Helper functions for generating instances of classes related to
 * PPUD release requests with their fields pre-filled with random
 * values. Intended for use in unit tests.
 */

internal fun ppudCreateOrUpdateReleaseRequest(
  dateOfRelease: LocalDate = randomLocalDate(),
  postRelease: PpudUpdatePostRelease = ppudUpdatePostRelease(),
  releasedFrom: String = randomString(),
  releasedUnder: String = randomString(),
) =
  PpudCreateOrUpdateReleaseRequest(
    dateOfRelease,
    postRelease,
    releasedFrom,
    releasedUnder,
  )

internal fun PpudCreateOrUpdateReleaseRequest.toJson() =
  json(
    """
      {
        "dateOfRelease": "$dateOfRelease",
        "postRelease": ${postRelease.toJson()},
        "releasedFrom": "$releasedFrom",
        "releasedUnder": "$releasedUnder"
      }
  """.trimIndent())