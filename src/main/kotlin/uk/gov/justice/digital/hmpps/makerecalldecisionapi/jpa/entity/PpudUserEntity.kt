package uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity

import java.security.SecureRandom
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import kotlin.math.abs

@Entity
@Table(name = "ppud_user")
data class PpudUserEntity(
  @Id
  open var id: Long = abs(SecureRandom().nextInt().toLong()),
  var userName: String,
  var ppudUserFullName: String,
  var ppudTeamName: String,
)
