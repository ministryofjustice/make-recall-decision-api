package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomBoolean
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDate
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString

fun managerRecallDecision(
  selected: ManagerRecallDecisionTypeSelectedValue? = managerRecallDecisionTypeSelectedValue(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
  isSentToDelius: Boolean? = randomBoolean(),
  createdBy: String? = randomString(),
  createdDate: String? = randomString(),
) = ManagerRecallDecision(
  selected = selected,
  allOptions = allOptions,
  isSentToDelius = isSentToDelius,
  createdBy = createdBy,
  createdDate = createdDate,
)

fun managerRecallDecisionTypeSelectedValue(
  value: ManagerRecallDecisionTypeValue? = randomEnum<ManagerRecallDecisionTypeValue>(),
  details: String? = randomString(),
) = ManagerRecallDecisionTypeSelectedValue(
  value = value,
  details = details,
)

fun recallType(
  selected: RecallTypeSelectedValue? = recallTypeSelectedValue(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = RecallType(
  selected = selected,
  allOptions = allOptions,
)

fun recallTypeSelectedValue(
  value: RecallTypeValue? = randomEnum<RecallTypeValue>(),
  details: String? = randomString(),
) = RecallTypeSelectedValue(
  value = value,
  details = details,
)

fun considerationRationale(
  createdBy: String? = randomString(),
  createdDate: String? = randomLocalDate().toString(),
  createdTime: String? = randomLocalTime().toString(),
  sensitive: Boolean? = randomBoolean(),
) = ConsiderationRationale(
  createdBy = createdBy,
  createdDate = createdDate,
  createdTime = createdTime,
  sensitive = sensitive,
)
