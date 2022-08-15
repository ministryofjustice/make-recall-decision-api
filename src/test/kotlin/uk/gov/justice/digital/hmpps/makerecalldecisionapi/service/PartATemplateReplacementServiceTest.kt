package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypePartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternative
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactSchemeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
<<<<<<< HEAD
import java.time.LocalDate
=======
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
>>>>>>> MRD-458_alternative-to-recall: Removed redundant RecallAllternative class.

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PartATemplateReplacementServiceTest : ServiceTestBase() {

  @Test
  fun `given recommendation data then build the part A document`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          custodyStatus = CustodyStatus(selected = CustodyStatusValue.YES_POLICE, allOptions = null),
          recallType = RecallType(selected = RecallTypeSelectedValue(value = RecallTypeValue.FIXED_TERM, details = "My details"), allOptions = null),
          responseToProbation = "They did not respond well",
          isThisAnEmergencyRecall = true,

          hasVictimsInContactScheme = VictimsInContactScheme(selected = VictimsInContactSchemeValue.YES, allOptions = null),
          dateVloInformed = LocalDate.now(),
          alternativesToRecallTried = AlternativesToRecallTried(
            selected = listOf(SelectedAlternative(value = "WARNINGS_LETTER", details = "We sent a warning letter on 27th July 2022")),
<<<<<<< HEAD
            allOptions = listOf(RecallAlternative(value = "WARNINGS_LETTER", text = "Warnings/licence breach letters"))
          )
=======
            allOptions = listOf(TextValueOption(value = "WARNINGS_LETTER", text = "Warnings/licence breach letters"))
          ),
          isThisAnEmergencyRecall = true
>>>>>>> MRD-458_alternative-to-recall: Removed redundant RecallAllternative class.
        )
      )

      partATemplateReplacementService.generateDocFromTemplate(recommendation)
    }
  }

  @Test
  fun `given recommendation data then build the mappings for the template`() {
    runTest {
      val partA = PartAData(
        custodyStatus = CustodyStatusValue.YES_POLICE.partADisplayValue,
        recallType = RecallTypePartA(value = RecallTypeValue.FIXED_TERM.displayValue, details = "My details"),
        responseToProbation = "They have not responded well",
        isThisAnEmergencyRecall = "Yes",
        hasVictimsInContactScheme = VictimsInContactSchemeValue.YES.partADisplayValue,
        dateVloInformed = "1 September 2022",
        selectedAlternativesMap = mapOf(
          "warning_letter_details" to "We sent a warning letter on 27th July 2022",
          "drug_testing_details" to "drugs test passed",
          "increased_frequency_details" to "increased frequency",
          "extra_licence_conditions_details" to "licence conditions added",
          "referral_to_other_teams_details" to "referral to other team",
          "referral_to_approved_premises_details" to "referred to approved premises",
          "referral_to_partnership_agencies_details" to "referred to partner agency",
          "risk_escalation_details" to "risk escalation",
          "alternative_to_recall_other_details" to "alternative action"
        )
      )

      val result = partATemplateReplacementService.mappingsForTemplate(partA)

      assertThat(result.size).isEqualTo(16)
      assertThat(result["custody_status"]).isEqualTo("Police Custody")
      assertThat(result["recall_type"]).isEqualTo("Fixed")
      assertThat(result["recall_type_details"]).isEqualTo("My details")
      assertThat(result["response_to_probation"]).isEqualTo("They have not responded well")
      assertThat(result["is_this_an_emergency_recall"]).isEqualTo("Yes")
      assertThat(result["has_victims_in_contact_scheme"]).isEqualTo("Yes")
      assertThat(result["date_vlo_informed"]).isEqualTo("1 September 2022")
      assertThat(result["warning_letter_details"]).isEqualTo("We sent a warning letter on 27th July 2022")
      assertThat(result["drug_testing_details"]).isEqualTo("drugs test passed")
      assertThat(result["increased_frequency_details"]).isEqualTo("increased frequency")
      assertThat(result["extra_licence_conditions_details"]).isEqualTo("licence conditions added")
      assertThat(result["referral_to_other_teams_details"]).isEqualTo("referral to other team")
      assertThat(result["referral_to_approved_premises_details"]).isEqualTo("referred to approved premises")
      assertThat(result["referral_to_partnership_agencies_details"]).isEqualTo("referred to partner agency")
      assertThat(result["risk_escalation_details"]).isEqualTo("risk escalation")
      assertThat(result["alternative_to_recall_other_details"]).isEqualTo("alternative action")
    }
  }
}
