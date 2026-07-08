package uk.gov.justice.digital.hmpps.makerecalldecisionapi

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class DataDictionaryMetadata(
  val description: String,
  val sar: Boolean,
)
