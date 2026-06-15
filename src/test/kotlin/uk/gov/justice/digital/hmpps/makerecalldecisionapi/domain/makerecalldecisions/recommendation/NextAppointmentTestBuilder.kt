package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.textValueOption
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum

fun nextAppointment(
  howWillAppointmentHappen: HowWillAppointmentHappen? = null,
  dateTimeOfAppointment: String? = null,
  probationPhoneNumber: String? = null,
) = NextAppointment(
  howWillAppointmentHappen = howWillAppointmentHappen,
  dateTimeOfAppointment = dateTimeOfAppointment,
  probationPhoneNumber = probationPhoneNumber,
)

fun howWillAppointmentHappen(
  selected: NextAppointmentValue? = randomEnum<NextAppointmentValue>(),
  allOptions: List<TextValueOption>? = listOf(textValueOption()),
) = HowWillAppointmentHappen(
  selected = selected,
  allOptions = allOptions,
)
