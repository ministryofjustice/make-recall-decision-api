package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DntrData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecall
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.WhyConsideredRecallValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class DntrTemplateReplacementServiceTest : ServiceTestBase() {

  @Test
  fun `given recommendation data then build the DNTR document`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(
          crn = crn,
          whyConsideredRecall = WhyConsideredRecall(
            selected = WhyConsideredRecallValue.RISK_INCREASED,
            allOptions = listOf(
              TextValueOption(value = "RISK_INCREASED", text = "Your risk is assessed as increased"),
              TextValueOption(value = "CONTACT_STOPPED", text = "Contact with your probation practitioner has broken down"),
              TextValueOption(value = "RISK_INCREASED_AND_CONTACT_STOPPED", text = "Your risk is assessed as increased and contact with your probation practitioner has broken down")
            )
          )
        )
      )
      dntrTemplateReplacementService.generateDocFromTemplate(recommendation)
    }
  }

  @Test
  fun `given recommendation data then build the mappings for the DNTR template`() {
    runTest {
      // given
      val dntrData = dntrData()

      // when
      val result = dntrTemplateReplacementService.mappingsForTemplate(dntrData)

      // then
      assertThat(result.size).isEqualTo(1)
      assertThat(result["why_considered_recall"]).isEqualTo("Your risk is assessed as increased")
    }
  }

  @Test
  fun `given empty some data then build the mappings with blank strings for the DNTR template`() {
    runTest {
      // given
      val dntrData = DntrData(whyConsideredRecall = null)

      // when
      val result = dntrTemplateReplacementService.mappingsForTemplate(dntrData)

      // then
      assertThat(result["why_considered_recall"]).isEqualTo(EMPTY_STRING)
    }
  }

  private fun dntrData(): DntrData {
    return DntrData(whyConsideredRecall = "Your risk is assessed as increased")
  }
}
