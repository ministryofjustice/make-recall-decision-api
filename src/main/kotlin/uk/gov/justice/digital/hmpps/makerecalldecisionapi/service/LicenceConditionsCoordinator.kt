package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse

@Service
class LicenceConditionsCoordinator {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  internal fun selectLicenceConditions(
    nDeliusLicenceConditions: LicenceConditions,
    cvlLicenceConditions: List<LicenceConditionResponse>,
  ): SelectedLicenceConditions {
    val hasAllConvictionsReleasedOnLicence = hasAllActiveCustodialConvictionsReleasedOnLicence(nDeliusLicenceConditions)
    val onLicenceCvlWithLaterOrSameStartDate =
      isCvlLicenceMoreOrAsRecent(nDeliusLicenceConditions, cvlLicenceConditions, hasAllConvictionsReleasedOnLicence)
    return SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = hasAllConvictionsReleasedOnLicence,
      ndeliusActiveConvictions = nDeliusLicenceConditions.activeConvictions.filter { it.sentence?.isCustodial == true },
      cvlLicenceCondition = if (onLicenceCvlWithLaterOrSameStartDate) cvlLicenceConditions.firstOrNull() else null,
    )
  }

  private fun hasAllActiveCustodialConvictionsReleasedOnLicence(nDeliusLicenceConditions: LicenceConditions): Boolean {
    val hasAtLeastOneActiveCustodialConviction = nDeliusLicenceConditions.activeConvictions.isNotEmpty() && nDeliusLicenceConditions.activeConvictions.any { it.sentence?.isCustodial == true }
    return hasAtLeastOneActiveCustodialConviction &&
      nDeliusLicenceConditions.activeConvictions
        .filter { activeConvictions -> activeConvictions.sentence?.isCustodial == true }
        .all { activeCustodialConvictions -> activeCustodialConvictions.sentence?.custodialStatusCode == "B" }
  }

  private fun isCvlLicenceMoreOrAsRecent(
    nDeliusLicenceConditions: LicenceConditions,
    cvlLicenceConditions: List<LicenceConditionResponse>,
    onLicence: Boolean,
  ): Boolean {
    val cvlLicenceStartDates =
      cvlLicenceConditions.filter { it.licenceStatus == "ACTIVE" }.mapNotNull { it.licenceStartDate }.sortedDescending()

    val cvlLicenceMoreRecent = nDeliusLicenceConditions.activeConvictions.filter { it.sentence?.isCustodial == true }
      .flatMap { it.licenceConditions }.map { it.startDate }.all {
        cvlLicenceStartDates.isNotEmpty() &&
          (it.isBefore(cvlLicenceStartDates[0]) || it.isEqual(cvlLicenceStartDates[0]))
      }
    return onLicence && cvlLicenceMoreRecent
  }
}

data class SelectedLicenceConditions(
  val hasAllConvictionsReleasedOnLicence: Boolean,
  val ndeliusActiveConvictions: List<LicenceConditions.ConvictionWithLicenceConditions> = emptyList(),
  val cvlLicenceCondition: LicenceConditionResponse? = null,
)
