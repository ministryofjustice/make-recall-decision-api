package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.documents

fun groupedDocumentsDeliusResponse() = """
{
    "documents": [
        {
            "id": "f2943b31-2250-41ab-a04d-004e27a97add",
            "documentName": "test doc.docx",
            "author": "Trevor Small",
            "type": {
                "code": "CONTACT_DOCUMENT",
                "description": "Contact related document"
            },
            "extendedDescription": "Contact on 21/06/2022 for Information - from 3rd Party",
            "lastModifiedAt": "2022-06-21T20:27:23.407",
            "createdAt": "2022-06-21T20:27:23",
            "parentPrimaryKeyId": 2504412185
        },
        {
            "id": "630ca741-cbb6-4f2e-8e86-73825d8c4d82",
            "documentName": "a test.pdf",
            "author": "Jackie Gough",
            "type": {
                "code": "CONTACT_DOCUMENT",
                "description": "Contact related document"
            },
            "extendedDescription": "Contact on 21/06/2020 for Complementary Therapy Session (NS)",
            "lastModifiedAt": "2022-06-21T20:29:17.324",
            "createdAt": "2022-06-21T20:29:17",
            "parentPrimaryKeyId": 2504435532
        },
        {
            "id": "444ca741-cbb6-4f2e-8e86-73825d8c4d83",
            "documentName": "another test.pdf",
            "author": "Brenda Jones",
            "type": {
                "code": "NSI_DOCUMENT",
                "description": "Non Statutory Intervention related document"
            },
            "extendedDescription": "Another description",
            "lastModifiedAt": "2022-06-22T20:29:17.324",
            "createdAt": "2022-06-22T20:29:17",
            "parentPrimaryKeyId": 2504435532
        }
    ],
    "convictions": [
        {
            "convictionId": "2500614567",
            "documents": [
                {
                    "id": "374136ce-f863-48d8-96dc-7581636e461e",
                    "documentName": "GKlicencejune2022.pdf",
                    "author": "Tom Thumb",
                    "type": {
                        "code": "CONVICTION_DOCUMENT",
                        "description": "Sentence related"
                    },
                    "lastModifiedAt": "2022-06-07T17:00:29.493",
                    "createdAt": "2022-06-07T17:00:29",
                    "parentPrimaryKeyId": 2500614567
                },
                {
                    "id": "374136ce-f863-48d8-96dc-7581636e123e",
                    "documentName": "TDlicencejuly2022.pdf",
                    "author": "Wendy Rose",
                    "type": {
                        "code": "CONVICTION_DOCUMENT",
                        "description": "Sentence related"
                    },
                    "lastModifiedAt": "2022-07-08T10:00:29.493",
                    "createdAt": "2022-06-08T10:00:29",
                    "parentPrimaryKeyId": 2500614567
                },
                {
                    "id": "374136ce-f863-48d8-96dc-7581636e461e",
                    "documentName": "ContactDoc.pdf",
                    "author": "Terry Tibbs",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact document"
                    },
                    "lastModifiedAt": "2022-06-07T17:00:29.493",
                    "createdAt": "2022-06-07T17:00:29",
                    "parentPrimaryKeyId": 2504435999
                },
                {
                    "id": "342234a8-b279-4d6e-b9ff-c7910afce95e",
                    "documentName": "Part A Recall Report 08 06 2022.doc",
                    "author": "Harry Wilson",
                    "type": {
                        "code": "NSI_DOCUMENT",
                        "description": "Non Statutory Intervention related document"
                    },
                    "extendedDescription": "Non Statutory Intervention for Request for Recall on 08/06/2022",
                    "lastModifiedAt": "2022-06-08T14:24:53.217",
                    "createdAt": "2022-06-08T14:24:53",
                    "parentPrimaryKeyId": 2500049480
                }
            ]
        }
    ]
}
""".trimIndent()
