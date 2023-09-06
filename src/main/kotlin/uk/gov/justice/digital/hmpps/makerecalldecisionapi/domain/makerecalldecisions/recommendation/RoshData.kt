package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

data class RoshData(
  val riskToChildren: RoshDataScore? = null,
  val riskToPublic: RoshDataScore? = null,
  val riskToKnownAdult: RoshDataScore? = null,
  val riskToStaff: RoshDataScore? = null,
  val riskToPrisoners: RoshDataScore? = null,
)

enum class RoshDataScore(val partADisplayValue: String) {
  VERY_HIGH("Very High"),
  HIGH("High"),
  MEDIUM("Medium"),
  LOW("Low"),
  NOT_APPLICABLE("N/A"),
}
