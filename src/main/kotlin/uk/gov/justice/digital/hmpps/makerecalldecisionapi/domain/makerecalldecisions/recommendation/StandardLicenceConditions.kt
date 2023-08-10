package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.TextValueOption

data class StandardLicenceConditions(
  val selected: List<String>? = null,
  val allOptions: List<TextValueOption>? = null
)

enum class SelectedStandardLicenceConditions(val cvlCode: String) {
  GOOD_BEHAVIOUR("9ce9d594-e346-4785-9642-c87e764bee37"),
  NO_OFFENCE("3b19fdb0-4ca3-4615-9fdd-61fabc1587af"),
  KEEP_IN_TOUCH("3361683a-504a-4357-ae22-6aa01b370b4a"),
  SUPERVISING_OFFICER_VISIT("9fc04065-df29-4bda-9b1d-bced8335c356"),
  ADDRESS_APPROVED("e670ac69-eda2-4b04-a0a1-a3c8492fe1e6"),
  NO_WORK_UNDERTAKEN("88069445-08cb-4f16-915f-5a162d085c26"),
  NO_TRAVEL_OUTSIDE_UK("7d416906-0e94-4fde-ae86-8339d339ccb7"),
  NAME_CHANGE("78A5F860-4791-48F2-B707-D6D4413850EE"),
  CONTACT_DETAILS("6FA6E492-F0AB-4E76-B868-63813DB44696");

  fun isCodeForCvl(cvlCode: String): Boolean {
    return this.cvlCode.equals(cvlCode, ignoreCase = true)
  }
}
