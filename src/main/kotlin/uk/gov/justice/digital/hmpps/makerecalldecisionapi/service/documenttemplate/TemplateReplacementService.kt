package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import com.deepoove.poi.XWPFTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.DecisionNotToRecallLetterDocumentMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.PartADocumentMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LetterContent
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilitiesRecommendation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.ADULT_OR_CHILD_SAFEGUARDING_CONCERNS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.BEING_AT_RISK_OF_SERIOUS_HARM_FROM_OTHERS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.BEING_BULLIED_BY_OTHERS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.BEREAVEMENT_ISSUES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.BULLYING_OTHERS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.CULTURAL_OR_LANGUAGE_DIFFERENCES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.DOMESTIC_ABUSE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.DRUG_OR_ALCOHOL_USE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.LEARNING_DIFFICULTIES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.MEDICATION_TAKEN_INCLUDING_COMPLIANCE_WITH_MEDICATION
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.MENTAL_HEALTH_CONCERNS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.NONE
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.NOT_KNOWN
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.PHYSICAL_DISABILITIES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.PHYSICAL_HEALTH_CONCERNS
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.RELATIONSHIP_BREAKDOWN
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VulnerabilityOptions.RISK_OF_SUICIDE_OR_SELF_HARM
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_SPECIFIED
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.TICK_CHARACTER
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
internal class TemplateReplacementService(
  val partADocumentMapper: PartADocumentMapper,
  val decisionNotToRecallLetterDocumentMapper: DecisionNotToRecallLetterDocumentMapper,
  val templateRetrievalService: TemplateRetrievalService,
) {

  suspend fun generateDocFromRecommendation(
    recommendation: RecommendationResponse,
    documentType: DocumentType,
    metaData: RecommendationMetaData,
    featureFlags: FeatureFlags = FeatureFlags(),
  ): String {
    val documentData =
      if (documentType == DocumentType.PART_A_DOCUMENT || documentType == DocumentType.PREVIEW_PART_A_DOCUMENT) {
        partADocumentMapper.mapRecommendationDataToDocumentData(recommendation, metaData, featureFlags)
      } else {
        decisionNotToRecallLetterDocumentMapper.mapRecommendationDataToDocumentData(recommendation)
      }

    val templateClassPathResource = templateRetrievalService.loadDocumentTemplate(documentType)
    val template = XWPFTemplate.compile(templateClassPathResource.inputStream)

//    template.xwpfDocument.headerFooterPolicy.createWatermark("PREVIEW")
//    styleWatermark(template.xwpfDocument.headerFooterPolicy.getHeader(XWPFHeaderFooterPolicy.DEFAULT))

    val file = template.render(
      mappingsForTemplate(documentData),
    )
//    file.writeToFile("out_template_${documentType}.docx")
    return writeDataToFile(file)
  }

//  private fun styleWatermark(header: XWPFHeader) {
//    val paragraph = header.getParagraphArray(0)
//    val xmlobjects: Array<XmlObject> =
//      paragraph.getCTP().getRArray(0).getPictArray(0).selectChildren(QName("urn:schemas-microsoft-com:vml", "shape"))
//
//    if (xmlobjects.size > 0) {
//      val ctshape = xmlobjects[0] as CTShape
//      ctshape.fillcolor = "#d8d8d8"
//      ctshape.style = ctshape.style + ";rotation:315"
//    }
//  }

  fun generateLetterContentForPreviewFromRecommendation(recommendation: RecommendationResponse): LetterContent {
    val documentData = decisionNotToRecallLetterDocumentMapper.mapRecommendationDataToDocumentData(recommendation)

    return LetterContent(
      letterAddress = documentData.letterAddress,
      letterDate = DateTimeHelper.convertLocalDateToDateWithSlashes(LocalDate.now()),
      salutation = documentData.salutation,
      letterTitle = documentData.letterTitle,
      section1 = documentData.section1,
      section2 = documentData.section2,
      section3 = documentData.section3,
      signedByParagraph = documentData.signedByParagraph,
    )
  }

  // TODO MRD-2785: outsource conversion
  fun mappingsForTemplate(documentData: DocumentData): HashMap<String, String?> {
    val mappings = hashMapOf(
      "custody_status" to documentData.custodyStatus?.value,
      "custody_status_details" to documentData.custodyStatus?.details?.replace("\r\n", ", "),
      "recall_type" to documentData.recallType?.value,
      "recall_type_details" to documentData.recallType?.details,
      "response_to_probation" to documentData.responseToProbation,
      "what_led_to_recall" to documentData.whatLedToRecall,
      "is_this_an_emergency_recall" to documentData.isThisAnEmergencyRecall,
      "is_under_18" to documentData.isUnder18,
      "is_sentence_12_months_or_over" to documentData.isSentence12MonthsOrOver,
      "is_mappa_above_level_1" to documentData.isMappaAboveLevel1,
      "is_charged_with_serious_offence" to documentData.isChargedWithSeriousOffence,
      "is_sentence_48_months_or_over" to documentData.isSentence48MonthsOrOver,
      "is_mappa_category_4" to documentData.isMappaCategory4,
      "is_mappa_level_2_or_3" to documentData.isMappaLevel2Or3,
      "is_recalled_on_new_charged_offence" to documentData.isRecalledOnNewChargedOffence,
      "is_serving_ft_sentence_for_terrorist_offence" to documentData.isServingFTSentenceForTerroristOffence,
      "has_been_charged_with_terrorist_or_state_threat_offence" to documentData.hasBeenChargedWithTerroristOrStateThreatOffence,
      "has_victims_in_contact_scheme" to documentData.hasVictimsInContactScheme,
      "indeterminate_sentence_type" to documentData.indeterminateSentenceType,
      "is_extended_sentence" to documentData.isExtendedSentence,
      "date_vlo_informed" to documentData.dateVloInformed,
      "has_arrest_issues" to documentData.hasArrestIssues?.value,
      "has_arrest_issues_details" to documentData.hasArrestIssues?.details,
      "has_contraband_risk" to documentData.hasContrabandRisk?.value,
      "has_contraband_risk_details" to documentData.hasContrabandRisk?.details,
      "additional_conditions_breached" to documentData.additionalConditionsBreached,
      "is_under_integrated_offender_management" to documentData.isUnderIntegratedOffenderManagement?.let {
        YesNoNotApplicableOptions.valueOf(
          it,
        ).partADisplayValue
      },
      "contact_name" to documentData.localPoliceContact?.contactName,
      "phone_number" to documentData.localPoliceContact?.phoneNumber,
      "fax_number" to documentData.localPoliceContact?.faxNumber,
      "email_address" to documentData.localPoliceContact?.emailAddress,
      "has_vulnerabilities" to if (hasVulnerabilities(documentData.vulnerabilities)) YES else NO,
      "gender" to documentData.gender,
      "date_of_birth" to documentData.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      "name" to documentData.name,
      "ethnicity" to if (documentData.ethnicity.isNullOrBlank()) NOT_SPECIFIED else documentData.ethnicity,
      "primary_language" to documentData.primaryLanguage,
      "last_recorded_address" to documentData.lastRecordedAddress,
      "no_fixed_abode" to documentData.noFixedAbode,
      "cro_number" to documentData.croNumber,
      "pnc_number" to documentData.pncNumber,
      "crn" to documentData.crn,
      "most_recent_prisoner_number" to documentData.mostRecentPrisonerNumber,
      "noms_number" to documentData.nomsNumber,
      "gender" to documentData.gender,
      "index_offence_description" to documentData.indexOffenceDescription,
      "date_of_original_offence" to documentData.dateOfOriginalOffence,
      "date_of_sentence" to documentData.dateOfSentence,
      "length_of_sentence" to documentData.lengthOfSentence,
      "licence_expiry_date" to documentData.licenceExpiryDate,
      "sentence_expiry_date" to documentData.sentenceExpiryDate,
      "custodial_term" to documentData.custodialTerm,
      "extended_term" to documentData.extendedTerm,
      "mappa_level" to formatMappaLevel(documentData.mappa),
      "mappa_category" to formatMappaCategory(documentData.mappa),
      "completed_by_name" to documentData.completedBy.name,
      "completed_by_telephone" to documentData.completedBy.telephone,
      "completed_by_email" to documentData.completedBy.email,
      "completed_by_region" to documentData.completedBy.region,
      "completed_by_local_delivery_unit" to documentData.completedBy.localDeliveryUnit,
      "completed_by_ppcs_query_emails" to documentData.completedBy.ppcsQueryEmails.joinToString("; "),
      "supervising_practitioner_name" to documentData.supervisingPractitioner.name,
      "supervising_practitioner_telephone" to documentData.supervisingPractitioner.telephone,
      "supervising_practitioner_email" to documentData.supervisingPractitioner.email,
      "supervising_practitioner_region" to documentData.supervisingPractitioner.region,
      "supervising_practitioner_local_delivery_unit" to documentData.supervisingPractitioner.localDeliveryUnit,
      "supervising_practitioner_ppcs_query_emails" to documentData.supervisingPractitioner.ppcsQueryEmails.joinToString(
        "; ",
      ),
      "revocation_order_recipients" to documentData.revocationOrderRecipients.joinToString("; "),
      "date_of_decision" to documentData.dateOfDecision,
      "time_of_decision" to documentData.timeOfDecision,
      "index_offence_details" to (documentData.offenceAnalysis ?: EMPTY_STRING),
      "fixed_term_additional_licence_conditions" to documentData.fixedTermAdditionalLicenceConditions,
      "behaviour_similar_to_index_offence" to documentData.behaviourSimilarToIndexOffence,
      "behaviour_similar_to_index_offence_present" to documentData.behaviourSimilarToIndexOffencePresent,
      "behaviour_leading_to_sexual_or_violent_offence" to documentData.behaviourLeadingToSexualOrViolentOffence,
      "behaviour_leading_to_sexual_or_violent_offence_present" to documentData.behaviourLeadingToSexualOrViolentOffencePresent,
      "out_of_touch" to documentData.outOfTouch,
      "out_of_touch_present" to documentData.outOfTouchPresent,
      "other_possible_addresses" to documentData.otherPossibleAddresses,
      "salutation" to documentData.salutation,
      "letter_address" to documentData.letterAddress,
      "letter_title" to documentData.letterTitle,
      "letter_date" to documentData.letterDate,
      "section_1" to documentData.section1,
      "section_2" to documentData.section2,
      "section_3" to documentData.section3,
      "letter_signed_by_paragraph" to documentData.signedByParagraph,
      "last_releasing_prison" to documentData.lastReleasingPrison,
      "last_release_date" to documentData.lastReleaseDate,
      "dates_of_last_releases" to documentData.datesOfLastReleases,
      "date_of_previous_recalls" to documentData.datesOfLastRecalls,
      "risk_to_children" to (documentData.riskToChildren ?: EMPTY_STRING),
      "risk_to_public" to (documentData.riskToPublic ?: EMPTY_STRING),
      "risk_to_known_adult" to (documentData.riskToKnownAdult ?: EMPTY_STRING),
      "risk_to_staff" to (documentData.riskToStaff ?: EMPTY_STRING),
      "risk_to_prisoners" to (documentData.riskToPrisoners ?: EMPTY_STRING),
      "countersign_aco_email" to (documentData.countersignAcoEmail ?: EMPTY_STRING),
      "countersign_spo_name" to (documentData.countersignSpoName ?: EMPTY_STRING),
      "countersign_spo_telephone" to (documentData.countersignSpoTelephone ?: EMPTY_STRING),
      "countersign_spo_date" to (documentData.countersignSpoDate ?: EMPTY_STRING),
      "countersign_spo_time" to (documentData.countersignSpoTime ?: EMPTY_STRING),

      "countersign_spo_exposition" to (documentData.countersignSpoExposition ?: EMPTY_STRING),
      "spo_countersign_complete" to (if (!documentData.countersignSpoExposition.isNullOrEmpty()) TICK_CHARACTER else EMPTY_STRING),
      "countersign_spo_email" to (documentData.counterSignSpoEmail ?: EMPTY_STRING),
      "countersign_aco_name" to (documentData.countersignAcoName ?: EMPTY_STRING),
      "countersign_aco_telephone" to (documentData.countersignAcoTelephone ?: EMPTY_STRING),
      "countersign_aco_date" to (documentData.countersignAcoDate ?: EMPTY_STRING),
      "countersign_aco_time" to (documentData.countersignAcoTime ?: EMPTY_STRING),
      "countersign_aco_exposition" to (documentData.countersignAcoExposition ?: EMPTY_STRING),
      "aco_countersign_complete" to (if (!documentData.countersignAcoExposition.isNullOrEmpty()) TICK_CHARACTER else EMPTY_STRING),

      "release_under_ecsl" to if (documentData.releaseUnderECSL == true) YES else NO,
      "date_of_release" to (documentData.dateOfRelease ?: EMPTY_STRING),
      "conditional_release_date" to (documentData.conditionalReleaseDate ?: EMPTY_STRING),

    )
    mappings.putAll(convertToSelectedAlternativesMap(documentData.selectedAlternatives))
    mappings.putAll(convertToSelectedStandardConditionsBreachedMap(documentData.selectedStandardConditionsBreached))
    mappings.putAll(convertToSelectedVulnerabilitiesMap(documentData.vulnerabilities))

    return mappings
  }

  private fun hasVulnerabilities(vulnerabilities: VulnerabilitiesRecommendation?) = vulnerabilities?.selected?.isNotEmpty() == true && !vulnerabilities.selected.any { it.value == NONE.name || it.value == NOT_KNOWN.name }

  private fun convertToSelectedAlternativesMap(selectedAlternatives: List<ValueWithDetails>?): HashMap<String, String> {
    val selectedAlternativesMap = selectedAlternatives?.associate { it.value to it.details } ?: emptyMap()
    return hashMapOf(
      "warning_letter_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.WARNINGS_LETTER.name]
          ?: EMPTY_STRING
        ),
      "drug_testing_details" to (selectedAlternativesMap[SelectedAlternativeOptions.DRUG_TESTING.name] ?: EMPTY_STRING),
      "increased_frequency_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.INCREASED_FREQUENCY.name]
          ?: EMPTY_STRING
        ),
      "extra_licence_conditions_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.EXTRA_LICENCE_CONDITIONS.name]
          ?: EMPTY_STRING
        ),
      "referral_to_approved_premises_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.REFERRAL_TO_APPROVED_PREMISES.name]
          ?: EMPTY_STRING
        ),
      "referral_to_other_teams_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.REFERRAL_TO_OTHER_TEAMS.name]
          ?: EMPTY_STRING
        ),
      "referral_to_partnership_agencies_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.REFERRAL_TO_PARTNERSHIP_AGENCIES.name]
          ?: EMPTY_STRING
        ),
      "alternative_to_recall_other_details" to (
        selectedAlternativesMap[SelectedAlternativeOptions.ALTERNATIVE_TO_RECALL_OTHER.name]
          ?: EMPTY_STRING
        ),
    )
  }

  private fun convertToSelectedStandardConditionsBreachedMap(selectedConditions: List<String>?): Map<String, String> = mapOf(
    "other_name_known_by" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NAME_CHANGE.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "contact_details_changed" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.CONTACT_DETAILS.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "good_behaviour_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "no_offence_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NO_OFFENCE.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "keep_in_touch_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.KEEP_IN_TOUCH.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "officer_visit_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.SUPERVISING_OFFICER_VISIT.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "address_approved_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.ADDRESS_APPROVED.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "no_work_undertaken_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NO_WORK_UNDERTAKEN.name) == true) TICK_CHARACTER else EMPTY_STRING),
    "no_travel_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NO_TRAVEL_OUTSIDE_UK.name) == true) TICK_CHARACTER else EMPTY_STRING),
  )

  private fun convertToSelectedVulnerabilitiesMap(vulnerabilities: VulnerabilitiesRecommendation?): Map<String, String> = mapOf(
    "risk_of_suicide_or_self_harm" to (
      getVulnerabilityDisplayText(
        RISK_OF_SUICIDE_OR_SELF_HARM.name,
        vulnerabilities,
      )
      ),
    "relationship_breakdown" to (getVulnerabilityDisplayText(RELATIONSHIP_BREAKDOWN.name, vulnerabilities)),
    "not_known" to (getVulnerabilityDisplayText(NOT_KNOWN.name, vulnerabilities)),
    "none" to (getVulnerabilityDisplayText(NONE.name, vulnerabilities)),
    "domestic_abuse" to (getVulnerabilityDisplayText(DOMESTIC_ABUSE.name, vulnerabilities)),
    "drug_or_alcohol_use" to (getVulnerabilityDisplayText(DRUG_OR_ALCOHOL_USE.name, vulnerabilities)),
    "bullying_others" to (getVulnerabilityDisplayText(BULLYING_OTHERS.name, vulnerabilities)),
    "being_bullied_by_others" to (getVulnerabilityDisplayText(BEING_BULLIED_BY_OTHERS.name, vulnerabilities)),
    "being_at_risk_of_serious_harm_from_others" to (
      getVulnerabilityDisplayText(
        BEING_AT_RISK_OF_SERIOUS_HARM_FROM_OTHERS.name,
        vulnerabilities,
      )
      ),
    "adult_or_child_safeguarding_concerns" to (
      getVulnerabilityDisplayText(
        ADULT_OR_CHILD_SAFEGUARDING_CONCERNS.name,
        vulnerabilities,
      )
      ),
    "mental_health_concerns" to (getVulnerabilityDisplayText(MENTAL_HEALTH_CONCERNS.name, vulnerabilities)),
    "physical_health_concerns" to (getVulnerabilityDisplayText(PHYSICAL_HEALTH_CONCERNS.name, vulnerabilities)),
    "medication_taken_including_compliance_with_medication" to (
      getVulnerabilityDisplayText(
        MEDICATION_TAKEN_INCLUDING_COMPLIANCE_WITH_MEDICATION.name,
        vulnerabilities,
      )
      ),
    "bereavement_issues" to (getVulnerabilityDisplayText(BEREAVEMENT_ISSUES.name, vulnerabilities)),
    "learning_difficulties" to (getVulnerabilityDisplayText(LEARNING_DIFFICULTIES.name, vulnerabilities)),
    "physical_disabilities" to (getVulnerabilityDisplayText(PHYSICAL_DISABILITIES.name, vulnerabilities)),
    "cultural_or_language_differences" to (
      getVulnerabilityDisplayText(
        CULTURAL_OR_LANGUAGE_DIFFERENCES.name,
        vulnerabilities,
      )
      ),
  )

  private fun getVulnerabilityDisplayText(
    vulnerability: String?,
    vulnerabilities: VulnerabilitiesRecommendation?,
  ): String {
    val selectedVulnerabilities = vulnerabilities?.selected?.map { it.value }
    val displayTextMap = vulnerabilities?.allOptions?.associate { it.value to it.text }
    val detailsMap = vulnerabilities?.selected?.associate {
      if (it.value == NOT_KNOWN.name || it.value == NONE.name) {
        it.value to EMPTY_STRING
      } else {
        it.value to it.details
      }
    }

    return if (selectedVulnerabilities?.contains(vulnerability) == true) {
      "\n${"${displayTextMap?.get(vulnerability)!!}:"}\n${detailsMap?.get(vulnerability)}\n"
    } else {
      EMPTY_STRING
    }
  }

  private fun formatMappaCategory(mappa: Mappa?): String = if (mappa == null) {
    EMPTY_STRING
  } else if (mappa.category == null) {
    MrdTextConstants.NOT_APPLICABLE
  } else {
    "Category${MrdTextConstants.WHITE_SPACE}${(mappa.category)}"
  }

  private fun formatMappaLevel(mappa: Mappa?): String = if (mappa == null) {
    EMPTY_STRING
  } else if (mappa.level == null) {
    MrdTextConstants.NOT_APPLICABLE
  } else {
    "Level${MrdTextConstants.WHITE_SPACE}${(mappa.level)}"
  }
}
