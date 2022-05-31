package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun noActiveLicences(convictionId: Long) = """
{
     "licenceConditions": [
     {
          "startDate": "2022-05-18",
          "createdDateTime": "2022-05-18T19:33:56",
          "active": false,
          "licenceConditionTypeMainCat": {
              "code": "NLC8",
              "description": "Freedom of movement for conviction $convictionId"
          },
          "licenceConditionTypeSubCat": {
              "code": "NSTT8",
              "description": "To only attend places of worship which have been previously agreed with your supervising officer."
          }
     }
     ]
}
""".trimIndent()
