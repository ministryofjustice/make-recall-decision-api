package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses

fun emptyContactSummaryResponse() = """
{
    "pageable": {
        "sort": {
            "empty": false,
            "sorted": true,
            "unsorted": false
        },
        "offset": 0,
        "pageSize": 1000,
        "pageNumber": 0,
        "unpaged": false,
        "paged": true
    },
    "last": true,
    "totalElements": 0,
    "totalPages": 0,
    "size": 1000,
    "number": 0,
    "sort": {
        "empty": false,
        "sorted": true,
        "unsorted": false
    },
    "first": true,
    "numberOfElements": 0,
    "empty": true
}
""".trimIndent()
