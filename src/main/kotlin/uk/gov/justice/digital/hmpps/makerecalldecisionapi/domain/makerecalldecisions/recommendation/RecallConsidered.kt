package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import java.security.SecureRandom
import kotlin.math.abs

data class RecallConsidered(
  var id: Long = abs(SecureRandom().nextInt().toLong()),
  val userId: String?,
  val createdDate: String?,
  val userName: String?,
  val recallConsideredDetail: String?,
)
