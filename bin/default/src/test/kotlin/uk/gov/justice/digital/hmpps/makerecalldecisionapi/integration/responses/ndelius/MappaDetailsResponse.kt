package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.ndelius

fun mappaDetailsResponse(level: Int?, category: Int?) = """
{
    "level": $level,
    "levelDescription": "MAPPA Level 1",
    "category": $category,
    "categoryDescription": "All - Category to be determined",
    "startDate": "2021-02-10",
    "reviewDate": "2021-05-10",
    "team": {
        "code": "N07CHT",
        "description": "Automation SPG"
    },
    "officer": {
        "code": "N07A060",
        "forenames": "NDelius26",
        "surname": "Anderson",
        "unallocated": false
    },
    "probationArea": {
        "code": "N07",
        "description": "NPS London"
    },
    "notes": "Please Note - Category 3 offenders require multi-agency management at Level 2 or 3 and should not be recorded at Level 1.\nNote\nnew note"
}
""".trimIndent()
