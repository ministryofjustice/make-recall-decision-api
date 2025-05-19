package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class HowWillAppointmentHappen(
  val selected: NextAppointmentValue? = null,
  val allOptions: List<TextValueOption>? = null,
)

data class NextAppointment(
  val howWillAppointmentHappen: HowWillAppointmentHappen? = null,
  val dateTimeOfAppointment: String? = null,
  val probationPhoneNumber: String? = null,
)

enum class NextAppointmentValue {
  TELEPHONE,
  VIDEO_CALL,
  OFFICE_VISIT,
  HOME_VISIT,
}
