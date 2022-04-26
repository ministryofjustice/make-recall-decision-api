package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ValidationRules.alphanumeric
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ValidationRules.notBlank
import java.util.UUID

/*
  Tiny wrapper types:
  If the type will be used json it will need custom Jackson serializer/deserializer: see `RestConfiguration`.
  It will also need adding to the `OpenApiConfiguration` to ensure it is presented correctly in the swagger docs
  (until we can figure out how to get SpringDoc to do that otherwise).
  If it is to be used as a PathVariable it will need a customer Converter: see `DomainConverters`.
  If used as a field on a persisted entity (e.g. Recall) then it will need a *JpaConverter in `Converters.jt`
 */

class Crn(value: String) : Validated<String>(value, notBlank, alphanumeric)
class FullName(value: String) : Validated<String>(value, notBlank)

fun <T : Validated<UUID>> ((UUID) -> T).random() = this(UUID.randomUUID())

object ValidationRules {
  val notBlank: Rule<String> get() = { it.isNotBlank() }
  val alphanumeric: Rule<String> get() = { it.all(Char::isLetterOrDigit) }
  val kebabAlphanumeric: Rule<String> get() = { str -> str.all { ch -> ch.isLetterOrDigit() || ch == '-' } }
  private val IntRange.constrainLength: Rule<String> get() = { contains(it.length) }
  val Int.minLength: Rule<String> get() = (this..Integer.MAX_VALUE).constrainLength
  val Int.maxLength: Rule<String> get() = (0..this).constrainLength
}

typealias Rule<T> = (T) -> Boolean

open class Validated<T : Comparable<T>>(
  val value: T,
  vararg rules: Rule<T>,
  private val asString: String = value.toString()
) : Comparable<Validated<T>> {
  init {
    rules.find { !it(value) }?.apply {
      throw IllegalArgumentException("'$asString' violated ${this@Validated::class.java.name} rule")
    }
  }

  override fun toString() = value.toString()

  override fun hashCode() = value.hashCode()

  override fun compareTo(other: Validated<T>): Int = value.compareTo(other.value)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Validated<*>

    if (value != other.value) return false

    return true
  }
}
