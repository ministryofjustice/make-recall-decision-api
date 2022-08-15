package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeSelectedValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.RecallTypeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactScheme
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.VictimsInContactSchemeValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel
import java.time.LocalDate

@ExperimentalCoroutinesApi
class RecommendationToPartADataMapperTest {

  private lateinit var recommendationToPartADataMapper: RecommendationToPartADataMapper

  @BeforeEach
  fun setup() {
    recommendationToPartADataMapper = RecommendationToPartADataMapper()
  }

  @ParameterizedTest(name = "given custody status {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES_POLICE,Police Custody", "YES_PRISON,Prison Custody", "NO,No")
  fun `given custody status in recommendation data then should map to the part A text`(custodyValue: CustodyStatusValue, partADisplayText: String) {
    runTest {
      val recommendation = RecommendationEntity(id = 1, data = RecommendationModel(crn = "ABC123", custodyStatus = CustodyStatus(selected = custodyValue, allOptions = null)))

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.custodyStatus).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest(name = "given recall type {0} in recommendation data should map to the part A text {1}")
  @CsvSource("STANDARD,Standard", "FIXED_TERM,Fixed", "NO_RECALL,")
  fun `given recall type in recommendation data then should map to the part A text`(recallValue: RecallTypeValue, partADisplayText: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", recallType = RecallType(selected = RecallTypeSelectedValue(value = recallValue, details = "My details"), allOptions = null))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.recallType.value).isEqualTo(partADisplayText)
      assertThat(result.recallType.details).isEqualTo("My details")
    }
  }

  @ParameterizedTest(name = "given is emergency recall field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("true,Yes", "false,No", "null,No")
  fun `given is emergency recall data then should map to the part A text`(isThisAnEmergencyRecall: Boolean, partADisplayText: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", isThisAnEmergencyRecall = isThisAnEmergencyRecall)
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.isThisAnEmergencyRecall).isEqualTo(partADisplayText)
    }
  }

  @ParameterizedTest(name = "given is emergency recall field {0} in recommendation data should map to the part A text {1}")
  @CsvSource("YES,Yes", "NO,No", "NOT_APPLICABLE,N/A")
  fun `given victims in contact scheme data then should map to the part A text`(victimsInContactScheme: VictimsInContactSchemeValue, partADisplayText: String?) {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", hasVictimsInContactScheme = VictimsInContactScheme(selected = victimsInContactScheme))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.hasVictimsInContactScheme).isEqualTo(partADisplayText)
    }
  }

  @Test
  fun `given date vlo informed then should map to readable date in part A text`() {
    runTest {
      val recommendation = RecommendationEntity(
        id = 1,
        data = RecommendationModel(crn = "ABC123", dateVloInformed = LocalDate.parse("2022-09-01"))
      )

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.dateVloInformed).isEqualTo("1 September 2022")
    }
  }
}
