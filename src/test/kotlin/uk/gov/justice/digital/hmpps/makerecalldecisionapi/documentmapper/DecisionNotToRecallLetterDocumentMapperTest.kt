package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.HowWillAppointmentHappen
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointment
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.NextAppointmentValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ReasonsForNoRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecommendationResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExperimentalCoroutinesApi
class DecisionNotToRecallLetterDocumentMapperTest {

  private lateinit var decisionNotToRecallLetterDocumentMapper: DecisionNotToRecallLetterDocumentMapper

  @BeforeEach
  fun setup() {
    decisionNotToRecallLetterDocumentMapper = DecisionNotToRecallLetterDocumentMapper()
  }

  @Test
  fun `given no recall journey then build contents of the decision not to recall letter`() {
    runTest {
      val recommendation = RecommendationResponse(
        crn = "ABC123",
        personOnProbation = PersonOnProbation(
          firstName = "Cliff",
          middleNames = "Dave",
          surname = "Rowland",
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
        whyConsideredRecall = WhyConsideredRecall(
          selected = WhyConsideredRecallValue.RISK_INCREASED,
          allOptions = listOf(
            TextValueOption(value = "RISK_INCREASED", text = "Your risk is assessed as increased"),
            TextValueOption(value = "CONTACT_STOPPED", text = "Contact with your probation practitioner has broken down"),
            TextValueOption(value = "RISK_INCREASED_AND_CONTACT_STOPPED", text = "Your risk is assessed as increased and contact with your probation practitioner has broken down")
          )
        ),
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
          dateTimeOfAppointment = "2023-02-24T13:00:00.000Z",
          probationPhoneNumber = "01238282838"
        ),
        reasonsForNoRecall = ReasonsForNoRecall(licenceBreach = "Reason for breaching licence", noRecallRationale = "Rationale for no recall", popProgressMade = "Progress made so far detail", futureExpectations = "Future expectations detail")
      )

      val result = decisionNotToRecallLetterDocumentMapper.mapRecommendationDataToDocumentData(recommendation)

      assertThat(result.salutation).isEqualTo("Dear Cliff Rowland,")
      assertThat(result.letterAddress).isEqualTo("Cliff Rowland\nAddress line 1\nAddress line 2\nAddress line town\nTS1 1ST")
      assertThat(result.letterTitle).isEqualTo("DECISION NOT TO RECALL")
      assertThat(result.letterDate).isEqualTo(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))

      assertThat(result.section1).isEqualTo(
        "I am writing to you because you have breached your licence conditions in such a way that your risk is assessed as increased.\n\n" +
          "This breach has been discussed with a Probation manager and a decision has been made that you will not be recalled to prison. This letter explains this decision. If you have any questions, please contact me.\n\n" +
          "Reason for breaching licence\n\n" +
          "Rationale for no recall\n\n" +
          "Progress made so far detail\n\n" +
          "Future expectations detail\n\n" +
          "I hope our conversation and/or this letter has helped to clarify what is required of you going forward and that we can continue to work together to enable you to successfully complete your licence period.\n\n" +
          "Your next appointment is by telephone on:"
      )

      assertThat(result.section2).isEqualTo("Friday 24th February 2023 at 1:00pm\n")

      assertThat(result.section3).isEqualTo("You must please contact me immediately if you are not able to keep this appointment. Should you wish to discuss anything before then, please contact me by the following telephone number: 01238282838\n")

      assertThat(result.signedByParagraph).isEqualTo("Yours sincerely,\n\n\nProbation Practitioner/Senior Probation Officer/Head of PDU")
    }
  }
}
