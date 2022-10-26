package ft

import io.restassured.RestAssured
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ContactHistoryTest() : FunctionalTest() {
  @Test
  fun `fetch contact history, expected 200`() {
    // given
    val expected = HttpStatus.OK.value()

    // when
    lastResponse = RestAssured
      .given()
      .pathParam("crn", testCrn)
      .header("Authorization", token)
      .get("http://127.0.0.1:8080/cases/{crn}/contact-history")

    // then
    assertThat(lastResponse.statusCode).isEqualTo(expected)
    assertResponse(lastResponse, contactHistoryExpectation())
  }
}

fun contactHistoryExpectation() = """
{
    "userAccessResponse": null,
    "personalDetailsOverview": {
        "name": "Ikenberry Camploongo",
        "firstName": "Ikenberry",
        "middleNames": "ZZ",
        "surname": "Camploongo",
        "dateOfBirth": "1986-05-11",
        "age": 36,
        "gender": "Male",
        "crn": "D006296",
        "ethnicity": "",
        "croNumber": "",
        "mostRecentPrisonerNumber": "",
        "pncNumber": "",
        "nomsNumber": ""
    },
    "contactSummary": [
        {
            "contactStartDate": "2022-10-17T15:23:43Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d58e8ba7-664c-4c65-8d08-5054139d2d82",
                    "documentName": "ikenberry-camploongo-d006296-143.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 17/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-17T15:23:44",
                    "createdAt": "2022-10-17T15:23:44",
                    "parentPrimaryKeyId": 2505355926
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-10-17T15:22:46Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "20b4a1fd-398d-486a-9b0b-f3c1df08c1f8",
                    "documentName": "ikenberry-camploongo-d006296-142.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 17/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-17T15:22:47",
                    "createdAt": "2022-10-17T15:22:47",
                    "parentPrimaryKeyId": 2505355925
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-10-17T11:47:22Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "f7144941-c902-4aee-b017-2554fb215453",
                    "documentName": "ikenberry-camploongo-d006296-141.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 17/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-17T11:47:27",
                    "createdAt": "2022-10-17T11:47:27",
                    "parentPrimaryKeyId": 2505355889
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-10-03T14:59:36Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d090d133-481c-424c-a06f-42ef44e737c7",
                    "documentName": "ikenberry-camploongo-d006296-140.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 03/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-03T14:59:37",
                    "createdAt": "2022-10-03T14:59:37",
                    "parentPrimaryKeyId": 2505310726
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-10-03T14:58:38Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "1a3a2ed7-4575-4d10-8861-4c514e8313a0",
                    "documentName": "ikenberry-camploongo-d006296-139.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 03/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-03T14:58:39",
                    "createdAt": "2022-10-03T14:58:39",
                    "parentPrimaryKeyId": 2505310725
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-10-03T10:55:27Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "859a4cba-548f-4fcc-9345-b52694a006b4",
                    "documentName": "ikenberry-camploongo-d006296-138.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 03/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-03T10:55:28",
                    "createdAt": "2022-10-03T10:55:28",
                    "parentPrimaryKeyId": 2505310185
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-10-03T10:54:21Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "183f8800-b64a-47b7-806d-168d5db87b31",
                    "documentName": "ikenberry-camploongo-d006296-137.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 03/10/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-10-03T10:54:23",
                    "createdAt": "2022-10-03T10:54:23",
                    "parentPrimaryKeyId": 2505310171
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T19:02:51Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "0b28c6f6-c882-4c9a-9932-87a8818b8d98",
                    "documentName": "ikenberry-camploongo-d006296-136.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T19:02:52",
                    "createdAt": "2022-09-30T19:02:52",
                    "parentPrimaryKeyId": 2505308788
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T19:01:24Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "36e41b7b-603c-4766-8fa9-24f19f143ec3",
                    "documentName": "ikenberry-camploongo-d006296-135.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T19:01:25",
                    "createdAt": "2022-09-30T19:01:25",
                    "parentPrimaryKeyId": 2505308765
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T17:10:27Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "2fefeed5-c3ef-4ec3-a85f-b5de74b71c14",
                    "documentName": "ikenberry-camploongo-d006296-134.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T17:10:28",
                    "createdAt": "2022-09-30T17:10:28",
                    "parentPrimaryKeyId": 2505306979
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T17:09:25Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "346eb5d4-af64-4f93-9dd3-cef4bb991a3d",
                    "documentName": "ikenberry-camploongo-d006296-133.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T17:09:25",
                    "createdAt": "2022-09-30T17:09:25",
                    "parentPrimaryKeyId": 2505306967
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T16:33:28Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "6b9ca4fc-9105-4733-8c09-2117f821047e",
                    "documentName": "ikenberry-camploongo-d006296-132.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T16:33:29",
                    "createdAt": "2022-09-30T16:33:29",
                    "parentPrimaryKeyId": 2505306372
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T16:32:24Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "1f7bfefd-d45e-4423-8689-f0b0f9363c3e",
                    "documentName": "ikenberry-camploongo-d006296-131.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T16:32:25",
                    "createdAt": "2022-09-30T16:32:25",
                    "parentPrimaryKeyId": 2505306356
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T14:35:31Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "2e1fbd6b-1053-4fee-bb11-c85a43386c34",
                    "documentName": "ikenberry-camploongo-d006296-130.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T14:35:32",
                    "createdAt": "2022-09-30T14:35:32",
                    "parentPrimaryKeyId": 2505305977
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T14:34:09Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "789eed8d-2e25-400f-a599-7b5c3888e74c",
                    "documentName": "ikenberry-camploongo-d006296-129.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T14:34:10",
                    "createdAt": "2022-09-30T14:34:10",
                    "parentPrimaryKeyId": 2505305952
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T13:44:17Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "7ea263a3-de77-4367-a22d-35c144e12e7d",
                    "documentName": "ikenberry-camploongo-d006296-128.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T13:44:18",
                    "createdAt": "2022-09-30T13:44:18",
                    "parentPrimaryKeyId": 2505305804
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T13:43:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a43ac2df-3ca3-431f-8356-4313119de025",
                    "documentName": "ikenberry-camploongo-d006296-127.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T13:43:13",
                    "createdAt": "2022-09-30T13:43:13",
                    "parentPrimaryKeyId": 2505305803
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T08:21:11Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ade96a4b-7974-491b-9824-ef1f17bc4c19",
                    "documentName": "ikenberry-camploongo-d006296-126.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T08:21:12",
                    "createdAt": "2022-09-30T08:21:12",
                    "parentPrimaryKeyId": 2505305333
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-30T08:20:00Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "3722b8a4-16eb-464d-912f-d337ff7e2567",
                    "documentName": "ikenberry-camploongo-d006296-125.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-30T08:20:08",
                    "createdAt": "2022-09-30T08:20:08",
                    "parentPrimaryKeyId": 2505305332
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:51:03Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "34999625-9732-45c8-b6c2-08d3bdaf6b13",
                    "documentName": "ikenberry-camploongo-d006296-124.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:51:04",
                    "createdAt": "2022-09-29T18:51:04",
                    "parentPrimaryKeyId": 2505304940
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:50:11Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a4494262-e6c6-4a41-84cd-858a2f191ff5",
                    "documentName": "ikenberry-camploongo-d006296-123.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:50:11",
                    "createdAt": "2022-09-29T18:50:11",
                    "parentPrimaryKeyId": 2505304939
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:25:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d0d9609f-1d73-498e-9fab-bf61b1f85ddb",
                    "documentName": "ikenberry-camploongo-d006296-122.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:25:09",
                    "createdAt": "2022-09-29T18:25:09",
                    "parentPrimaryKeyId": 2505304938
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:24:15Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "731176fc-0ea6-4ccf-9bb2-35cac204f395",
                    "documentName": "ikenberry-camploongo-d006296-121.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:24:16",
                    "createdAt": "2022-09-29T18:24:16",
                    "parentPrimaryKeyId": 2505304937
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:19:58Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "5304574c-d16b-4da3-87a1-0405b4a3c8ba",
                    "documentName": "ikenberry-camploongo-d006296-120.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:19:59",
                    "createdAt": "2022-09-29T18:19:59",
                    "parentPrimaryKeyId": 2505304936
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:19:05Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "9c13c437-77ed-4ad6-8da7-def10ad3fff4",
                    "documentName": "ikenberry-camploongo-d006296-119.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:19:05",
                    "createdAt": "2022-09-29T18:19:05",
                    "parentPrimaryKeyId": 2505304935
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:13:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "b2b962e6-5df8-4a39-99ae-c52e7fcd6eb2",
                    "documentName": "ikenberry-camploongo-d006296-118.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:13:15",
                    "createdAt": "2022-09-29T18:13:15",
                    "parentPrimaryKeyId": 2505304934
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T18:12:20Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "640771a2-8078-4ce1-84e1-71873215e3f5",
                    "documentName": "ikenberry-camploongo-d006296-117.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T18:12:21",
                    "createdAt": "2022-09-29T18:12:21",
                    "parentPrimaryKeyId": 2505304933
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:53:58Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "dd836c7f-9573-4278-8cad-564065dc3cbf",
                    "documentName": "ikenberry-camploongo-d006296-116.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:53:59",
                    "createdAt": "2022-09-29T17:53:59",
                    "parentPrimaryKeyId": 2505304887
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:53:04Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "6a7b0918-d760-4680-b0c6-71a621513c56",
                    "documentName": "ikenberry-camploongo-d006296-115.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:53:05",
                    "createdAt": "2022-09-29T17:53:05",
                    "parentPrimaryKeyId": 2505304886
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:49:46Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ce111388-b805-4016-ad12-5d68033363c7",
                    "documentName": "ikenberry-camploongo-d006296-114.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:49:47",
                    "createdAt": "2022-09-29T17:49:47",
                    "parentPrimaryKeyId": 2505304882
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:48:53Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c485cd3a-3e76-44d7-89dd-9fcb37067219",
                    "documentName": "ikenberry-camploongo-d006296-113.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:48:53",
                    "createdAt": "2022-09-29T17:48:53",
                    "parentPrimaryKeyId": 2505304878
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:45:30Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "bb2d255b-c6e7-45bb-8688-d758b2b448c4",
                    "documentName": "ikenberry-camploongo-d006296-112.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:45:31",
                    "createdAt": "2022-09-29T17:45:31",
                    "parentPrimaryKeyId": 2505304875
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:44:36Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "f1e68e23-e6c1-48b1-9790-d8fce3877ae3",
                    "documentName": "ikenberry-camploongo-d006296-111.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:44:37",
                    "createdAt": "2022-09-29T17:44:37",
                    "parentPrimaryKeyId": 2505304873
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:41:27Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "75d5a2d0-6643-4afd-b1e6-3cde888c9242",
                    "documentName": "ikenberry-camploongo-d006296-110.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:41:28",
                    "createdAt": "2022-09-29T17:41:28",
                    "parentPrimaryKeyId": 2505304869
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:40:34Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "efe0a89b-7bd7-4203-b500-5fdfa1a1dc3a",
                    "documentName": "ikenberry-camploongo-d006296-109.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:40:34",
                    "createdAt": "2022-09-29T17:40:34",
                    "parentPrimaryKeyId": 2505304867
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:37:26Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "475de197-7ec9-4bee-bafa-b058b7224e16",
                    "documentName": "ikenberry-camploongo-d006296-108.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:37:27",
                    "createdAt": "2022-09-29T17:37:27",
                    "parentPrimaryKeyId": 2505304860
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:36:32Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "9d84329b-f7ee-4443-a135-6d78f99d0abf",
                    "documentName": "ikenberry-camploongo-d006296-107.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:36:33",
                    "createdAt": "2022-09-29T17:36:33",
                    "parentPrimaryKeyId": 2505304858
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:26:24Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "84bc2a31-284f-436d-a7ca-37f7b038132e",
                    "documentName": "ikenberry-camploongo-d006296-106.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:26:24",
                    "createdAt": "2022-09-29T17:26:24",
                    "parentPrimaryKeyId": 2505304851
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:18:25Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "24d05fea-caad-4470-af45-cff3311525cf",
                    "documentName": "ikenberry-camploongo-d006296-105.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:18:26",
                    "createdAt": "2022-09-29T17:18:26",
                    "parentPrimaryKeyId": 2505304803
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:12:06Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c8c1927c-4900-4ec0-ad7a-b69057bf1494",
                    "documentName": "ikenberry-camploongo-d006296-104.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:12:07",
                    "createdAt": "2022-09-29T17:12:07",
                    "parentPrimaryKeyId": 2505304802
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T17:00:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a55deb0e-7f61-4686-88b8-3158457a1428",
                    "documentName": "ikenberry-camploongo-d006296-103.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T17:00:15",
                    "createdAt": "2022-09-29T17:00:15",
                    "parentPrimaryKeyId": 2505304800
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T16:59:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "431894f2-123b-4585-a88f-a0e523d3b092",
                    "documentName": "ikenberry-camploongo-d006296-102.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T16:59:14",
                    "createdAt": "2022-09-29T16:59:14",
                    "parentPrimaryKeyId": 2505304799
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T16:51:31Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "b3c2e753-1993-4044-91aa-54864e7818ed",
                    "documentName": "ikenberry-camploongo-d006296-101.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T16:51:32",
                    "createdAt": "2022-09-29T16:51:32",
                    "parentPrimaryKeyId": 2505304796
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T16:25:56Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "f543e74c-a40c-4061-b573-7491c9379f02",
                    "documentName": "ikenberry-camploongo-d006296-100.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T16:25:57",
                    "createdAt": "2022-09-29T16:25:57",
                    "parentPrimaryKeyId": 2505304794
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T16:15:04Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "80da0c21-1e7a-454a-8600-2bb00c224ab0",
                    "documentName": "ikenberry-camploongo-d006296-99.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T16:15:05",
                    "createdAt": "2022-09-29T16:15:05",
                    "parentPrimaryKeyId": 2505304793
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T16:12:43Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "3efaf7bc-b2dd-4305-8571-7d565e4435ff",
                    "documentName": "ikenberry-camploongo-d006296-98.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T16:12:44",
                    "createdAt": "2022-09-29T16:12:44",
                    "parentPrimaryKeyId": 2505304792
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T14:58:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "48f50ac8-2301-4d15-aa33-d5e44b27d10f",
                    "documentName": "ikenberry-camploongo-d006296-97.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T14:58:08",
                    "createdAt": "2022-09-29T14:58:08",
                    "parentPrimaryKeyId": 2505304780
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T13:25:52Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "91de063a-4940-4cf5-b7e7-dedafe873a09",
                    "documentName": "ikenberry-camploongo-d006296-96.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T13:25:53",
                    "createdAt": "2022-09-29T13:25:53",
                    "parentPrimaryKeyId": 2505304742
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T13:03:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "badf4504-0506-45aa-900d-e257b1b18e27",
                    "documentName": "ikenberry-camploongo-d006296-95.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T13:03:16",
                    "createdAt": "2022-09-29T13:03:16",
                    "parentPrimaryKeyId": 2505304741
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-29T08:47:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "436b75f3-8516-454e-bc34-8ffa48048ba3",
                    "documentName": "ikenberry-camploongo-d006296-94.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-29T08:47:10",
                    "createdAt": "2022-09-29T08:47:10",
                    "parentPrimaryKeyId": 2505304614
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-27T12:47:11Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "7a116832-a007-4feb-80d8-9e147b488ce4",
                    "documentName": "ikenberry-camploongo-d006296-93.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 27/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-27T12:47:15",
                    "createdAt": "2022-09-27T12:47:15",
                    "parentPrimaryKeyId": 2505294267
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-27T09:48:18Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "04f6ed42-edd5-4496-abbb-90d0d11b9227",
                    "documentName": "ikenberry-camploongo-d006296-92.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 27/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-27T09:48:19",
                    "createdAt": "2022-09-27T09:48:19",
                    "parentPrimaryKeyId": 2505293833
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-27T09:22:37Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "16245a41-bf9b-43ee-bf13-db4aca8880b5",
                    "documentName": "ikenberry-camploongo-d006296-91.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 27/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-27T09:22:38",
                    "createdAt": "2022-09-27T09:22:38",
                    "parentPrimaryKeyId": 2505293830
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-26T16:40:46Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "36506060-ee03-42d6-8dfe-6192acfa0c71",
                    "documentName": "ikenberry-camploongo-d006296-90.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 26/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-26T16:40:47",
                    "createdAt": "2022-09-26T16:40:47",
                    "parentPrimaryKeyId": 2505293382
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-26T15:57:24Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a392a5e7-d4af-43c0-94b9-cab1bf3035c7",
                    "documentName": "ikenberry-camploongo-d006296-89.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 26/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-26T15:57:25",
                    "createdAt": "2022-09-26T15:57:25",
                    "parentPrimaryKeyId": 2505293376
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-26T15:24:19Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "e8977c1d-c9f7-4fa3-afd1-f59f04ab08a8",
                    "documentName": "ikenberry-camploongo-d006296-88.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 26/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-26T15:24:20",
                    "createdAt": "2022-09-26T15:24:20",
                    "parentPrimaryKeyId": 2505293375
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-26T15:11:59Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "cc44d05c-f79e-4cbb-9dd6-99078393415c",
                    "documentName": "ikenberry-camploongo-d006296-87.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 26/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-26T15:12:02",
                    "createdAt": "2022-09-26T15:12:02",
                    "parentPrimaryKeyId": 2505293374
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-22T07:54:33Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "e04cb973-3e2a-420b-a346-fee8a5440fef",
                    "documentName": "ikenberry-camploongo-d006296-86.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 22/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-22T07:54:34",
                    "createdAt": "2022-09-22T07:54:34",
                    "parentPrimaryKeyId": 2505275331
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-22T07:18:16Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "00d003d3-a37b-4794-82c5-b9f0af4a04e2",
                    "documentName": "ikenberry-camploongo-d006296-85.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 22/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-22T07:18:17",
                    "createdAt": "2022-09-22T07:18:17",
                    "parentPrimaryKeyId": 2505275330
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-21T18:46:36Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "9cbbac59-2ff0-4ed2-89a0-53c34a6b0694",
                    "documentName": "ikenberry-camploongo-d006296-84.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 21/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-21T18:46:37",
                    "createdAt": "2022-09-21T18:46:37",
                    "parentPrimaryKeyId": 2505274785
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-21T18:16:26Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "fa6508dd-e268-48b0-824f-5a90c575bd85",
                    "documentName": "ikenberry-camploongo-d006296-83.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 21/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-21T18:16:31",
                    "createdAt": "2022-09-21T18:16:31",
                    "parentPrimaryKeyId": 2505274782
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-16T07:06:51Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c52b9096-3b82-4a3d-b670-1d1eff97feb4",
                    "documentName": "ikenberry-camploongo-d006296-82.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-16T07:06:57",
                    "createdAt": "2022-09-16T07:06:57",
                    "parentPrimaryKeyId": 2505248797
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-13T16:24:30Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "f7203c6e-e158-4630-a652-97e4f91ed422",
                    "documentName": "ikenberry-camploongo-d006296-81.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 13/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-13T16:24:31",
                    "createdAt": "2022-09-13T16:24:31",
                    "parentPrimaryKeyId": 2505213776
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-13T10:28:46Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "11f6db87-ec75-4cd4-bb2b-1197d12782e0",
                    "documentName": "ikenberry-camploongo-d006296-80.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 13/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-13T10:28:56",
                    "createdAt": "2022-09-13T10:28:56",
                    "parentPrimaryKeyId": 2505213403
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-09T07:46:02Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "11478f62-57a2-4111-9c09-96f2c23b147f",
                    "documentName": "ikenberry-camploongo-d006296-79.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 09/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-09T07:46:04",
                    "createdAt": "2022-09-09T07:46:04",
                    "parentPrimaryKeyId": 2505198793
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T16:32:37Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "eb2be04f-240f-415a-b3fe-440eae61e45d",
                    "documentName": "ikenberry-camploongo-d006296-78.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T16:32:38",
                    "createdAt": "2022-09-08T16:32:38",
                    "parentPrimaryKeyId": 2505195631
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T10:51:19Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "680663f7-072b-420f-a9ec-08939839fb89",
                    "documentName": "ikenberry-camploongo-d006296-77.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T10:51:20",
                    "createdAt": "2022-09-08T10:51:20",
                    "parentPrimaryKeyId": 2505195380
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T10:30:37Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "cd5fe1de-4a9e-4aed-8ab8-41c3f21edd8b",
                    "documentName": "ikenberry-camploongo-d006296-76.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T10:30:38",
                    "createdAt": "2022-09-08T10:30:38",
                    "parentPrimaryKeyId": 2505195376
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T09:44:12Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "1af51ef1-a55e-426b-81db-05682968579d",
                    "documentName": "ikenberry-camploongo-d006296-75.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T09:44:13",
                    "createdAt": "2022-09-08T09:44:13",
                    "parentPrimaryKeyId": 2505195362
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T09:42:36Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "6906ce96-aab8-485e-a55f-859195adabb2",
                    "documentName": "ikenberry-camploongo-d006296-74.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T09:42:37",
                    "createdAt": "2022-09-08T09:42:37",
                    "parentPrimaryKeyId": 2505195361
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T09:37:05Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "e65e6835-2343-4444-bd39-93c0889d4157",
                    "documentName": "ikenberry-camploongo-d006296-73.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T09:37:06",
                    "createdAt": "2022-09-08T09:37:06",
                    "parentPrimaryKeyId": 2505195360
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-08T09:09:00Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c0aea286-4eff-41c6-b885-c67c88b5d4a7",
                    "documentName": "ikenberry-camploongo-d006296-72.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-08T09:09:03",
                    "createdAt": "2022-09-08T09:09:03",
                    "parentPrimaryKeyId": 2505195344
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T15:20:04Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "bc0dbb5f-ca52-4739-bc7e-e1833bdbe137",
                    "documentName": "ikenberry-camploongo-d006296-71.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T15:20:05",
                    "createdAt": "2022-09-07T15:20:05",
                    "parentPrimaryKeyId": 2505190860
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T12:55:19Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ac57f3bd-a652-44fb-979b-7407eb933109",
                    "documentName": "ikenberry-camploongo-d006296-70.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T12:55:20",
                    "createdAt": "2022-09-07T12:55:20",
                    "parentPrimaryKeyId": 2505190854
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T11:04:47Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "29934745-5451-48ab-b499-53c0d0f87e40",
                    "documentName": "ikenberry-camploongo-d006296-69.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T11:04:48",
                    "createdAt": "2022-09-07T11:04:48",
                    "parentPrimaryKeyId": 2505190848
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T10:44:05Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "22da93fb-fc59-46b1-abd8-38d0176f366a",
                    "documentName": "ikenberry-camploongo-d006296-68.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T10:44:06",
                    "createdAt": "2022-09-07T10:44:06",
                    "parentPrimaryKeyId": 2505190847
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T10:25:47Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "32597d96-838d-432f-8a40-d195f38d072b",
                    "documentName": "ikenberry-camploongo-d006296-67.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T10:25:48",
                    "createdAt": "2022-09-07T10:25:48",
                    "parentPrimaryKeyId": 2505190846
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T09:41:47Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "8821dae0-07a5-464e-8be3-e26207fa0279",
                    "documentName": "ikenberry-camploongo-d006296-66.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T09:41:48",
                    "createdAt": "2022-09-07T09:41:48",
                    "parentPrimaryKeyId": 2505190842
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T09:29:59Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "4af8f465-b9c1-41a6-a2d2-6f8f9b580582",
                    "documentName": "ikenberry-camploongo-d006296-65.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T09:30:00",
                    "createdAt": "2022-09-07T09:30:00",
                    "parentPrimaryKeyId": 2505190841
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-07T09:24:42Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "f42ec85f-3b4c-4077-9eb1-5e603b32450d",
                    "documentName": "ikenberry-camploongo-d006296-64.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-07T09:24:45",
                    "createdAt": "2022-09-07T09:24:45",
                    "parentPrimaryKeyId": 2505190840
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T16:28:40Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d1e1080a-e526-4b0f-9923-0bb93c642809",
                    "documentName": "ikenberry-camploongo-d006296-63.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T16:28:41",
                    "createdAt": "2022-09-06T16:28:41",
                    "parentPrimaryKeyId": 2505190357
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T16:28:31Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ab3a973b-3d63-4432-8dd0-aaa232025aae",
                    "documentName": "ikenberry-camploongo-d006296-62.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T16:28:32",
                    "createdAt": "2022-09-06T16:28:32",
                    "parentPrimaryKeyId": 2505190356
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T16:28:22Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "229a6c69-587f-4604-9945-09d396203981",
                    "documentName": "ikenberry-camploongo-d006296-61.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T16:28:23",
                    "createdAt": "2022-09-06T16:28:23",
                    "parentPrimaryKeyId": 2505190355
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T16:28:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ef8a5ece-ac56-4e0d-a16a-bee1a241a635",
                    "documentName": "ikenberry-camploongo-d006296-60.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T16:28:14",
                    "createdAt": "2022-09-06T16:28:14",
                    "parentPrimaryKeyId": 2505190354
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T16:05:24Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "26352f61-7290-4cfe-b740-5c19f5ff683f",
                    "documentName": "ikenberry-camploongo-d006296-59.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T16:05:25",
                    "createdAt": "2022-09-06T16:05:25",
                    "parentPrimaryKeyId": 2505190353
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T14:38:03Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d20b1d7c-ce87-4ee3-94c0-095aeb08ac42",
                    "documentName": "ikenberry-camploongo-d006296-58.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T14:38:04",
                    "createdAt": "2022-09-06T14:38:04",
                    "parentPrimaryKeyId": 2505190352
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T14:03:23Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d341457b-88ce-4e4f-9754-1f7c13806db6",
                    "documentName": "ikenberry-camploongo-d006296-57.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T14:03:24",
                    "createdAt": "2022-09-06T14:03:24",
                    "parentPrimaryKeyId": 2505190351
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T13:37:26Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "be352f5c-4872-4eae-a08b-fc07154937c2",
                    "documentName": "ikenberry-camploongo-d006296-56.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T13:37:27",
                    "createdAt": "2022-09-06T13:37:27",
                    "parentPrimaryKeyId": 2505190350
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T11:51:44Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c19db8b3-ad68-432f-8df8-0dd9e93fc637",
                    "documentName": "ikenberry-camploongo-d006296-55.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T11:51:45",
                    "createdAt": "2022-09-06T11:51:45",
                    "parentPrimaryKeyId": 2505190346
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T11:04:16Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "dcd81875-f6d6-47ff-a40e-13f73bfb66b3",
                    "documentName": "ikenberry-camploongo-d006296-54.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T11:04:17",
                    "createdAt": "2022-09-06T11:04:17",
                    "parentPrimaryKeyId": 2505190345
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T10:52:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a161d68b-8f73-475f-9015-33f9d9217450",
                    "documentName": "ikenberry-camploongo-d006296-53.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T10:52:13",
                    "createdAt": "2022-09-06T10:52:13",
                    "parentPrimaryKeyId": 2505190344
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T10:19:01Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "81645aa6-6f8a-4531-a396-75241c194f54",
                    "documentName": "ikenberry-camploongo-d006296-52.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T10:19:02",
                    "createdAt": "2022-09-06T10:19:02",
                    "parentPrimaryKeyId": 2505190343
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-06T09:47:57Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "e6ba08d5-3d10-4a5e-990d-2f448ff436fd",
                    "documentName": "ikenberry-camploongo-d006296-51.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-06T09:47:58",
                    "createdAt": "2022-09-06T09:47:58",
                    "parentPrimaryKeyId": 2505190342
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-05T17:11:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "52eed362-9bd1-4acb-9ce3-0b52f470d6ad",
                    "documentName": "ikenberry-camploongo-d006296-50.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 05/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-05T17:11:09",
                    "createdAt": "2022-09-05T17:11:09",
                    "parentPrimaryKeyId": 2505189914
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-09-05T16:30:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "29666974-6a5c-4ef7-9739-1373cfd277a2",
                    "documentName": "ikenberry-camploongo-d006296-49.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 05/09/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-09-05T16:30:10",
                    "createdAt": "2022-09-05T16:30:10",
                    "parentPrimaryKeyId": 2505189910
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-08-05T14:06:29Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "bd4fe57a-0636-4143-9932-986618b24f2c",
                    "documentName": "ikenberry-camploongo-d006296-48.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 05/08/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-08-05T14:06:30",
                    "createdAt": "2022-08-05T14:06:30",
                    "parentPrimaryKeyId": 2505099337
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-08-05T14:06:15Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "fad247d1-fb0d-4b55-b4f9-815bdd94f547",
                    "documentName": "ikenberry-camploongo-d006296-47.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 05/08/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-08-05T14:06:16",
                    "createdAt": "2022-08-05T14:06:16",
                    "parentPrimaryKeyId": 2505099336
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-08-05T14:06:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "310c11da-61f7-4f7e-8a43-a6bcdd59b13f",
                    "documentName": "ikenberry-camploongo-d006296-46.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 05/08/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-08-05T14:06:09",
                    "createdAt": "2022-08-05T14:06:09",
                    "parentPrimaryKeyId": 2505099334
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-08-05T14:06:00Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "9dbf2e02-cf27-466b-840a-907437242ef6",
                    "documentName": "ikenberry-camploongo-d006296-45.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 05/08/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-08-05T14:06:02",
                    "createdAt": "2022-08-05T14:06:02",
                    "parentPrimaryKeyId": 2505099333
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-08-01T12:14:05Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "7927745e-35dc-4590-a4cd-6a6a1c204480",
                    "documentName": "ikenberry-camploongo-d006296-44.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 01/08/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-08-01T12:14:07",
                    "createdAt": "2022-08-01T12:14:07",
                    "parentPrimaryKeyId": 2505071732
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-29T14:14:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "aa1d852d-d02c-4097-b142-28c2c27d337a",
                    "documentName": "ikenberry-camploongo-d006296-43.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-29T14:14:14",
                    "createdAt": "2022-07-29T14:14:14",
                    "parentPrimaryKeyId": 2505063588
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-29T13:49:34Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "6d56119c-5155-4598-9914-33ebda5580e6",
                    "documentName": "ikenberry-camploongo-d006296-42.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-29T13:49:35",
                    "createdAt": "2022-07-29T13:49:35",
                    "parentPrimaryKeyId": 2505063479
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-29T12:44:36Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d9f572df-b2a2-4026-8fd0-4b52f5eacbf2",
                    "documentName": "ikenberry-camploongo-d006296-41.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-29T12:44:37",
                    "createdAt": "2022-07-29T12:44:37",
                    "parentPrimaryKeyId": 2505063272
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-26T10:32:49Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "34933e58-87b8-47c0-b9ac-380ec0ae2e0c",
                    "documentName": "ikenberry-camploongo-d006296-40.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 26/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-26T10:32:50",
                    "createdAt": "2022-07-26T10:32:50",
                    "parentPrimaryKeyId": 2505043288
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-22T10:13:54Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "7c032bd4-567a-4154-951b-bd24951a353f",
                    "documentName": "ikenberry-camploongo-d006296-39.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 22/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-22T10:13:56",
                    "createdAt": "2022-07-22T10:13:56",
                    "parentPrimaryKeyId": 2505020941
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-19T23:00:00Z",
            "descriptionType": "NSI Referral",
            "code": "NREF",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-07-19T23:00:00Z",
            "descriptionType": "NSI Terminated",
            "code": "NTER",
            "outcome": null,
            "notes": "Comment added by Interventions Service on 20/07/2022 at 11:44\nNSI Terminated with Outcome: CRS Referral Cancelled",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-07-19T23:00:00Z",
            "descriptionType": "NSI Commenced",
            "code": "NCOM",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-07-20T10:44:58Z",
            "descriptionType": "Completed",
            "code": "C092",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": false,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-07-20T10:40:54Z",
            "descriptionType": "In Progress",
            "code": "C091",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": false,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-07-14T07:56:52Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "f9f65b3e-9320-4d54-ae57-003d90fcdd10",
                    "documentName": "ikenberry-camploongo-d006296-38.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 14/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-14T07:56:53",
                    "createdAt": "2022-07-14T07:56:53",
                    "parentPrimaryKeyId": 2504963351
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-07T14:40:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "89b9424e-b7c8-42c5-b86b-4386106ecdd8",
                    "documentName": "ikenberry-camploongo-d006296-37.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-07T14:40:13",
                    "createdAt": "2022-07-07T14:40:13",
                    "parentPrimaryKeyId": 2504902946
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-07T14:01:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "06f75660-e4ea-48ce-825a-5c15ebb7575e",
                    "documentName": "ikenberry-camploongo-d006296-36.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-07T14:01:14",
                    "createdAt": "2022-07-07T14:01:14",
                    "parentPrimaryKeyId": 2504902877
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-07T13:48:41Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "b35afe9d-4933-4a6c-8588-1d7776246868",
                    "documentName": "ikenberry-camploongo-d006296-35.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-07T13:48:42",
                    "createdAt": "2022-07-07T13:48:42",
                    "parentPrimaryKeyId": 2504902839
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-07T13:36:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "1a6f4000-c260-4f79-9132-642268fdabc0",
                    "documentName": "ikenberry-camploongo-d006296-34.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-07T13:36:15",
                    "createdAt": "2022-07-07T13:36:15",
                    "parentPrimaryKeyId": 2504902778
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-07T12:51:58Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "11108332-0b76-4a58-9384-6b8aea9552e6",
                    "documentName": "ikenberry-camploongo-d006296-33.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-07T12:51:58",
                    "createdAt": "2022-07-07T12:51:58",
                    "parentPrimaryKeyId": 2504902632
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-06T12:52:01Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "2b5dd689-6865-44ef-945a-8a93d341753e",
                    "documentName": "ikenberry-camploongo-d006296-32.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-06T12:52:02",
                    "createdAt": "2022-07-06T12:52:02",
                    "parentPrimaryKeyId": 2504889581
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-07-06T12:29:18Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "2bd6db33-131b-4e30-9f1b-d986d93fed5a",
                    "documentName": "ikenberry-camploongo-d006296-31.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 06/07/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-07-06T12:29:19",
                    "createdAt": "2022-07-06T12:29:19",
                    "parentPrimaryKeyId": 2504889016
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-30T14:40:21Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "4ab0b1dd-d68a-4ae5-ae73-79696288c893",
                    "documentName": "ikenberry-camploongo-d006296-30.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-30T14:40:22",
                    "createdAt": "2022-06-30T14:40:22",
                    "parentPrimaryKeyId": 2504852662
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-30T14:20:16Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c68ce2fd-3ec5-46f4-9408-269788cee46b",
                    "documentName": "ikenberry-camploongo-d006296-29.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 30/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-30T14:20:17",
                    "createdAt": "2022-06-30T14:20:17",
                    "parentPrimaryKeyId": 2504852290
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-29T15:23:26Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ca128455-3913-4be5-8c1c-7e7569caed5e",
                    "documentName": "ikenberry-camploongo-d006296-28.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-29T15:23:27",
                    "createdAt": "2022-06-29T15:23:27",
                    "parentPrimaryKeyId": 2504839103
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-29T15:12:45Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "8fbc08be-f7c8-42e5-9b09-69a0bba234c9",
                    "documentName": "ikenberry-camploongo-d006296-27.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-29T15:12:46",
                    "createdAt": "2022-06-29T15:12:46",
                    "parentPrimaryKeyId": 2504838817
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-29T14:50:59Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a52d91f4-a447-4355-841e-53f9ae934829",
                    "documentName": "ikenberry-camploongo-d006296-26.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 29/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-29T14:51:00",
                    "createdAt": "2022-06-29T14:51:00",
                    "parentPrimaryKeyId": 2504838366
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-16T16:40:30Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "90c64088-1857-46ce-859a-434c1afa0715",
                    "documentName": "ikenberry-camploongo-d006296-25.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-16T16:40:31",
                    "createdAt": "2022-06-16T16:40:31",
                    "parentPrimaryKeyId": 2504724280
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-16T16:29:57Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d212c52e-73d8-490c-b2fc-4c7e02088324",
                    "documentName": "ikenberry-camploongo-d006296-24.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-16T16:29:58",
                    "createdAt": "2022-06-16T16:29:58",
                    "parentPrimaryKeyId": 2504724120
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-16T16:16:57Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "7a6a24aa-3f9b-4115-ada9-72d646f77d1f",
                    "documentName": "ikenberry-camploongo-d006296-23.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-16T16:16:58",
                    "createdAt": "2022-06-16T16:16:58",
                    "parentPrimaryKeyId": 2504723883
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-16T12:24:22Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "61ceef1f-818c-4576-9895-cf56d2550e4a",
                    "documentName": "ikenberry-camploongo-d006296-22.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-16T12:24:23",
                    "createdAt": "2022-06-16T12:24:23",
                    "parentPrimaryKeyId": 2504720077
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-08T17:42:34Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "1d925df4-478f-480f-98a1-50fb5ff140e2",
                    "documentName": "ikenberry-camploongo-d006296-21.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-08T17:42:35",
                    "createdAt": "2022-06-08T17:42:35",
                    "parentPrimaryKeyId": 2504684097
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-08T17:42:32Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "6471bae8-497a-4684-b5c2-415f9af54479",
                    "documentName": "ikenberry-camploongo-d006296-20.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-08T17:42:33",
                    "createdAt": "2022-06-08T17:42:33",
                    "parentPrimaryKeyId": 2504684096
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-08T17:42:01Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "37c3e7c3-cb96-47e5-a6fd-5ac6ab6eb8bc",
                    "documentName": "ikenberry-camploongo-d006296-19.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-08T17:42:02",
                    "createdAt": "2022-06-08T17:42:02",
                    "parentPrimaryKeyId": 2504684085
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-08T14:05:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "a3496094-108e-4c1c-9fa1-7ec27a60da13",
                    "documentName": "ikenberry-camploongo-d006296-18.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 08/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-08T14:05:10",
                    "createdAt": "2022-06-08T14:05:10",
                    "parentPrimaryKeyId": 2504677766
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-06-07T08:23:30Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "50787afd-de7e-48da-b9fa-10be525a9b2c",
                    "documentName": "ikenberry-camploongo-d006296-17.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 07/06/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-06-07T08:23:31",
                    "createdAt": "2022-06-07T08:23:31",
                    "parentPrimaryKeyId": 2504652508
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-19T11:11:41Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "282b4bd3-f786-4a0f-abc6-c02f06eb182d",
                    "documentName": "ikenberry-camploongo-d006296-16.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 19/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-19T11:11:42",
                    "createdAt": "2022-05-19T11:11:42",
                    "parentPrimaryKeyId": 2504495514
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-18T14:14:01Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "46c62488-c0ed-48dd-b456-54b3bd4ea696",
                    "documentName": "ikenberry-camploongo-d006296-15.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 18/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-18T14:14:02",
                    "createdAt": "2022-05-18T14:14:02",
                    "parentPrimaryKeyId": 2504488739
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-17T17:38:24Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "dd2bf379-c4b5-4f62-8969-62ed1ea9e09d",
                    "documentName": "ikenberry-camploongo-d006296-14.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 17/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-17T17:38:25",
                    "createdAt": "2022-05-17T17:38:25",
                    "parentPrimaryKeyId": 2504480928
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-17T16:05:43Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "e96bb723-d41b-4632-b28a-9044f230a909",
                    "documentName": "ikenberry-camploongo-d006296-13.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 17/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-17T16:05:47",
                    "createdAt": "2022-05-17T16:05:47",
                    "parentPrimaryKeyId": 2504480189
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-16T15:15:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "16830efa-d2a4-4c7c-9636-a9f664fc0dee",
                    "documentName": "ikenberry-camploongo-d006296-12.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-16T15:15:14",
                    "createdAt": "2022-05-16T15:15:14",
                    "parentPrimaryKeyId": 2504471511
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-16T14:02:13Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "dd5ae668-0faa-47c5-945a-059d2820e51d",
                    "documentName": "ikenberry-camploongo-d006296-11.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-16T14:02:13",
                    "createdAt": "2022-05-16T14:02:13",
                    "parentPrimaryKeyId": 2504471090
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-16T14:01:14Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c0f92619-b223-4b03-a27c-dab285fbafc2",
                    "documentName": "ikenberry-camploongo-d006296-10.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 16/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-16T14:01:15",
                    "createdAt": "2022-05-16T14:01:15",
                    "parentPrimaryKeyId": 2504471089
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-13T15:25:06Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "ce190f33-28fc-4744-a4ee-a311699a9c6a",
                    "documentName": "ikenberry-camploongo-d006296-9.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 13/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-13T15:25:07",
                    "createdAt": "2022-05-13T15:25:07",
                    "parentPrimaryKeyId": 2504454067
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-13T15:01:38Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "8d84af2d-270f-4210-9e8b-db113dda1d26",
                    "documentName": "ikenberry-camploongo-d006296-8.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 13/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-13T15:01:39",
                    "createdAt": "2022-05-13T15:01:39",
                    "parentPrimaryKeyId": 2504453943
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-13T14:52:46Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "c08a5f10-272a-4156-8de8-d397cb603693",
                    "documentName": "ikenberry-camploongo-d006296-7.pdf",
                    "author": "Lucas Cairns",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 13/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-13T14:52:47",
                    "createdAt": "2022-05-13T14:52:47",
                    "parentPrimaryKeyId": 2504453879
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-13T14:40:26Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "39f61f41-5ad2-437d-96b1-759bf0fad688",
                    "documentName": "ikenberry-camploongo-d006296-6.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 13/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-13T14:40:27",
                    "createdAt": "2022-05-13T14:40:27",
                    "parentPrimaryKeyId": 2504453775
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-09T10:10:44Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "d34bc340-8682-4599-8cd1-91af94b71f47",
                    "documentName": "ikenberry-camploongo-d006296-5.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 09/05/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-05-09T10:10:44",
                    "createdAt": "2022-05-09T10:10:44",
                    "parentPrimaryKeyId": 2504430290
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-05-05T23:00:00Z",
            "descriptionType": "Responsible Officer Change",
            "code": "ROC",
            "outcome": null,
            "notes": "Comment added by Stuart Whitlam on 06/05/2022 at 15:56\nNew Details:\nResponsible Officer Type: Offender Manager\nResponsible Officer: Auth, HMPPS (Automation SPG, NPS London)\nStart Date: 06/05/2022 15:56:20\nAllocation Reason: Case Allocated to CRC\n\nPrevious Details:\nResponsible Officer Type: Offender Manager\nResponsible Officer: Denlum, Abby ZZ (OMU B, CPA Cheshire and Gtr Manchester)\nStart Date: 29/05/2015 11:55:34\nEnd Date: 06/05/2022 15:56:20\nAllocation Reason: CRC Initial Cohort Allocation",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-05-06T14:56:21Z",
            "descriptionType": "Inter Provider Order Transfer Accepted",
            "code": "ETOA",
            "outcome": null,
            "notes": "Comment added by Stuart Whitlam on 06/05/2022 at 15:56\nTransfer Status: Transfer Accepted\nTransfer Reason: Case Allocated to NPS\nAccepted Decision: Accepted\nOwning Provider: CPA Cheshire and Gtr Manchester\nReceiving Provider: NPS London\nNotes: \nnull",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-05-06T14:56:20Z",
            "descriptionType": "Transfer Accepted",
            "code": "ETTA",
            "outcome": null,
            "notes": "Comment added by Stuart Whitlam on 06/05/2022 at 15:56\nTransfer Status: Transfer Accepted\nTransfer Reason: Case Allocated to CRC\nAccepted Decision: Accepted\nOwning Provider: CPA Cheshire and Gtr Manchester\nReceiving Provider: NPS London\nNotes: \nnull",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-04-28T11:42:25Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "33a64d37-9ffb-4b58-99d1-be2349c510d8",
                    "documentName": "ikenberry-camploongo-d006296-4.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 28/04/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-04-28T11:42:26",
                    "createdAt": "2022-04-28T11:42:26",
                    "parentPrimaryKeyId": 2504375282
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-04-28T11:33:08Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "8ad07808-30ce-4362-af68-746fe5de8380",
                    "documentName": "ikenberry-camploongo-d006296-3.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 28/04/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-04-28T11:33:12",
                    "createdAt": "2022-04-28T11:33:12",
                    "parentPrimaryKeyId": 2504375239
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-04-27T12:51:23Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "7cef3f28-0538-4b61-b2e1-353a671f48fe",
                    "documentName": "ikenberry-camploongo-d006296-2.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 27/04/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-04-27T12:51:24",
                    "createdAt": "2022-04-27T12:51:24",
                    "parentPrimaryKeyId": 2504370475
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-04-27T10:54:50Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "8b448ff1-d70a-4fa4-bb51-acedd671204e",
                    "documentName": "ikenberry-camploongo-d006296-1.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 27/04/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-04-27T10:54:50",
                    "createdAt": "2022-04-27T10:54:50",
                    "parentPrimaryKeyId": 2504369954
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-04-26T11:47:21Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [
                {
                    "id": "73eef297-cee8-40f0-bddd-d55f1deda54f",
                    "documentName": "ikenberry-camploongo-d006296.pdf",
                    "author": "Stuart Whitlam",
                    "type": {
                        "code": "CONTACT_DOCUMENT",
                        "description": "Contact related document"
                    },
                    "extendedDescription": "Contact on 26/04/2022 for CP/UPW Assessment",
                    "lastModifiedAt": "2022-04-26T11:47:22",
                    "createdAt": "2022-04-26T11:47:22",
                    "parentPrimaryKeyId": 2504364875
                }
            ],
            "description": null
        },
        {
            "contactStartDate": "2022-04-26T11:47:07Z",
            "descriptionType": "Transfer Requested",
            "code": "ETTR",
            "outcome": null,
            "notes": "Comment added by Stuart Whitlam on 26/04/2022 at 12:47\nTransfer Status: Pending\nTransfer Reason: Case Allocated to CRC\nOwning Provider: CPA Cheshire and Gtr Manchester\nReceiving Provider: NPS London\nNotes: \nnull",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-04-26T11:47:07Z",
            "descriptionType": "Inter Provider Order Transfer Requested",
            "code": "ETOR",
            "outcome": null,
            "notes": "Comment added by Stuart Whitlam on 26/04/2022 at 12:47\nTransfer Status: Pending\nTransfer Reason: Case Allocated to NPS\nOwning Provider: CPA Cheshire and Gtr Manchester\nReceiving Provider: NPS London\nNotes: \nnull",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-04-26T11:45:41Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2022-04-26T11:43:01Z",
            "descriptionType": "CP/UPW Assessment",
            "code": "EASU",
            "outcome": null,
            "notes": null,
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": false,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-05-29T10:59:57Z",
            "descriptionType": "Inter Provider Order Transfer Accepted",
            "code": "ETOA",
            "outcome": null,
            "notes": "Transfer Status: Transfer Accepted\n\nTransfer Reason: CRC Initial Cohort Allocation\n\nAccepted Decision: Accepted\n\nOwning Trust: NPS Training\n\nReceiving Trust: CPA Training\nNotes: \nnull\n\n",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-05-29T10:59:34Z",
            "descriptionType": "Inter Provider Order Transfer Requested",
            "code": "ETOR",
            "outcome": null,
            "notes": "Transfer Status: Pending\nTransfer Reason: CRC Initial Cohort Allocation\nOwning Trust: NPS Training\nReceiving Trust: CPA Training\nNotes: \nnull\n\n",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-05-29T10:55:34Z",
            "descriptionType": "Transfer Accepted",
            "code": "ETTA",
            "outcome": null,
            "notes": "Transfer Status: Transfer Accepted\n\nTransfer Reason: CRC Initial Cohort Allocation\n\nAccepted Decision: Accepted\n\nOwning Trust: NPS Training\n\nReceiving Trust: CPA Training\nNotes: \nnull\n\n\n\n----------------------------\n\nAlert flag cleared using an automated process on 04-OCT-2018 12:54:42 by USER,SYSTEM",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-05-29T10:55:22Z",
            "descriptionType": "Transfer Requested",
            "code": "ETTR",
            "outcome": null,
            "notes": "Transfer Status: Pending\nTransfer Reason: CRC Initial Cohort Allocation\nOwning Trust: NPS Training\nReceiving Trust: CPA Training\nNotes: \nnull\n\n\n\n----------------------------\n\nAlert flag cleared using an automated process on 04-OCT-2018 12:54:42 by USER,SYSTEM",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-05-16T23:00:00Z",
            "descriptionType": "Registration Review",
            "code": "ERGR",
            "outcome": null,
            "notes": "Type: Information - Foreign National",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-03-17T00:00:00Z",
            "descriptionType": "Registration Review",
            "code": "ERGR",
            "outcome": null,
            "notes": "Type: RoSH - Medium RoSH",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2015-02-14T00:00:00Z",
            "descriptionType": "Registration Review",
            "code": "ERGR",
            "outcome": null,
            "notes": "Type: Alerts - Foreign Travel Order",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2014-11-17T00:00:00Z",
            "descriptionType": "Tier Change",
            "code": "ETCH",
            "outcome": null,
            "notes": "Tier Change Date: 23/02/2014\nTier: 2\nTier Change Reason: Initial Assessment\n",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2014-11-17T00:00:00Z",
            "descriptionType": "New Registration",
            "code": "ERGN",
            "outcome": null,
            "notes": "Type: Information - Foreign National\nNext Review Date: 23/08/2014",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2014-11-17T00:00:00Z",
            "descriptionType": "New Registration",
            "code": "ERGN",
            "outcome": null,
            "notes": "Type: Alerts - Foreign Travel Order\nNext Review Date: 23/05/2014",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2014-11-17T00:00:00Z",
            "descriptionType": "New Registration",
            "code": "ERGN",
            "outcome": null,
            "notes": "Type: RoSH - Medium RoSH\nNext Review Date: 23/06/2014",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2014-11-17T00:00:00Z",
            "descriptionType": "Court Appearance",
            "code": "EAPP",
            "outcome": null,
            "notes": "Main Offence: Endangering life at sea - 00700 (x1) on 07/02/2014\nCourt: Kirklees Magistrates Court\nAppearance Type: Sentence\nOutcome: CJA - Std Determinate Custody\n-------------------------------\n",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        },
        {
            "contactStartDate": "2014-11-17T00:00:00Z",
            "descriptionType": "Order/Component Commenced",
            "code": "ECOM",
            "outcome": null,
            "notes": "Order: CJA - Std Determinate Custody\nLength: 12 Months\n-------------------------------\n",
            "enforcementAction": null,
            "systemGenerated": true,
            "sensitive": null,
            "contactDocuments": [],
            "description": null
        }
    ],
    "contactTypeGroups": [
        {
            "groupId": "unknown",
            "label": "Unknown",
            "contactTypeCodes": [
                "EASU",
                "NREF",
                "NTER",
                "NCOM",
                "C092",
                "C091",
                "ROC",
                "ETOA",
                "ETTA",
                "ETTR",
                "ETOR",
                "ERGR",
                "ETCH",
                "ERGN",
                "EAPP",
                "ECOM"
            ]
        }
    ],
    "releaseSummary": {
        "lastRelease": null,
        "lastRecall": null
    },
    "activeRecommendation": null
}
""".trimIndent()
