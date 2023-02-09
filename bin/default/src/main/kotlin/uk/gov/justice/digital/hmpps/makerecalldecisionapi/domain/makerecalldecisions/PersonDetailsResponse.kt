package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class PersonDetailsResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val addresses: List<Address>? = null,
  val offenderManager: OffenderManager? = null,
  val activeRecommendation: ActiveRecommendation? = null,
)

fun PersonDetailsResponse.toPersonOnProbation(): PersonOnProbation =
  PersonOnProbation(
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
