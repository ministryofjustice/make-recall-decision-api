package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation

data class StaticRecommendationDataWrapper(
  val personOnProbation: PersonOnProbation? = null,
  val convictionDetail: ConvictionDetail? = null,
  val region: String? = null,
  val localDeliveryUnit: String? = null,
)
