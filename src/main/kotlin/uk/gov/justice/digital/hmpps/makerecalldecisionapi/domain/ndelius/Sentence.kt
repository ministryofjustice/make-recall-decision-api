package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class Sentence @JsonCreator constructor(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val startDate: LocalDate?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val terminationDate: LocalDate?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
  val expectedSentenceEndDate: LocalDate?,
  val description: String?,
  val originalLength: Int?,
  val originalLengthUnits: String?,
  val secondLength: Int?,
  val secondLengthUnits: String?,
  val sentenceType: SentenceType?,
)
