package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecallTypeOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Recommendation

data class RecallType(
  val value: Recommendation? = null,
  val options: List<RecallTypeOption>? = null
)
