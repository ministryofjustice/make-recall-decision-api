package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate
import java.time.LocalDateTime

data class ConvictionResponse(
  @JsonFormat(pattern = "yyyy-MM-dd", shape = STRING)
  val convictionDate: LocalDate?,
  val sentence: Sentence?,
  val active: Boolean?,
  val offences: List<Offence>?,
  val convictionId: Long?,
  val orderManagers: List<OrderManager>?,
  val custody: Custody?
)

data class OrderManager(
  val dateStartOfAllocation: LocalDateTime?,
  val name: String?,
  val staffCode: String?,
  val gradeCode: String?
)

data class Custody(
  val status: CustodyStatus?
)

data class CustodyStatus(
  val code: String?
)

data class Offence @JsonCreator constructor(
  val mainOffence: Boolean?,
  val detail: OffenceDetail?,
)

data class OffenceDetail @JsonCreator constructor(
  val mainCategoryDescription: String?,
  val subCategoryDescription: String?,
  val description: String?,
)

data class Sentence @JsonCreator constructor(

  @JsonFormat(pattern = "yyyy-MM-dd", shape = STRING)
  val startDate: LocalDate?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = STRING)
  val terminationDate: LocalDate?,
  @JsonFormat(pattern = "yyyy-MM-dd", shape = STRING)
  val expectedSentenceEndDate: LocalDate?,
  val description: String?,
  val originalLength: Int?,
  val originalLengthUnits: String?,
  val sentenceType: SentenceType?
)

data class SentenceType @JsonCreator constructor(
  val code: String?,
)
