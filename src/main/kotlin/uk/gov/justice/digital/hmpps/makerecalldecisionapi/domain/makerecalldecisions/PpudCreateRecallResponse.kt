package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class PpudCreateRecallResponse(
  val recall: PpudCreateRecall = PpudCreateRecall(),
)

data class PpudCreateRecall(
  val id: String? = null,
)
