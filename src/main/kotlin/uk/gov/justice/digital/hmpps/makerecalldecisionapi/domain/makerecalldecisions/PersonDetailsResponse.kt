package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation

data class PersonDetailsResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val addresses: List<Address>? = null,
  val offenderManager: OffenderManager? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)

fun PersonDetailsResponse.toPersonOnProbation(): PersonOnProbation = PersonOnProbation(
  croNumber = this.personalDetailsOverview?.croNumber,
  mostRecentPrisonerNumber = this.personalDetailsOverview?.mostRecentPrisonerNumber,
  nomsNumber = this.personalDetailsOverview?.nomsNumber,
  pncNumber = this.personalDetailsOverview?.pncNumber,
  name = this.personalDetailsOverview?.name,
  firstName = this.personalDetailsOverview?.firstName,
  middleNames = this.personalDetailsOverview?.middleNames,
  surname = this.personalDetailsOverview?.surname,
  gender = this.personalDetailsOverview?.gender,
  ethnicity = this.personalDetailsOverview?.ethnicity,
  primaryLanguage = this.personalDetailsOverview?.primaryLanguage,
  dateOfBirth = this.personalDetailsOverview?.dateOfBirth,
  addresses = this.addresses,
)
