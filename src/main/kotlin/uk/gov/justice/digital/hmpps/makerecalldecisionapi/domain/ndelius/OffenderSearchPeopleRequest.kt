package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderSearchPeopleRequest(
  val firstName: String? = null,
  val surname: String? = null,
  val crn: String? = null,
)
