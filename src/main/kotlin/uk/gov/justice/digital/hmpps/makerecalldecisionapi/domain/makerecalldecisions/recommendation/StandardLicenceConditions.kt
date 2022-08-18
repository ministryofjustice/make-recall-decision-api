package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class StandardLicenceConditions(
  val selected: List<String>? = null,
  val allOptions: List<TextValueOption>? = null
)

enum class SelectedStandardLicenceConditions {
  GOOD_BEHAVIOUR,
  NO_OFFENCE,
  KEEP_IN_TOUCH,
  SUPERVISING_OFFICER_VISIT,
  ADDRESS_APPROVED,
  NO_WORK_UNDERTAKEN,
  NO_TRAVEL_OUTSIDE_UK
}
