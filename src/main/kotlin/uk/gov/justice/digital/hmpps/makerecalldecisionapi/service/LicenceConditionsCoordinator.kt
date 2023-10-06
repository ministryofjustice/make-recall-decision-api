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
    val nDeliusActiveCustodialConvictions =
      nDeliusLicenceConditions.activeConvictions.filter { it.sentence?.isCustodial == true }
    val hasAllConvictionsReleasedOnLicence =
      hasAllActiveCustodialConvictionsReleasedOnLicence(nDeliusActiveCustodialConvictions)
    val onLicenceCvlWithLaterOrSameStartDate =
      isCvlLicenceMoreOrAsRecent(
        cvlLicenceConditions,
        nDeliusActiveCustodialConvictions,
        hasAllConvictionsReleasedOnLicence,
      )
    return SelectedLicenceConditions(
      hasAllConvictionsReleasedOnLicence = hasAllConvictionsReleasedOnLicence,
      ndeliusActiveConvictions = nDeliusActiveCustodialConvictions,
      ndeliusActiveCustodialConvictions = nDeliusActiveCustodialConvictions,
      cvlLicenceCondition = if (onLicenceCvlWithLaterOrSameStartDate) cvlLicenceConditions.firstOrNull() else null,
    )
  }

  private fun hasAllActiveCustodialConvictionsReleasedOnLicence(activeCustodialConvictions: List<LicenceConditions.ConvictionWithLicenceConditions>): Boolean {
    return activeCustodialConvictions.isNotEmpty() &&
      activeCustodialConvictions.all { it.sentence?.custodialStatusCode == "B" }
  }

  private fun isCvlLicenceMoreOrAsRecent(
    cvlLicenceConditions: List<LicenceConditionResponse>,
    activeCustodialConvictions: List<LicenceConditions.ConvictionWithLicenceConditions>,
    onLicence: Boolean,
  ): Boolean {
    val cvlLicenceStartDates =
      cvlLicenceConditions.filter { it.licenceStatus == "ACTIVE" }.mapNotNull { it.licenceStartDate }.sortedDescending()

    val cvlLicenceMoreRecent = activeCustodialConvictions
      .flatMap { it.licenceConditions }.map { it.startDate }.all {
        cvlLicenceStartDates.isNotEmpty() &&
          (it.isBefore(cvlLicenceStartDates[0]) || it.isEqual(cvlLicenceStartDates[0]))
      }
    return onLicence && cvlLicenceMoreRecent
  }
}

data class SelectedLicenceConditions(
  val hasAllConvictionsReleasedOnLicence: Boolean,
  val ndeliusActiveCustodialConvictions: List<LicenceConditions.ConvictionWithLicenceConditions> = emptyList(),
  @Deprecated("This contains active custodial convictions and is replaced by ndeliusActiveCustodialConvictions")
  val ndeliusActiveConvictions: List<LicenceConditions.ConvictionWithLicenceConditions> = emptyList(),
  val cvlLicenceCondition: LicenceConditionResponse? = null,
)
