package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class AlternativesToRecallTried(
  val selected: List<SelectedAlternative>?,
  val allOptions: List<TextValueOption>? = null
)

data class SelectedAlternative(
  val value: String?,
  val details: String?
)
