package uk.gov.justice.digital.hmpps.makerecalldecisionapi.util

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatus
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.CustodyStatusValue
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationModel

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
      val recommendation = RecommendationEntity(id = 1, data = RecommendationModel(crn = "ABC123", custodyStatus = CustodyStatus(value = custodyValue, options = null)))

      val result = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

      assertThat(result.custodyStatus).isEqualTo(partADisplayText)
    }
  }
}
