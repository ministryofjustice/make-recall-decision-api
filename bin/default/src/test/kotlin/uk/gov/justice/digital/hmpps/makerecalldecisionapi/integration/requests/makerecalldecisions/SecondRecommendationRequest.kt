package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.requests.makerecalldecisions

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.Status

fun secondUpdateRecommendationRequest(status: Status = Status.DRAFT) = """
{
  "status": "$status",
  "previousReleases": {
    "previousReleaseDates": [
      "2015-04-24"
    ]
  },
  "previousRecalls": {
    "previousRecallDates": [
      "2018-10-10",
      "2016-04-30"
    ]
  }
}
""".trimIndent()
