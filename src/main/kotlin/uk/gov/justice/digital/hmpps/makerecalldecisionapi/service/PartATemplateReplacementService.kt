package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.deepoove.poi.XWPFTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.Vulnerabilities
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NO
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.NOT_SPECIFIED
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.TICK_CHARACTER
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.YES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.RecommendationToPartADataMapper
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.util.Base64

@Service
internal class PartATemplateReplacementService {
  val resource = ClassPathResource("NAT Recall Part A London Template - obtained 131021.docx")

  fun generateDocFromTemplate(recommendation: RecommendationEntity): String {

    val partAData = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

    val file = XWPFTemplate.compile(resource.inputStream).render(
      mappingsForTemplate(partAData)
    )
//    ).writeToFile("out_template.docx")

    val out = ByteArrayOutputStream()
    file.write(out)
    file.close()

    return Base64.getEncoder().encodeToString(out.toByteArray())
  }

  fun mappingsForTemplate(partAData: PartAData): HashMap<String, String?> {
    val mappings = hashMapOf(
      "custody_status" to partAData.custodyStatus?.value,
      "custody_status_details" to partAData.custodyStatus?.details?.replace("\r\n", ", "),
      "recall_type" to partAData.recallType?.value,
      "recall_type_details" to partAData.recallType?.details,
      "response_to_probation" to partAData.responseToProbation,
      "what_led_to_recall" to partAData.whatLedToRecall,
      "is_this_an_emergency_recall" to partAData.isThisAnEmergencyRecall,
      "has_victims_in_contact_scheme" to partAData.hasVictimsInContactScheme,
      "indeterminate_sentence_type" to partAData.indeterminateSentenceType,
      "is_extended_sentence" to partAData.isExtendedSentence,
      "date_vlo_informed" to partAData.dateVloInformed,
      "has_arrest_issues" to partAData.hasArrestIssues?.value,
      "has_arrest_issues_details" to partAData.hasArrestIssues?.details,
      "has_contraband_risk" to partAData.hasContrabandRisk?.value,
      "has_contraband_risk_details" to partAData.hasContrabandRisk?.details,
      "additional_conditions_breached" to partAData.additionalConditionsBreached,
      "is_under_integrated_offender_management" to partAData.isUnderIntegratedOffenderManagement?.let { YesNoNotApplicableOptions.valueOf(it).partADisplayValue },
      "contact_name" to partAData.localPoliceContact?.contactName,
      "phone_number" to partAData.localPoliceContact?.phoneNumber,
      "fax_number" to partAData.localPoliceContact?.faxNumber,
      "email_address" to partAData.localPoliceContact?.emailAddress,
      "has_vulnerabilities" to if (hasVulnerabilities(partAData)) YES else NO,
      "gender" to partAData.gender,
      "date_of_birth" to partAData.dateOfBirth?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
      "name" to partAData.name,
      "ethnicity" to if (partAData.ethnicity.isNullOrBlank()) NOT_SPECIFIED else partAData.ethnicity,
      "last_recorded_address" to partAData.lastRecordedAddress,
      "no_fixed_abode" to partAData.noFixedAbode,
      "cro_number" to partAData.croNumber,
      "pnc_number" to partAData.pncNumber,
      "most_recent_prisoner_number" to partAData.mostRecentPrisonerNumber,
      "noms_number" to partAData.nomsNumber,
      "gender" to partAData.gender,
      "index_offence_description" to partAData.indexOffenceDescription,
      "date_of_original_offence" to partAData.dateOfOriginalOffence,
      "date_of_sentence" to partAData.dateOfSentence,
      "length_of_sentence" to partAData.lengthOfSentence,
      "licence_expiry_date" to partAData.licenceExpiryDate,
      "sentence_expiry_date" to partAData.sentenceExpiryDate,
      "custodial_term" to partAData.custodialTerm,
      "extended_term" to partAData.extendedTerm,
      "mappa_level" to formatMappaLevel(partAData.mappa),
      "mappa_category" to formatMappaCategory(partAData.mappa),
      "last_person_completing_form_name" to partAData.lastPersonCompletingFormName,
      "last_person_completing_form_email" to partAData.lastPersonCompletingFormEmail,
      "region" to partAData.region,
      "local_delivery_unit" to partAData.localDeliveryUnit,
      "date_of_decision" to partAData.dateOfDecision,
      "time_of_decision" to partAData.timeOfDecision,
    )

    mappings.putAll(convertToSelectedAlternativesMap(partAData.selectedAlternatives))
    mappings.putAll(convertToSelectedStandardConditionsBreachedMap(partAData.selectedStandardConditionsBreached))
    mappings.putAll(convertToSelectedVulnerabilitiesMap(partAData.vulnerabilities))

    return mappings
  }

  private fun hasVulnerabilities(partAData: PartAData) =
    partAData.vulnerabilities?.selected?.isNotEmpty() == true && !partAData.vulnerabilities.selected.any { it.value == NONE.name || it.value == NOT_KNOWN.name }

  private fun convertToSelectedAlternativesMap(selectedAlternatives: List<ValueWithDetails>?): HashMap<String, String> {
    val selectedAlternativesMap = selectedAlternatives?.associate { it.value to it.details } ?: emptyMap()
    return hashMapOf(
      "warning_letter_details" to (selectedAlternativesMap[SelectedAlternativeOptions.WARNINGS_LETTER.name] ?: EMPTY_STRING),
      "drug_testing_details" to (selectedAlternativesMap[SelectedAlternativeOptions.DRUG_TESTING.name] ?: EMPTY_STRING),
      "increased_frequency_details" to (selectedAlternativesMap[SelectedAlternativeOptions.INCREASED_FREQUENCY.name] ?: EMPTY_STRING),
      "extra_licence_conditions_details" to (selectedAlternativesMap[SelectedAlternativeOptions.EXTRA_LICENCE_CONDITIONS.name] ?: EMPTY_STRING),
      "referral_to_approved_premises_details" to (selectedAlternativesMap[SelectedAlternativeOptions.REFERRAL_TO_APPROVED_PREMISES.name] ?: EMPTY_STRING),
      "referral_to_other_teams_details" to (selectedAlternativesMap[SelectedAlternativeOptions.REFERRAL_TO_OTHER_TEAMS.name] ?: EMPTY_STRING),
      "referral_to_partnership_agencies_details" to (selectedAlternativesMap[SelectedAlternativeOptions.REFERRAL_TO_PARTNERSHIP_AGENCIES.name] ?: EMPTY_STRING),
      "risk_escalation_details" to (selectedAlternativesMap[SelectedAlternativeOptions.RISK_ESCALATION.name] ?: EMPTY_STRING),
      "alternative_to_recall_other_details" to (selectedAlternativesMap[SelectedAlternativeOptions.ALTERNATIVE_TO_RECALL_OTHER.name] ?: EMPTY_STRING)
    )
  }

  private fun convertToSelectedStandardConditionsBreachedMap(selectedConditions: List<String>?): Map<String, String> {
    return mapOf(
      "good_behaviour_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name) == true) TICK_CHARACTER else EMPTY_STRING),
      "no_offence_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NO_OFFENCE.name) == true) TICK_CHARACTER else EMPTY_STRING),
      "keep_in_touch_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.KEEP_IN_TOUCH.name) == true) TICK_CHARACTER else EMPTY_STRING),
      "officer_visit_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.SUPERVISING_OFFICER_VISIT.name) == true) TICK_CHARACTER else EMPTY_STRING),
      "address_approved_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.ADDRESS_APPROVED.name) == true) TICK_CHARACTER else EMPTY_STRING),
      "no_work_undertaken_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NO_WORK_UNDERTAKEN.name) == true) TICK_CHARACTER else EMPTY_STRING),
      "no_travel_condition" to (if (selectedConditions?.contains(SelectedStandardLicenceConditions.NO_TRAVEL_OUTSIDE_UK.name) == true) TICK_CHARACTER else EMPTY_STRING)
    )
  }

  private fun convertToSelectedVulnerabilitiesMap(vulnerabilities: Vulnerabilities?): Map<String, String> {
    return mapOf(
      "risk_of_suicide_or_self_harm" to (getVulnerabilityDisplayText(RISK_OF_SUICIDE_OR_SELF_HARM.name, vulnerabilities)),
      "relationship_breakdown" to (getVulnerabilityDisplayText(RELATIONSHIP_BREAKDOWN.name, vulnerabilities)),
      "not_known" to (getVulnerabilityDisplayText(NOT_KNOWN.name, vulnerabilities)),
      "none" to (getVulnerabilityDisplayText(NONE.name, vulnerabilities)),
      "domestic_abuse" to (getVulnerabilityDisplayText(DOMESTIC_ABUSE.name, vulnerabilities)),
      "drug_or_alcohol_use" to (getVulnerabilityDisplayText(DRUG_OR_ALCOHOL_USE.name, vulnerabilities)),
      "bullying_others" to (getVulnerabilityDisplayText(BULLYING_OTHERS.name, vulnerabilities)),
      "being_bullied_by_others" to (getVulnerabilityDisplayText(BEING_BULLIED_BY_OTHERS.name, vulnerabilities)),
      "being_at_risk_of_serious_harm_from_others" to (getVulnerabilityDisplayText(BEING_AT_RISK_OF_SERIOUS_HARM_FROM_OTHERS.name, vulnerabilities)),
      "adult_or_child_safeguarding_concerns" to (getVulnerabilityDisplayText(ADULT_OR_CHILD_SAFEGUARDING_CONCERNS.name, vulnerabilities)),
      "mental_health_concerns" to (getVulnerabilityDisplayText(MENTAL_HEALTH_CONCERNS.name, vulnerabilities)),
      "physical_health_concerns" to (getVulnerabilityDisplayText(PHYSICAL_HEALTH_CONCERNS.name, vulnerabilities)),
      "medication_taken_including_compliance_with_medication" to (getVulnerabilityDisplayText(MEDICATION_TAKEN_INCLUDING_COMPLIANCE_WITH_MEDICATION.name, vulnerabilities)),
      "bereavement_issues" to (getVulnerabilityDisplayText(BEREAVEMENT_ISSUES.name, vulnerabilities)),
      "learning_difficulties" to (getVulnerabilityDisplayText(LEARNING_DIFFICULTIES.name, vulnerabilities)),
      "physical_disabilities" to (getVulnerabilityDisplayText(PHYSICAL_DISABILITIES.name, vulnerabilities)),
      "cultural_or_language_differences" to (getVulnerabilityDisplayText(CULTURAL_OR_LANGUAGE_DIFFERENCES.name, vulnerabilities))
    )
  }

  private fun getVulnerabilityDisplayText(vulnerability: String?, vulnerabilities: Vulnerabilities?): String {
    val selectedVulnerabilities = vulnerabilities?.selected?.map { it.value }
    val displayTextMap = vulnerabilities?.allOptions?.associate { it.value to it.text }
    val detailsMap = vulnerabilities?.selected?.associate {
      if (it.value == NOT_KNOWN.name || it.value == NONE.name) {
        it.value to EMPTY_STRING
      } else it.value to it.details
    }

    return if (selectedVulnerabilities?.contains(vulnerability) == true) {
      "\n${"${displayTextMap?.get(vulnerability)!!}:"}\n${detailsMap?.get(vulnerability)}\n"
    } else {
      EMPTY_STRING
    }
  }

  private fun formatMappaCategory(mappa: Mappa?): String {
    return if (mappa == null) EMPTY_STRING
    else if (mappa.category == null) MrdTextConstants.NOT_APPLICABLE
    else "Category${MrdTextConstants.WHITE_SPACE}${(mappa.category)}"
  }

  private fun formatMappaLevel(mappa: Mappa?): String {
    return if (mappa == null) EMPTY_STRING
    else if (mappa.level == null) MrdTextConstants.NOT_APPLICABLE
    else "Level${MrdTextConstants.WHITE_SPACE}${(mappa.level)}"
  }
}
