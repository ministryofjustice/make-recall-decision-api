package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun secondUpdateRecommendationRequest(status: Status = Status.DRAFT) = """
{
  "status": "$status"
}
""".trimIndent()
