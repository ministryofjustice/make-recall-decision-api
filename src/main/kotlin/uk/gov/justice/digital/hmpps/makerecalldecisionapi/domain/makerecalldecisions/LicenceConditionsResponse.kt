package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.ConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class LicenceConditionsResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val activeConvictions: List<ConvictionWithLicenceConditions> = emptyList(),
  val activeRecommendation: ActiveRecommendation? = null
)
