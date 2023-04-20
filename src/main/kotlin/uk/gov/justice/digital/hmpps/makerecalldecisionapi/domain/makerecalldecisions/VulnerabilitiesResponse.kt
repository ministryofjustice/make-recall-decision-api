package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.UserAccess
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation

data class VulnerabilitiesResponse(
  val userAccessResponse: UserAccess? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
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
