package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.deepoove.poi.XWPFTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
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
    return hashMapOf(
      "custody_status" to partAData.custodyStatus,
      "recall_type" to partAData.recallType.value,
      "recall_type_details" to partAData.recallType.details,
      "response_to_probation" to partAData.responseToProbation,
      "is_this_an_emergency_recall" to partAData.isThisAnEmergencyRecall,
      "has_victims_in_contact_scheme" to partAData.hasVictimsInContactScheme,
      "date_vlo_informed" to partAData.dateVloInformed,
      "warning_letter_details" to partAData.selectedAlternativesMap["warning_letter_details"],
      "drug_testing_details" to partAData.selectedAlternativesMap["drug_testing_details"],
      "increased_frequency_details" to partAData.selectedAlternativesMap["increased_frequency_details"],
      "extra_licence_conditions_details" to partAData.selectedAlternativesMap["extra_licence_conditions_details"],
      "referral_to_other_teams_details" to partAData.selectedAlternativesMap["referral_to_other_teams_details"],
      "referral_to_approved_premises_details" to partAData.selectedAlternativesMap["referral_to_approved_premises_details"],
      "referral_to_partnership_agencies_details" to partAData.selectedAlternativesMap["referral_to_partnership_agencies_details"],
      "risk_escalation_details" to partAData.selectedAlternativesMap["risk_escalation_details"],
      "alternative_to_recall_other_details" to partAData.selectedAlternativesMap["alternative_to_recall_other_details"]
    )
  }
}
