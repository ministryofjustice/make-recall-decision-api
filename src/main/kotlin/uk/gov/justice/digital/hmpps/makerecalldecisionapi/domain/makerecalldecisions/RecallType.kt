package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecallTypeOption

data class RecallType(
  val value: String? = null,
  val options: List<RecallTypeOption>? = null
)
