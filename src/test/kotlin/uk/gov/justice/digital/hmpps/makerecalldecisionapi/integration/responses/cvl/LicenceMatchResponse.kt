package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.cvl

fun licenceMatchResponse(nomisId: String, crn: String, licenceStatus: String? = "IN_PROGRESS", licenceId: Int? = 123344) = """
[
  {
    "licenceId": $licenceId,
    "licenceType": "AP",
    "licenceStatus": "$licenceStatus",
    "nomisId": "$nomisId",
    "surname": "Smith",
    "forename": "Brian",
    "prisonCode": "MDI",
    "prisonDescription": "Moorland (HMP)",
    "probationAreaCode": "N01",
    "probationAreaDescription": "Wales",
    "probationPduCode": "N01CA",
    "probationPduDescription": "North Wales",
    "probationLauCode": "NA01CA-02",
    "probationLauDescription": "North Wales",
    "probationTeamCode": "NA01CA-02-A",
    "probationTeamDescription": "Cardiff South",
    "conditionalReleaseDate": "2022-10-19",
    "actualReleaseDate": "2022-10-19",
    "crn": "$crn",
    "dateOfBirth": "1987-10-19",
    "comUsername": "jsmith",
    "bookingId": 773722,
    "dateCreated": "2022-10-19T16:13:29.274Z"
  }
]
""".trimIndent()
