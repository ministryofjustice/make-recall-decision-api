package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius.licenceconditions

fun multipleLicenceResponse() = """
{
     "licenceConditions": [
     {
          "startDate": "2022-05-18",
          "createdDateTime": "2022-05-18T19:33:56",
          "active": true,
          "licenceConditionTypeMainCat": {
              "code": "NLC8",
              "description": "Freedom of movement"
          },
          "licenceConditionTypeSubCat": {
              "code": "NSTT8",
              "description": "To only attend places of worship which have been previously agreed with your supervising officer."
          }
     },
     {
          "startDate": "2022-05-22",
          "createdDateTime": "2022-05-22T08:33:56",
          "active": true,
          "licenceConditionTypeMainCat": {
              "code": "NLC9",
              "description": "Another main condition"
          },
          "licenceConditionTypeSubCat": {
              "code": "NSTT9",
              "description": "Do not attend Hull city center after 8pm"
          }
     },
     {
          "startDate": "2022-05-20",
          "createdDateTime": "2022-05-20T12:33:56",
          "active": false,
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
