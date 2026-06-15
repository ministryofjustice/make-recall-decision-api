package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDate

internal fun ppudSearchResponse(
  results: List<PpudSearchResult> = listOf(ppudSearchResult()),
) = PpudSearchResponse(results)

internal fun ppudSearchResult(
  id: String = randomString(),
  croNumber: String = randomString(),
  nomsId: String = randomString(),
  firstNames: String = randomString(),
  familyName: String = randomString(),
  dateOfBirth: LocalDate = randomLocalDate(),
) = PpudSearchResult(
  id = id,
  croNumber = croNumber,
  nomsId = nomsId,
  firstNames = firstNames,
  familyName = familyName,
  dateOfBirth = dateOfBirth,
)
