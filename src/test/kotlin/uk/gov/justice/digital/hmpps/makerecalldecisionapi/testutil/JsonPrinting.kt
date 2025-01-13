package uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil

/**
 * Utility function for printing nullable fields into JSON strings.
 *
 * In JSON, non-null values should have double quotes around them and null
 * ones should print the string "null" without any quotes. Should only be
 * used with types that correspond to a JSON string field (i.e. not to use
 * with Int or with objects with sub-fields that should be printed as another
 * JSON object).
 */
internal fun toJsonNullableStringField(obj: Any?) =
  obj?.let { "\"$it\"" }
