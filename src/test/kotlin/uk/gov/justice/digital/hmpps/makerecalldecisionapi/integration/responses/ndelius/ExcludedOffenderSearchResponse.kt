package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun limitedAccessOffenderSearchResponse(crn: String) = """
[
  {
    "offenderId": 123456753,
    "otherIds": {
      "crn": "$crn"
    },
    "offenderManagers": [
      {
        "trustOfficer": {
          "forenames": "Test",
          "surname": "Officername"
        },
        "staff": {
          "code": "A123456",
          "forenames": "Tester",
          "surname": "Name",
          "unallocated": false
        },
        "partitionArea": "National Data",
        "softDeleted": false,
        "team": {
          "code": "N123",
          "description": "Ox 4",
          "localDeliveryUnit": {
            "code": "NE1",
            "description": "Ox"
          },
          "district": {
            "code": "N123",
            "description": "Ox"
          },
          "borough": {
            "code": "N123",
            "description": "Ox"
          }
        },
        "probationArea": {
          "code": "N22",
          "description": "NPS North West Region",
          "nps": true
        },
        "fromDate": "2019-06-27",
        "active": true,
        "allocationReason": {
          "code": "TR2",
          "description": "Transfer to support NPS Restructure"
        }
      }
    ],
    "accessDenied": true
  }
]
""".trimIndent()
