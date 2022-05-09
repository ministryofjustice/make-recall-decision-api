package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.communityapi

import com.fasterxml.jackson.annotation.JsonCreator

data class Offence @JsonCreator constructor(
  val mainOffence: Boolean,
  val detail: OffenceDetail,
)

data class OffenceDetail @JsonCreator constructor(
  val mainCategoryDescription: String,
  val subCategoryDescription: String,
  val description: String,
)
