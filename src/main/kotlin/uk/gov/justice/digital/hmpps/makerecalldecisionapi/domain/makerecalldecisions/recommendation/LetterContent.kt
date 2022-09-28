package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class LetterContent(
  val letterAddress: String? = null,
  val letterDate: String? = null,
  val salutation: String? = null,
  val letterTitle: String? = null,
  val section1: String? = null,
  val section2: String? = null,
  val section3: String? = null,
  val signedByParagraph: String? = null,
)
