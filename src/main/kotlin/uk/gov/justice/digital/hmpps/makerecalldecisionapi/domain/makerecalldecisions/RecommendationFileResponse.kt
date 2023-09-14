package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

import java.util.*

data class RecommendationFileResponse(
  val id: Long,
  val recommendationId: Long,
  val createdBy: String,
  val createdByUserFullName: String,
  val created: String,
  val category: String,
  val token: String,
  val name: String?,
  val s3Id: UUID?,
  val type: String?,
  val size: Long?,
  val notes: String?,
) {
}