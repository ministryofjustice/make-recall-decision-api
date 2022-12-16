package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class VulnerabilitiesResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonDetails? = null,
  val vulnerabilities: Vulnerabilities? = null,
  val activeRecommendation: ActiveRecommendation? = null
)

data class Vulnerabilities(
  val error: String? = null,
  val lastUpdatedDate: String? = null,
  val suicide: VulnerabilityDetail? = null,
  val selfHarm: VulnerabilityDetail? = null,
  val vulnerability: VulnerabilityDetail? = null,
  val custody: VulnerabilityDetail? = null,
  val hostelSetting: VulnerabilityDetail? = null
)

data class VulnerabilityDetail(
  val risk: String? = null,
  val previous: String? = null,
  val previousConcernsText: String? = null,
  val current: String? = null,
  val currentConcernsText: String? = null

)
