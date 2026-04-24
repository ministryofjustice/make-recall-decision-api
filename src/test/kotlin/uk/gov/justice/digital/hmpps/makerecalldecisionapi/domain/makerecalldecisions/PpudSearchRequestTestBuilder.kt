package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

internal fun ppudSearchRequest(
  croNumber: String? = randomString(),
  nomsId: String? = randomString(),
  familyName: String = randomString(),
  dateOfBirth: LocalDate = randomLocalDate(),
) = PpudSearchRequest(
  croNumber = croNumber,
  nomsId = nomsId,
  familyName = familyName,
  dateOfBirth = dateOfBirth,
)
