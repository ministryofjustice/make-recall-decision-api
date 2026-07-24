package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CvlLicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionSection
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.sar.SubjectAccessRequestResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

// This service uses the HMPPS SubjectAccesssRequest library
// which creates the endpoint `/subject-access-request`
// For more info see here: https://github.com/ministryofjustice/hmpps-kotlin-lib/blob/main/readme-contents/SubjectAccessRequestEndpoint.md
@Transactional
@Service
class SubjectAccessRequestService(
  private val recommendationRepository: RecommendationRepository,
) : HmppsProbationSubjectAccessRequestService {
  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val recommendations = recommendationRepository.findByCrnAndCreatedDate(crn, fromDate, toDate)

    if (recommendations.isEmpty()) {
      return null
    } else {
      return HmppsSubjectAccessRequestContent(
        content = SubjectAccessRequestResponse(
          crn = crn,
          recommendations = recommendations
            .map { rec ->
              rec.data.copy(
                cvlLicenceConditionsBreached = rec.data.cvlLicenceConditionsBreached?.let {
                  transformLicenceConditions(it)
                },
                licenceConditionsBreached = rec.data.licenceConditionsBreached?.let {
                  transformAdditionalLicenceConditions(it)
                },
              )
            },
        ),
      )
    }
  }

  // Transforms selectedOptions by resolving internal codes to human-readable title from allOptions.
  // SelectedOption.mainCatCode is replaced with the matching title.
  // This reuses the SelectedOption model fields to avoid changing the response type for SAR rendering.
  fun transformAdditionalLicenceConditions(input: LicenceConditionsBreached): LicenceConditionsBreached {
    val additional = input.additionalLicenceConditions ?: return input
    val subCatToOption = additional.allOptions
      ?.associateBy { it.subCatCode }
      ?: emptyMap()

    val transformedSelectedOptions = additional.selectedOptions
      ?.map { option ->
        val matched = subCatToOption[option.subCatCode]
        SelectedOption(
          mainCatCode = matched?.title ?: option.mainCatCode, // title in place of mainCatCode
          subCatCode = option.subCatCode,
        )
      }

    return input.copy(
      additionalLicenceConditions = additional.copy(
        selectedOptions = transformedSelectedOptions,
      ),
    )
  }

  fun transformLicenceConditions(input: CvlLicenceConditionsBreached): CvlLicenceConditionsBreached = input.copy(
    standardLicenceConditions = transformConditionSection(input.standardLicenceConditions),
    additionalLicenceConditions = transformConditionSection(input.additionalLicenceConditions),
    bespokeLicenceConditions = transformConditionSection(input.bespokeLicenceConditions),
  )

  fun transformConditionSection(section: LicenceConditionSection?): LicenceConditionSection? {
    if (section == null) return null

    val codeToText = section.allOptions
      ?.associate { it.code to it.text }
      ?: emptyMap()

    val transformedSelected = section.selected
      ?.mapNotNull { code -> codeToText[code] }
      ?: emptyList()

    return section.copy(selected = transformedSelected)
  }
}
