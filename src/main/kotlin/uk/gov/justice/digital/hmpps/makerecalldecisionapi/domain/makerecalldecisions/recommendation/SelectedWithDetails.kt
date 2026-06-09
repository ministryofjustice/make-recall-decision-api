package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.io.Serializable

data class SelectedWithDetails(
  val selected: Boolean? = null,
  val details: String? = null,
) : Serializable
