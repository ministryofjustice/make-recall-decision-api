package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PartAData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypePartA
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel

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
          recallType = RecallType(selected = RecallTypeSelectedValue(value = RecallTypeValue.FIXED_TERM, details = "My details"), allOptions = null)
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
        recallType = RecallTypePartA(value = RecallTypeValue.FIXED_TERM.displayValue, details = "My details")
      )

      val result = partATemplateReplacementService.mappingsForTemplate(partA)

      assertThat(result.size).isEqualTo(3)
      assertThat(result["custody_status"]).isEqualTo("Police Custody")
      assertThat(result["recall_type"]).isEqualTo("Fixed")
      assertThat(result["recall_type_details"]).isEqualTo("My details")
    }
  }
}
