package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

data class RecommendationResponse(
  val id: Long? = null,
  val status: Status? = null,
  val custodyStatus: CustodyStatus? = null,
  val crn: String? = null,
  val recallType: RecallType? = null,
  val responseToProbation: String? = null,
  val personOnProbation: PersonOnProbation? = null
)

data class PersonOnProbation(
  val name: String? = null,
  val firstName: String? = null,
  val surname: String? = null
)
