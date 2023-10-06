package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.LicenceConditionResponse

@Service
class LicenceConditionsCoordinator {

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
    val activeCustodial = nDeliusLicenceConditions.activeConvictions.filter { it.sentence?.isCustodial == true }
    return activeCustodial.isNotEmpty() &&
      activeCustodial.all { it.sentence?.custodialStatusCode == "B" }
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
