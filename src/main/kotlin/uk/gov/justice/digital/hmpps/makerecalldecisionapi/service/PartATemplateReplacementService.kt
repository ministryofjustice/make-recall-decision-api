package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.deepoove.poi.XWPFTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.TICK_CHARACTER
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.RecommendationToPartADataMapper
import java.io.ByteArrayOutputStream
import java.util.Base64

@Service
internal class PartATemplateReplacementService {
  val resource = ClassPathResource("NAT Recall Part A London Template - obtained 131021.docx")

  fun generateDocFromTemplate(recommendation: RecommendationEntity): String {

    val partAData = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

    val file = XWPFTemplate.compile(resource.inputStream).render(
      mappingsForTemplate(partAData)
    )
    // ).writeToFile("out_template.docx")

    val out = ByteArrayOutputStream()
    file.write(out)
    file.close()

    return Base64.getEncoder().encodeToString(out.toByteArray())
  }

  fun mappingsForTemplate(partAData: PartAData): HashMap<String, String?> {
    val mappings = hashMapOf(
      "custody_status" to partAData.custodyStatus,
      "recall_type" to partAData.recallType?.value,
      "recall_type_details" to partAData.recallType?.details,
      "response_to_probation" to partAData.responseToProbation,
      "is_this_an_emergency_recall" to partAData.isThisAnEmergencyRecall,
      "has_victims_in_contact_scheme" to partAData.hasVictimsInContactScheme,
      "date_vlo_informed" to partAData.dateVloInformed,
      "has_arrest_issues" to partAData.hasArrestIssues?.value,
      "has_arrest_issues_details" to partAData.hasArrestIssues?.details,
      "additional_conditions_breached" to partAData.additionalConditionsBreached,
      "is_under_integrated_offender_management" to partAData.isUnderIntegratedOffenderManagement?.value,
      // FIXME: this might need to be removed based on the outcome of the questions raised in comments on MRD-464. There is no place in the Part A to put the IOM details
      "is_under_integrated_offender_management_details" to partAData.isUnderIntegratedOffenderManagement?.details,
    )

    mappings.putAll(convertToSelectedAlternativesMap(partAData.selectedAlternatives))
    mappings.putAll(convertToSelectedStandardConditionsBreachedMap(partAData.selectedStandardConditionsBreached))

    return mappings
  }

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
}
