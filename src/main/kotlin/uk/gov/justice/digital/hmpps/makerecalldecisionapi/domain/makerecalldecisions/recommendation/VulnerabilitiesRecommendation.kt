package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class VulnerabilitiesRecommendation(
  val selected: List<ValueWithDetails>?,
  val allOptions: List<TextValueOption>? = null,
)

enum class VulnerabilityOptions {
  RISK_OF_SUICIDE_OR_SELF_HARM,
  NOT_KNOWN,
  NONE,
  RELATIONSHIP_BREAKDOWN,
  DOMESTIC_ABUSE,
  DRUG_OR_ALCOHOL_USE,
  BULLYING_OTHERS,
  BEING_BULLIED_BY_OTHERS,
  BEING_AT_RISK_OF_SERIOUS_HARM_FROM_OTHERS,
  ADULT_OR_CHILD_SAFEGUARDING_CONCERNS,
  MENTAL_HEALTH_CONCERNS,
  PHYSICAL_HEALTH_CONCERNS,
  MEDICATION_TAKEN_INCLUDING_COMPLIANCE_WITH_MEDICATION,
  BEREAVEMENT_ISSUES,
  LEARNING_DIFFICULTIES,
  PHYSICAL_DISABILITIES,
  CULTURAL_OR_LANGUAGE_DIFFERENCES,
}
