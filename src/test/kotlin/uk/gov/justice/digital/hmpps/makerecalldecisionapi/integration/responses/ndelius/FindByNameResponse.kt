package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun findByNameResponse(
  crn: String? = "X123456",
  firstName: String? = "Joe",
  surname: String? = "Bloggs",
  dateOfBirth: String? = "2000-11-09",
  pageSize: Int = 1,
  pageNumber: Int = 1,
  totalPages: Int = 1,
  // language=json
) = """
{
  "content": [
    {
      "name": {
        "forename": "$firstName",
        "surname": "$surname"
      },
      "dateOfBirth": "$dateOfBirth",
      "gender": "Male",
      "identifiers": {
        "crn": "$crn"
      }
    }
  ],
  "page": {
    "size": $pageSize,
    "number": $pageNumber,
    "totalElements": 1,
    "totalPages": $totalPages
  }
}
""".trimIndent()
