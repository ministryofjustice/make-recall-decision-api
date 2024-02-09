package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions

data class UpdatePostRelease(
  val assistantChiefOfficer: PpudContact = PpudContact(),
  val offenderManager: PpudContactWithTelephone = PpudContactWithTelephone(),
  val probationService: String = "",
  val spoc: PpudContact = PpudContact(),
)
