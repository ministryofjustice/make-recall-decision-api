package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonCreator

data class SentenceType @JsonCreator constructor(
  val code: String?,
)
