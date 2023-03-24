package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun offenderSearchDeliusResponse(crn: String? = "X123456", firstName: String? = "Pontius", surname: String? = "Pilate", fullName: String? = "Pontius Pilate") = """
[
  {
    "firstName": "$firstName",
    "surname": "$surname",
    "dateOfBirth": "2000-11-09",
    "otherIds": {
      "crn": "$crn"
    }
  }
]
""".trimIndent()
