package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.CustodyStatusOption

data class CustodyStatus(
  val value: String? = null,
  val options: List<CustodyStatusOption>? = null
)
