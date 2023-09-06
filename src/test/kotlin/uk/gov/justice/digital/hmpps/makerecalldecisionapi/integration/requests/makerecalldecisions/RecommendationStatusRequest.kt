package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun recommendationStatusRequest(
  activate: String,
  anotherToActivate: String? = null,
  deactivate: String? = null,
  anotherToDeactivate: String? = null,
): String {
  return if (anotherToActivate != null) {
    """{"activate": ["$activate", "$anotherToActivate"], "deActivate": ["$deactivate", "$anotherToDeactivate"]}""".trimIndent()
  } else {
    ""
  }
}
