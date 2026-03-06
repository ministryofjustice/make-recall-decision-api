package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import java.time.LocalDate

fun previousRecalls(
  lastRecallDate: LocalDate? = randomLocalDate(),
  hasBeenRecalledPreviously: Boolean? = randomBoolean(),
  previousRecallDates: List<LocalDate>? = listOf(randomLocalDate()),
) = PreviousRecalls(
  lastRecallDate = lastRecallDate,
  hasBeenRecalledPreviously = hasBeenRecalledPreviously,
  previousRecallDates = previousRecallDates,
)
