package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonCreator

data class OffenceDetail @JsonCreator constructor(
  val mainCategoryDescription: String?,
  val subCategoryDescription: String?,
  val description: String?,
)
