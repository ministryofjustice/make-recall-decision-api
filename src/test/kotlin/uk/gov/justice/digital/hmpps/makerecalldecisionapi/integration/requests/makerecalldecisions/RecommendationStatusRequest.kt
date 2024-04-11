package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

fun recommendationStatusRequest(
  activate: List<String>,
  deactivate: List<String> = emptyList(),
): String {
  val active = activate.map { '"' + it + '"' }.joinToString(separator = ",")
  val deactive = deactivate.map { '"' + it + '"' }.joinToString(separator = ",")

  return """{"activate": [$active], "deActivate": [$deactive]}""".trimIndent()
}
