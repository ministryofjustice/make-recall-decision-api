package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ArnApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Vulnerabilities
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.VulnerabilitiesResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.VulnerabilityDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.oasysarnapi.RiskVulnerabilityTypeResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.ExceptionCodeHelper.Helper.extractErrorCode

@Service
internal class VulnerabilitiesService(
  @Qualifier("assessRisksNeedsApiClientUserEnhanced") private val arnApiClient: ArnApiClient,
  private val userAccessValidator: UserAccessValidator,
  private val recommendationService: RecommendationService,
  private val personDetailsService: PersonDetailsService,
) {

  suspend fun getVulnerabilities(crn: String): VulnerabilitiesResponse {
    val userAccessResponse = userAccessValidator.checkUserAccess(crn)
    return if (userAccessValidator.isUserExcludedRestrictedOrNotFound(userAccessResponse)) {
      VulnerabilitiesResponse(userAccessResponse = userAccessResponse)
    } else {
      val personalDetailsOverview = personDetailsService.buildPersonalDetailsOverviewResponse(crn)
      val vulnerabilities = getVulnerabilityDetail(crn)
      val recommendationDetails = recommendationService.getRecommendationsInProgressForCrn(crn)

      return VulnerabilitiesResponse(
        personalDetailsOverview = personalDetailsOverview,
        vulnerabilities = vulnerabilities,
        activeRecommendation = recommendationDetails
      )
    }
  }

  private suspend fun getVulnerabilityDetail(crn: String): Vulnerabilities {
    val riskResponse = try {
      getValueAndHandleWrappedException(arnApiClient.getRisksWithFullText(crn))
    } catch (ex: Exception) {
      return Vulnerabilities(error = extractErrorCode(ex, "risks", crn))
    }

    return Vulnerabilities(
      lastUpdatedDate = riskResponse?.assessedOn?.let { DateTimeHelper.convertUtcDateTimeStringToIso8601Date(it) },
      suicide = extractVulnerabilityFromRisk(riskResponse?.riskToSelf?.suicide),
      selfHarm = extractVulnerabilityFromRisk(riskResponse?.riskToSelf?.selfHarm),
      vulnerability = extractVulnerabilityFromRisk(riskResponse?.riskToSelf?.vulnerability),
      custody = extractVulnerabilityFromRisk(riskResponse?.riskToSelf?.custody),
      hostelSetting = extractVulnerabilityFromRisk(riskResponse?.riskToSelf?.hostelSetting),
    )
  }

  private suspend fun extractVulnerabilityFromRisk(vulnerabilityResponse: RiskVulnerabilityTypeResponse?): VulnerabilityDetail {
    return VulnerabilityDetail(
      vulnerabilityResponse?.risk,
      vulnerabilityResponse?.previous,
      vulnerabilityResponse?.previousConcernsText,
      vulnerabilityResponse?.current,
      vulnerabilityResponse?.currentConcernsText
    )
  }
}
