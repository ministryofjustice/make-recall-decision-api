package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.ConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation

data class LicenceConditionsResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val activeConvictions: List<ConvictionWithLicenceConditions> = emptyList(),
  val activeRecommendation: ActiveRecommendation? = null,
  val source: String? = null,
  val hasAllConvictionsReleasedOnLicence: Boolean? = null
) : SelectedLicenceConditionsResponse
