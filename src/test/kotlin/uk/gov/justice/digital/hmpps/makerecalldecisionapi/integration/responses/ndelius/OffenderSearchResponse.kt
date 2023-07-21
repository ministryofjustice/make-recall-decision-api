package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun offenderSearchDeliusResponse(
  crn: String? = "X123456",
  firstName: String? = "Pontius",
  surname: String? = "Pilate",
  dateOfBirth: String? = "2000-11-09",
  totalPages: Int = 1
) = """
{
  "content": [
    {
      "firstName": "$firstName",
      "surname": "$surname",
      "dateOfBirth": "$dateOfBirth",
      "otherIds": {
        "crn": "$crn"
      }
    }
  ],
  "totalPages": $totalPages
}
""".trimIndent()
