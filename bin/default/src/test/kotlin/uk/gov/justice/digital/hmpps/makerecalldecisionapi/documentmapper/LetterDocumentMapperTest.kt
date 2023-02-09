package uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class LetterDocumentMapperTest {

  private lateinit var letterDocumentMapper: LetterDocumentMapper

  @BeforeEach
  fun setup() {
    letterDocumentMapper = DecisionNotToRecallLetterDocumentMapper()
  }

  @ParameterizedTest()
  @CsvSource("1,st", "2,nd", "3,rd", "4,th", "11,th", "13,th", "21,st", "22,nd", "23,rd", "24,th", "31,st")
  fun `set ordinal (suffix) of the day part of date`(day: Int, expected: String) {
    val result = letterDocumentMapper.getDayOfMonthSuffix(day)

    assertThat(result).isEqualTo(expected)
  }
}
