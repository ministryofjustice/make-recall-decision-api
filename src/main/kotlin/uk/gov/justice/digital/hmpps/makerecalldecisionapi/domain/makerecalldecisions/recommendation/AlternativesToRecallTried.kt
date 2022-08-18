package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class AlternativesToRecallTried(
  val selected: List<ValueWithDetails>?,
  val allOptions: List<TextValueOption>? = null
)

enum class SelectedAlternativeOptions {
  WARNINGS_LETTER,
  DRUG_TESTING,
  INCREASED_FREQUENCY,
  EXTRA_LICENCE_CONDITIONS,
  REFERRAL_TO_APPROVED_PREMISES,
  REFERRAL_TO_OTHER_TEAMS,
  REFERRAL_TO_PARTNERSHIP_AGENCIES,
  RISK_ESCALATION,
  ALTERNATIVE_TO_RECALL_OTHER,
  NONE
}
