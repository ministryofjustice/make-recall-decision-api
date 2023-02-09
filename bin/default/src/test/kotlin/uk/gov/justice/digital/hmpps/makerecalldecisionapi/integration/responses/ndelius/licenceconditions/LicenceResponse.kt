package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun licenceResponse(convictionId: Long) = """
{
     "licenceConditions": [
     {
          "startDate": "2022-05-18",
          "createdDateTime": "2022-05-18T19:33:56",
          "active": true,
          "terminationDate": "2022-05-22",
          "licenceConditionNotes": "I am a licence condition note",
          "licenceConditionTypeMainCat": {
              "code": "NLC8",
              "description": "Freedom of movement for conviction $convictionId"
          },
          "licenceConditionTypeSubCat": {
              "code": "NSTT8",
              "description": "To only attend places of worship which have been previously agreed with your supervising officer."
          }
     },
     {
          "startDate": "2022-05-20",
          "createdDateTime": "2022-05-20T12:33:56",
          "active": false,
          "licenceConditionNotes": "I am a second licence condition note",
          "licenceConditionTypeMainCat": {
              "code": "NLC7",
              "description": "Inactive test"
          },
          "licenceConditionTypeSubCat": {
              "code": "NSTT7",
              "description": "I am inactive"
          }
     }]
}
""".trimIndent()
