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
    "dateOfBirth": "1986-05-11",
    "age": 36,
    "gender": "Male",
    "crn": "D006296"
  },
  "contactSummary": [
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
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
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
      "contactDocuments": [
        
      ],
      "description": null
    }
  ],
  "contactTypeGroups": [
    {
      "groupId": "unknown",
      "label": "Unknown",
      "contactTypeCodes": [
        "EASU",
        "NTER",
        "NCOM",
        "NREF",
        "C092",
        "C091",
        "ROC",
        "ETOA",
        "ETTA",
        "ETTR",
        "ETOR",
        "ERGR",
        "EAPP",
        "ECOM",
        "ERGN",
        "ETCH"
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
