package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class AlternativesToRecallTried(
  val selected: List<SelectedAlternative>?,
  val allOptions: List<RecallAlternative>?
)

data class SelectedAlternative(
  val value: String?,
  val details: String?
)

data class RecallAlternative(
  val value: String?,
  val text: String?
)
