package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PrisonOffender

data class StaticRecommendationDataWrapper(
  val personOnProbation: PersonOnProbation? = null,
  val region: String? = null,
  val localDeliveryUnit: String? = null,
  val indexOffenceDetails: String? = null,
  val offender: PrisonOffender? = null,
)
