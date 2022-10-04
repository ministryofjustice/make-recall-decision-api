package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.arn

fun contingencyPlanSimpleResponse() = """
{
  "assessments": [
    {
      "initiationDate": "2020-07-03T11:42:01",
      "dateCompleted": "2020-07-03T11:42:01",
      "assessmentStatus": "COMPLETE",
      "keyConsiderationsCurrentSituation": "key considerations for current situation",
      "furtherConsiderationsCurrentSituation": "further considerations for current situation",
      "supervision": null,
      "monitoringAndControl": "monitoring and control",
      "interventionsAndTreatment": "interventions and treatment",
      "victimSafetyPlanning": "victim safety planning",
      "contingencyPlans": null
    }
  ]
}
""".trimIndent()
