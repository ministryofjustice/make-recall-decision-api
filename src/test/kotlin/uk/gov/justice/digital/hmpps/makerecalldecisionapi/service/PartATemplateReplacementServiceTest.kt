package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedAlternativeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedStandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.SelectedWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.StandardLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.UnderIntegratedOffenderManagement
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ValueWithDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.TICK_CHARACTER
import java.time.LocalDate

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
          hasVictimsInContactScheme = VictimsInContactScheme(selected = YesNoNotApplicableOptions.YES, allOptions = null),
          dateVloInformed = LocalDate.now(),
          alternativesToRecallTried = AlternativesToRecallTried(
            selected = listOf(ValueWithDetails(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, details = "We sent a warning letter on 27th July 2022")),
            allOptions = listOf(TextValueOption(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, text = "Warnings/licence breach letters"))
          ),
          hasArrestIssues = SelectedWithDetails(selected = true, details = "Has arrest issues"),
          licenceConditionsBreached = LicenceConditionsBreached(
            standardLicenceConditions = StandardLicenceConditions(
              selected = listOf(
                SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name,
                SelectedStandardLicenceConditions.NO_OFFENCE.name,
                SelectedStandardLicenceConditions.KEEP_IN_TOUCH.name,
                SelectedStandardLicenceConditions.SUPERVISING_OFFICER_VISIT.name,
                SelectedStandardLicenceConditions.NO_WORK_UNDERTAKEN.name,
                SelectedStandardLicenceConditions.NO_TRAVEL_OUTSIDE_UK.name
              )
            ),
            additionalLicenceConditions = AdditionalLicenceConditions(
              selected = listOf("NST14"),
              allOptions = listOf(
                AdditionalLicenceConditionOption(
                  subCatCode = "NST14",
                  mainCatCode = "NLC5",
                  title = "Additional title", details = "Additional details", note = "Additional note"
                )
              )
            )
          ),
          underIntegratedOffenderManagement = UnderIntegratedOffenderManagement(
            selected = "NOT_APPLICABLE",
            allOptions = listOf(TextValueOption(value = "YES", text = "Yes"), TextValueOption(value = "NO", text = "No"), TextValueOption(value = "NOT_APPLICABLE", text = "N/A"))
          ),
          localPoliceContact = LocalPoliceContact(contactName = "Thomas Magnum", phoneNumber = "555-0100", faxNumber = "555-0199", emailAddress = "thomas.magnum@gmail.com"),
          vulnerabilities = Vulnerabilities(
            selected = listOf(
              ValueWithDetails(value = RISK_OF_SUICIDE_OR_SELF_HARM.name, details = "Risk of suicide"),
              ValueWithDetails(value = RELATIONSHIP_BREAKDOWN.name, details = "Divorced"),
              ValueWithDetails(value = NOT_KNOWN.name, details = "Not known"),
//              ValueWithDetails(value = NONE.name, details = "None"),
              ValueWithDetails(value = DOMESTIC_ABUSE.name, details = "Victim of domestic violence"),
              ValueWithDetails(value = DRUG_OR_ALCOHOL_USE.name, details = "Has an alcohol dependency"),
              ValueWithDetails(value = BULLYING_OTHERS.name, details = "Bullying"),
              ValueWithDetails(value = BEING_BULLIED_BY_OTHERS.name, details = "Bullied"),
              ValueWithDetails(value = BEING_AT_RISK_OF_SERIOUS_HARM_FROM_OTHERS.name, details = "At risk of serious harm"),
              ValueWithDetails(value = ADULT_OR_CHILD_SAFEGUARDING_CONCERNS.name, details = "Safeguarding concerns"),
              ValueWithDetails(value = MENTAL_HEALTH_CONCERNS.name, details = "Depression and anxiety"),
              ValueWithDetails(value = PHYSICAL_HEALTH_CONCERNS.name, details = "Asthma"),
              ValueWithDetails(value = MEDICATION_TAKEN_INCLUDING_COMPLIANCE_WITH_MEDICATION.name, details = "Insulin"),
              ValueWithDetails(value = BEREAVEMENT_ISSUES.name, details = "Death in family"),
              ValueWithDetails(value = LEARNING_DIFFICULTIES.name, details = "ASD"),
              ValueWithDetails(value = PHYSICAL_DISABILITIES.name, details = "Leg injury"),
              ValueWithDetails(value = CULTURAL_OR_LANGUAGE_DIFFERENCES.name, details = "Jedi fundamentalist")
            ),
            allOptions = listOf(
              TextValueOption(value = RISK_OF_SUICIDE_OR_SELF_HARM.name, text = "Risk of suicide or self harm"),
              TextValueOption(value = RELATIONSHIP_BREAKDOWN.name, text = "Relationship breakdown"),
              TextValueOption(value = NOT_KNOWN.name, text = "Not known"),
              TextValueOption(value = NONE.name, text = "None"),
              TextValueOption(value = DOMESTIC_ABUSE.name, text = "Domestic abuse"),
              TextValueOption(value = DRUG_OR_ALCOHOL_USE.name, text = "Drug or alcohol use"),
              TextValueOption(value = BULLYING_OTHERS.name, text = "Bullying others"),
              TextValueOption(value = BEING_BULLIED_BY_OTHERS.name, text = "Being bullied by others"),
              TextValueOption(value = BEING_AT_RISK_OF_SERIOUS_HARM_FROM_OTHERS.name, text = "Being at risk of serious harm from others"),
              TextValueOption(value = ADULT_OR_CHILD_SAFEGUARDING_CONCERNS.name, text = "Adult or child safeguarding concerns"),
              TextValueOption(value = MENTAL_HEALTH_CONCERNS.name, text = "Mental health concerns"),
              TextValueOption(value = PHYSICAL_HEALTH_CONCERNS.name, text = "Physical health concerns"),
              TextValueOption(value = MEDICATION_TAKEN_INCLUDING_COMPLIANCE_WITH_MEDICATION.name, text = "Medication taken including compliance with medication"),
              TextValueOption(value = BEREAVEMENT_ISSUES.name, text = "Bereavement issues"),
              TextValueOption(value = LEARNING_DIFFICULTIES.name, text = "Learning difficulties"),
              TextValueOption(value = PHYSICAL_DISABILITIES.name, text = "Physical disabilities"),
              TextValueOption(value = CULTURAL_OR_LANGUAGE_DIFFERENCES.name, text = "Cultural or language differences")
            )
          )
        )
      )

      partATemplateReplacementService.generateDocFromTemplate(recommendation)
    }
  }

  @Test
  fun `given recommendation data then build the mappings for the Part A template`() {
    runTest {

      val alternativesList: List<ValueWithDetails> = listOf(
        ValueWithDetails(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, details = "We sent a warning letter on 27th July 2022"),
        ValueWithDetails(value = SelectedAlternativeOptions.DRUG_TESTING.name, details = "drugs test passed"),
        ValueWithDetails(value = SelectedAlternativeOptions.INCREASED_FREQUENCY.name, details = "increased frequency"),
        ValueWithDetails(value = SelectedAlternativeOptions.EXTRA_LICENCE_CONDITIONS.name, details = "licence conditions added"),
        ValueWithDetails(value = SelectedAlternativeOptions.REFERRAL_TO_APPROVED_PREMISES.name, details = "referred to approved premises"),
        ValueWithDetails(value = SelectedAlternativeOptions.REFERRAL_TO_OTHER_TEAMS.name, details = "referral to other team"),
        ValueWithDetails(value = SelectedAlternativeOptions.REFERRAL_TO_PARTNERSHIP_AGENCIES.name, details = "referred to partner agency"),
        ValueWithDetails(value = SelectedAlternativeOptions.RISK_ESCALATION.name, details = "risk escalation"),
        ValueWithDetails(value = SelectedAlternativeOptions.ALTERNATIVE_TO_RECALL_OTHER.name, details = "alternative action")
      )
      val partA = PartAData(
        custodyStatus = CustodyStatusValue.YES_POLICE.partADisplayValue,
        recallType = ValueWithDetails(value = RecallTypeValue.FIXED_TERM.displayValue, details = "My details"),
        responseToProbation = "They have not responded well",
        isThisAnEmergencyRecall = "Yes",
        hasVictimsInContactScheme = YesNoNotApplicableOptions.YES.partADisplayValue,
        dateVloInformed = "1 September 2022",
        selectedAlternatives = alternativesList,
        hasArrestIssues = ValueWithDetails(value = "Yes", details = "Arrest issue details"),
        selectedStandardConditionsBreached = listOf(
          SelectedStandardLicenceConditions.GOOD_BEHAVIOUR.name,
          SelectedStandardLicenceConditions.NO_OFFENCE.name,
          SelectedStandardLicenceConditions.KEEP_IN_TOUCH.name,
          SelectedStandardLicenceConditions.SUPERVISING_OFFICER_VISIT.name,
          SelectedStandardLicenceConditions.ADDRESS_APPROVED.name,
          SelectedStandardLicenceConditions.NO_WORK_UNDERTAKEN.name,
          SelectedStandardLicenceConditions.NO_TRAVEL_OUTSIDE_UK.name
        ),
        additionalConditionsBreached = "These are the additional conditions breached",
        isUnderIntegratedOffenderManagement = "YES",
        localPoliceContact = LocalPoliceContact(contactName = "Thomas Magnum", phoneNumber = "555-0100", faxNumber = "555-0199", emailAddress = "thomas.magnum@gmail.com"),
        vulnerabilities = Vulnerabilities(
          selected = listOf(ValueWithDetails(value = RISK_OF_SUICIDE_OR_SELF_HARM.name, details = "Risk of suicide")),
          allOptions = listOf(
            TextValueOption(value = RISK_OF_SUICIDE_OR_SELF_HARM.name, text = "Risk of suicide or self harm"),
            TextValueOption(value = RELATIONSHIP_BREAKDOWN.name, text = "Relationship breakdown")
          )
        )
      )

      val result = partATemplateReplacementService.mappingsForTemplate(partA)

      assertThat(result.size).isEqualTo(49)
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
      assertThat(result["has_arrest_issues"]).isEqualTo("Yes")
      assertThat(result["has_arrest_issues_details"]).isEqualTo("Arrest issue details")
      assertThat(result["good_behaviour_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["no_offence_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["keep_in_touch_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["officer_visit_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["address_approved_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["no_work_undertaken_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["no_travel_condition"]).isEqualTo(TICK_CHARACTER)
      assertThat(result["additional_conditions_breached"]).isEqualTo("These are the additional conditions breached")
      assertThat(result["is_under_integrated_offender_management"]).isEqualTo("Yes")
      assertThat(result["contact_name"]).isEqualTo("Thomas Magnum")
      assertThat(result["phone_number"]).isEqualTo("555-0100")
      assertThat(result["fax_number"]).isEqualTo("555-0199")
      assertThat(result["email_address"]).isEqualTo("thomas.magnum@gmail.com")
      assertThat(result["email_address"]).isEqualTo("thomas.magnum@gmail.com") // TODO - BS how do I handle checkboxes
    }
  }

  @Test
  fun `given empty some data then build the mappings with blank strings for the Part A template`() {
    runTest {

      val partA = PartAData(
        custodyStatus = CustodyStatusValue.YES_POLICE.partADisplayValue,
        recallType = ValueWithDetails(value = RecallTypeValue.FIXED_TERM.displayValue, details = "My details"),
        responseToProbation = "They have not responded well",
        isThisAnEmergencyRecall = "Yes",
        hasVictimsInContactScheme = YesNoNotApplicableOptions.YES.partADisplayValue,
        dateVloInformed = "1 September 2022",
        selectedAlternatives = listOf(),
        hasArrestIssues = ValueWithDetails(value = "Yes", details = "Arrest issue details"),
        selectedStandardConditionsBreached = null,
        additionalConditionsBreached = EMPTY_STRING
      )

      val result = partATemplateReplacementService.mappingsForTemplate(partA)

      assertThat(result["warning_letter_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["drug_testing_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["increased_frequency_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["extra_licence_conditions_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["referral_to_other_teams_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["referral_to_approved_premises_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["referral_to_partnership_agencies_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["risk_escalation_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["alternative_to_recall_other_details"]).isEqualTo(EMPTY_STRING)
      assertThat(result["good_behaviour_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["no_offence_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["keep_in_touch_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["officer_visit_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["address_approved_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["no_work_undertaken_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["no_travel_condition"]).isEqualTo(EMPTY_STRING)
      assertThat(result["additional_conditions_breached"]).isEqualTo(EMPTY_STRING)
    }
  }
}
