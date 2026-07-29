package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.ftrSuitabilityCriteria

enum class ChargedOrConvictedForNewOffence(val description: String) {
  ONLY_CHARGED("Yes, charged with a new offence but not convicted"),
  CHARGED_AND_CONVICTED("Yes, charged and convicted of a new offence"),
  NO("No"),
}
