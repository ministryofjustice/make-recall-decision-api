package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.security.SecureRandom
import kotlin.math.abs

@Entity
@Table(name = "ppud_users")
data class PpudUserEntity(
  @Id
  var id: Long = abs(SecureRandom().nextInt().toLong()),
  var userName: String,
  var ppudUserFullName: String,
  var ppudTeamName: String,
)
