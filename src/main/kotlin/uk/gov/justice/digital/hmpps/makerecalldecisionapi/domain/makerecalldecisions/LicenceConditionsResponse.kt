package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient.LicenceConditions.ConvictionWithLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ActiveRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeMainCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceConditionTypeSubCat
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.UserAccessResponse

data class LicenceConditionsResponse(
  val userAccessResponse: UserAccessResponse? = null,
  val personalDetailsOverview: PersonalDetailsOverview? = null,
  val activeConvictions: List<ConvictionWithLicenceConditions> = emptyList(),
  val activeRecommendation: ActiveRecommendation? = null,

  // The following fields and mapping code will be removed:
  @Deprecated("Moved to 'activeConvictions'. The 'convictions' field will be removed once the UI has switched over.")
  val convictions: List<ConvictionResponse>? = activeConvictions.map {
    ConvictionResponse(
      active = true,
      offences = listOf(Offence(true, it.mainOffence.description, it.mainOffence.code, it.mainOffence.date)) +
        it.additionalOffences.map { offence -> Offence(false, offence.description, offence.code, offence.date) },
      sentenceDescription = it.sentence?.description,
      sentenceOriginalLength = it.sentence?.length,
      sentenceOriginalLengthUnits = it.sentence?.lengthUnits,
      sentenceExpiryDate = it.sentence?.sentenceExpiryDate,
      licenceExpiryDate = it.sentence?.licenceExpiryDate,
      isCustodial = it.sentence?.isCustodial ?: false,
      statusCode = it.sentence?.custodialStatusCode,
      licenceConditions = it.licenceConditions.map { licenceCondition ->
        LicenceCondition(
          active = true,
          licenceConditionNotes = licenceCondition.notes,
          licenceConditionTypeMainCat = LicenceConditionTypeMainCat(licenceCondition.mainCategory.code, licenceCondition.mainCategory.description),
          licenceConditionTypeSubCat = licenceCondition.subCategory?.let { subCategory -> LicenceConditionTypeSubCat(subCategory.code, subCategory.description) },
        )
      }
    )
  }
)
