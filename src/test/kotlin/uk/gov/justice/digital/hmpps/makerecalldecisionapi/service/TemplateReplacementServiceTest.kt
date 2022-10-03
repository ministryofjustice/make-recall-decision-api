package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.DecisionNotToRecallLetterDocumentMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.PartADocumentMapper
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.Mappa
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditionOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AdditionalLicenceConditions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.AlternativesToRecallTried
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ConvictionDetail
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.HowWillAppointmentHappen
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateOrExtendedSentenceDetailsOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.IndeterminateSentenceTypeOptions
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LicenceConditionsBreached
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.LocalPoliceContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointmentValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ReasonsForNoRecall
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
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.YesNoNotApplicableOptions.YES
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.TICK_CHARACTER
import java.time.LocalDate.now
import java.time.LocalDate.parse
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class TemplateReplacementServiceTest : ServiceTestBase() {

  @Mock
  private lateinit var partADocumentMapperMocked: PartADocumentMapper

  @Mock
  private lateinit var decisionNotToRecallLetterDocumentMapperMocked: DecisionNotToRecallLetterDocumentMapper

  @ParameterizedTest()
  @CsvSource(
    "PART_A_DOCUMENT",
    "DNTR_DOCUMENT"
  )
  fun `given recommendation data then build the document`(documentType: DocumentType) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          indexOffenceDetails = "Juicy details!",
          custodyStatus = CustodyStatus(selected = CustodyStatusValue.YES_POLICE, details = "Bromsgrove Police Station\r\nLondon", allOptions = null),
          recallType = RecallType(selected = RecallTypeSelectedValue(value = RecallTypeValue.FIXED_TERM, details = "My details"), allOptions = null),
          responseToProbation = "They did not respond well",
          whatLedToRecall = "Increasingly violent behaviour",
          isThisAnEmergencyRecall = true,
          isIndeterminateSentence = false,
          isExtendedSentence = false,
          personOnProbation = PersonOnProbation(
            gender = "Male",
            dateOfBirth = parse("1982-10-24"),
            firstName = "Homer",
            middleNames = "Bart",
            surname = "Simpson",
            ethnicity = "Ainu",
            croNumber = "123456/04A",
            pncNumber = "2004/0712343H",
            mostRecentPrisonerNumber = "G12345",
            nomsNumber = "A1234CR",
            mappa = Mappa(level = null, category = null, isNominal = null, lastUpdated = null),
            addresses = listOf(
              Address(
                line1 = "Address line 1",
                line2 = "Address line 2",
                town = "Address line town",
                postcode = "TS1 1ST",
                noFixedAbode = false,
              )
            )
          ),
          hasVictimsInContactScheme = VictimsInContactScheme(selected = YES, allOptions = null),
          indeterminateSentenceType = IndeterminateSentenceType(selected = IndeterminateSentenceTypeOptions.LIFE, allOptions = null),
          dateVloInformed = now(),
          alternativesToRecallTried = AlternativesToRecallTried(
            selected = listOf(ValueWithDetails(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, details = "We sent a warning letter on 27th July 2022")),
            allOptions = listOf(TextValueOption(value = SelectedAlternativeOptions.WARNINGS_LETTER.name, text = "Warnings/licence breach letters"))
          ),
          hasArrestIssues = SelectedWithDetails(selected = true, details = "Has arrest issues"),
          hasContrabandRisk = SelectedWithDetails(selected = true, details = "Has contraband risk"),
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
              ValueWithDetails(value = NOT_KNOWN.name, details = null),
              ValueWithDetails(value = NONE.name, details = null),
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
          ),
          convictionDetail = ConvictionDetail(
            indexOffenceDescription = "Armed robbery",
            dateOfOriginalOffence = parse("2022-09-01"),
            dateOfSentence = parse("2022-09-05"),
            lengthOfSentence = 6,
            lengthOfSentenceUnits = "days",
            sentenceDescription = "Extended Determinate Sentence",
            licenceExpiryDate = parse("2022-09-06"),
            sentenceExpiryDate = parse("2022-09-07"),
            sentenceSecondLength = 20,
            sentenceSecondLengthUnits = "days"
          ),
          userNamePartACompletedBy = "Henry Richarlison",
          userEmailPartACompletedBy = "Henry.Richarlison@test.com",
          region = "NPS London",
          localDeliveryUnit = "All NPS London",
          lastPartADownloadDateTime = LocalDateTime.now(),
          fixedTermAdditionalLicenceConditions = SelectedWithDetails(selected = true, "This is an additional licence condition"),
          indeterminateOrExtendedSentenceDetails = IndeterminateOrExtendedSentenceDetails(
            selected = listOf(
              ValueWithDetails(value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name, details = "behaviour similar to index offence"),
              ValueWithDetails(value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE.name, details = "behaviour leading to sexual or violent behaviour"),
              ValueWithDetails(value = IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH.name, details = "out of touch")
            ),
            allOptions = listOf(
              TextValueOption(value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_SIMILAR_TO_INDEX_OFFENCE.name, text = "behaviour similar to index offence"),
              TextValueOption(value = IndeterminateOrExtendedSentenceDetailsOptions.BEHAVIOUR_LEADING_TO_SEXUAL_OR_VIOLENT_OFFENCE.name, text = "behaviour leading to sexual or violent behaviour"),
              TextValueOption(value = IndeterminateOrExtendedSentenceDetailsOptions.OUT_OF_TOUCH.name, text = "out of touch")
            )
          ),
          mainAddressWherePersonCanBeFound = SelectedWithDetails(selected = false, "123 Acacia Avenue, Birmingham, B23 1AV"),
          whyConsideredRecall = WhyConsideredRecall(
            selected = WhyConsideredRecallValue.RISK_INCREASED,
            allOptions = listOf(
              TextValueOption(value = "RISK_INCREASED", text = "Your risk is assessed as increased"),
              TextValueOption(value = "CONTACT_STOPPED", text = "Contact with your probation practitioner has broken down"),
              TextValueOption(value = "RISK_INCREASED_AND_CONTACT_STOPPED", text = "Your risk is assessed as increased and contact with your probation practitioner has broken down")
            )
          ),
          reasonsForNoRecall = ReasonsForNoRecall(licenceBreach = "Reason for breaching licence", noRecallRationale = "Rationale for no recall", popProgressMade = "Progress made so far detail", futureExpectations = "Future expectations detail"),
          nextAppointment = NextAppointment(
            HowWillAppointmentHappen(
              selected = NextAppointmentValue.TELEPHONE,
              allOptions = listOf(
                TextValueOption(text = "Telephone", value = "TELEPHONE"),
                TextValueOption(text = "Video call", value = "VIDEO_CALL"),
                TextValueOption(text = "Office visit", value = "OFFICE_VISIT"),
                TextValueOption(text = "Home visit", value = "HOME_VISIT")
              )
            ),
            dateTimeOfAppointment = "2022-04-24T20:39:00.000Z",
            probationPhoneNumber = "01238282838"
          )
        )
      )
      templateReplacementService.generateDocFromRecommendation(recommendation, documentType)
    }
  }

  @Test
  fun `given recommendation data then build a preview of the DNTR document`() {
    runTest {
      val recommendation = RecommendationEntity(data = RecommendationModel(crn = "123456"))

      val documentData = DocumentData(
        salutation = "Dear John Smith",
        letterTitle = "Decision not to recall",
        letterDate = "12/09/2022",
        letterAddress = "My address",
        section1 = "Section 1",
        section2 = "Section 2",
        section3 = "Section 3",
        signedByParagraph = "Signed by"
      )

      BDDMockito.given(decisionNotToRecallLetterDocumentMapperMocked.mapRecommendationDataToDocumentData(recommendation))
        .willReturn(documentData)

      val letterContent = TemplateReplacementService(partADocumentMapperMocked, decisionNotToRecallLetterDocumentMapperMocked).generateLetterContentForPreviewFromRecommendation(recommendation)

      assertThat(letterContent.letterAddress).isEqualTo("My address")
      assertThat(letterContent.letterDate).isEqualTo("12/09/2022")
      assertThat(letterContent.salutation).isEqualTo("Dear John Smith")
      assertThat(letterContent.letterTitle).isEqualTo("Decision not to recall")
      assertThat(letterContent.section1).isEqualTo("Section 1")
      assertThat(letterContent.section2).isEqualTo("Section 2")
      assertThat(letterContent.section3).isEqualTo("Section 3")
      assertThat(letterContent.signedByParagraph).isEqualTo("Signed by")
    }
  }

  @Test
  fun `given recommendation data then build the mappings for the document template`() {
    runTest {
      // given
      val document = documentData()

      // when
      val result = templateReplacementService.mappingsForTemplate(document)

      // then
      assertThat(result.size).isEqualTo(98)
      assertThat(result["custody_status"]).isEqualTo("Police Custody")
      assertThat(result["custody_status_details"]).isEqualTo("Bromsgrove Police Station, London")
      assertThat(result["recall_type"]).isEqualTo("Fixed")
      assertThat(result["recall_type_details"]).isEqualTo("My details")
      assertThat(result["response_to_probation"]).isEqualTo("They have not responded well")
      assertThat(result["what_led_to_recall"]).isEqualTo("Increasingly violent behaviour")
      assertThat(result["is_this_an_emergency_recall"]).isEqualTo("Yes")
      assertThat(result["is_extended_sentence"]).isEqualTo("Yes")
      assertThat(result["has_victims_in_contact_scheme"]).isEqualTo("Yes")
      assertThat(result["indeterminate_sentence_type"]).isEqualTo("Yes - Lifer")
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
      assertThat(result["has_contraband_risk"]).isEqualTo("Yes")
      assertThat(result["has_contraband_risk_details"]).isEqualTo("Contraband risk details")
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
      assertThat(result["gender"]).isEqualTo("Male")
      assertThat(result["name"]).isEqualTo("Homer Bart Simpson")
      assertThat(result["date_of_birth"]).isEqualTo("24/10/1982")
      assertThat(result["ethnicity"]).isEqualTo("Ainu")
      assertThat(result["cro_number"]).isEqualTo("123456/04A")
      assertThat(result["pnc_number"]).isEqualTo("2004/0712343H")
      assertThat(result["most_recent_prisoner_number"]).isEqualTo("G12345")
      assertThat(result["noms_number"]).isEqualTo("A1234CR")
      assertThat(result["index_offence_description"]).isEqualTo("Armed robbery")
      assertThat(result["date_of_original_offence"]).isEqualTo("01/09/2022")
      assertThat(result["date_of_sentence"]).isEqualTo("05/09/2022")
      assertThat(result["length_of_sentence"]).isEqualTo("6 days")
      assertThat(result["licence_expiry_date"]).isEqualTo("06/09/2022")
      assertThat(result["sentence_expiry_date"]).isEqualTo("07/09/2022")
      assertThat(result["custodial_term"]).isEqualTo("6 days")
      assertThat(result["extended_term"]).isEqualTo("20 days")
      assertThat(result["mappa_level"]).isEqualTo("Level 1")
      assertThat(result["mappa_category"]).isEqualTo("Category 1")
      assertThat(result["last_recorded_address"]).isEqualTo("Address line 1, Address line 2, My town, TS1 1ST")
      assertThat(result["no_fixed_abode"]).isEqualTo(EMPTY_STRING)
      assertThat(result["last_person_completing_form_name"]).isEqualTo("Henry Richarlison")
      assertThat(result["last_person_completing_form_email"]).isEqualTo("Henry.Richarlison@test.com")
      assertThat(result["region"]).isEqualTo("NPS London")
      assertThat(result["local_delivery_unit"]).isEqualTo("All NPS London")
      assertThat(result["date_of_decision"]).isEqualTo("13/09/2022")
      assertThat(result["time_of_decision"]).isEqualTo("08:26")
      assertThat(result["index_offence_details"]).isEqualTo("Juicy details!")
      assertThat(result["fixed_term_additional_licence_conditions"]).isEqualTo("This is an additional licence condition")
      assertThat(result["behaviour_similar_to_index_offence"]).isEqualTo("behavior similar to index offence")
      assertThat(result["behaviour_similar_to_index_offence_present"]).isEqualTo(YES.partADisplayValue)
      assertThat(result["behaviour_leading_to_sexual_or_violent_offence"]).isEqualTo("behaviour leading to sexual or violent offence")
      assertThat(result["behaviour_leading_to_sexual_or_violent_offence_present"]).isEqualTo(YES.partADisplayValue)
      assertThat(result["out_of_touch"]).isEqualTo("out of touch")
      assertThat(result["out_of_touch_present"]).isEqualTo(YES.partADisplayValue)
      assertThat(result["other_possible_addresses"]).isEqualTo("123 Acacia Avenue, Birmingham, B23 1AV")
      assertThat(result["salutation"]).isEqualTo("Dear Duncan Edwards")
      assertThat(result["letter_address"]).isEqualTo("123 Acacia Avenue, Birmingham B23 1AB")
      assertThat(result["letter_title"]).isEqualTo("DECISION NOT TO RECALL")
      assertThat(result["letter_date"]).isEqualTo("27/09/2022")
      assertThat(result["section_1"]).isEqualTo("This is the first paragraph")
      assertThat(result["section_2"]).isEqualTo("This is the second paragraph")
      assertThat(result["section_3"]).isEqualTo("This is the third paragraph")
      assertThat(result["letter_signed_by_paragraph"]).isEqualTo("Yours faithfully, Jim Smith")
    }
  }

  @Test
  fun `ethnicity in Part A should read 'Not Specified' when not available from Delius`() {
    runTest {
      // given
      val partA = documentData().copy(ethnicity = null)

      // when
      val result = templateReplacementService.mappingsForTemplate(partA)

      // then
      assertThat(result["ethnicity"]).isEqualTo("Not specified")
    }
  }

  @Test
  fun `given empty some data then build the mappings with blank strings for the Part A template`() {
    runTest {

      val partA = DocumentData(
        custodyStatus = ValueWithDetails(value = CustodyStatusValue.YES_POLICE.partADisplayValue, details = "Bromsgrove Police Station\r\nLondon"),
        recallType = ValueWithDetails(value = RecallTypeValue.FIXED_TERM.displayValue, details = "My details"),
        responseToProbation = "They have not responded well",
        whatLedToRecall = "Increasingly violent behaviour",
        isThisAnEmergencyRecall = "Yes",
        isExtendedSentence = "Yes",
        hasVictimsInContactScheme = YES.partADisplayValue,
        indeterminateSentenceType = IndeterminateSentenceTypeOptions.LIFE.partADisplayValue,
        dateVloInformed = "1 September 2022",
        selectedAlternatives = listOf(),
        hasArrestIssues = ValueWithDetails(value = "Yes", details = "Arrest issue details"),
        hasContrabandRisk = ValueWithDetails(value = "Yes", details = "Contraband risk details"),
        selectedStandardConditionsBreached = null,
        additionalConditionsBreached = EMPTY_STRING
      )

      val result = templateReplacementService.mappingsForTemplate(partA)

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

  private fun documentData(): DocumentData {
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
    return DocumentData(
      salutation = "Dear Duncan Edwards",
      letterTitle = "DECISION NOT TO RECALL",
      letterDate = "27/09/2022",
      section1 = "This is the first paragraph",
      section2 = "This is the second paragraph",
      section3 = "This is the third paragraph",
      signedByParagraph = "Yours faithfully, Jim Smith",
      indexOffenceDetails = "Juicy details!",
      custodyStatus = ValueWithDetails(value = CustodyStatusValue.YES_POLICE.partADisplayValue, details = "Bromsgrove Police Station\r\nLondon"),
      recallType = ValueWithDetails(value = RecallTypeValue.FIXED_TERM.displayValue, details = "My details"),
      responseToProbation = "They have not responded well",
      whatLedToRecall = "Increasingly violent behaviour",
      isThisAnEmergencyRecall = "Yes",
      isExtendedSentence = "Yes",
      hasVictimsInContactScheme = YES.partADisplayValue,
      indeterminateSentenceType = IndeterminateSentenceTypeOptions.LIFE.partADisplayValue,
      dateVloInformed = "1 September 2022",
      selectedAlternatives = alternativesList,
      hasArrestIssues = ValueWithDetails(value = "Yes", details = "Arrest issue details"),
      hasContrabandRisk = ValueWithDetails(value = "Yes", details = "Contraband risk details"),
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
      ),
      gender = "Male",
      name = "Homer Bart Simpson",
      ethnicity = "Ainu",
      dateOfBirth = parse("1982-10-24"),
      croNumber = "123456/04A",
      pncNumber = "2004/0712343H",
      mostRecentPrisonerNumber = "G12345",
      nomsNumber = "A1234CR",
      indexOffenceDescription = "Armed robbery",
      dateOfOriginalOffence = "01/09/2022",
      dateOfSentence = "05/09/2022",
      lengthOfSentence = "6 days",
      licenceExpiryDate = "06/09/2022",
      sentenceExpiryDate = "07/09/2022",
      custodialTerm = "6 days",
      extendedTerm = "20 days",
      mappa = Mappa(level = 1, category = 1, isNominal = null, lastUpdated = null),
      lastRecordedAddress = "Address line 1, Address line 2, My town, TS1 1ST",
      letterAddress = "123 Acacia Avenue, Birmingham B23 1AB",
      noFixedAbode = "",
      lastPersonCompletingFormName = "Henry Richarlison",
      lastPersonCompletingFormEmail = "Henry.Richarlison@test.com",
      region = "NPS London",
      localDeliveryUnit = "All NPS London",
      dateOfDecision = "13/09/2022",
      timeOfDecision = "08:26",
      fixedTermAdditionalLicenceConditions = "This is an additional licence condition",
      behaviourSimilarToIndexOffence = "behavior similar to index offence",
      behaviourSimilarToIndexOffencePresent = YES.partADisplayValue,
      behaviourLeadingToSexualOrViolentOffence = "behaviour leading to sexual or violent offence",
      behaviourLeadingToSexualOrViolentOffencePresent = YES.partADisplayValue,
      outOfTouch = "out of touch",
      outOfTouchPresent = YES.partADisplayValue,
      otherPossibleAddresses = "123 Acacia Avenue, Birmingham, B23 1AV"
    )
  }
}