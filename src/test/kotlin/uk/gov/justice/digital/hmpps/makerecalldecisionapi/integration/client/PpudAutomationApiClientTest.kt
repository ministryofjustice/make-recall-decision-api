package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.client

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.ppud.PpudAutomationApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.DocumentCategory
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudAddress
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudContact
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudContactWithTelephone
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateMinuteRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateReleaseRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateOrUpdateSentenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudCreateRecallRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenceRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdateOffenderRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUpdatePostRelease
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadAdditionalDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUploadMandatoryDocumentRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUser
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PpudUserSearchRequest
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.SentenceLength
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("test")
class PpudAutomationApiClientTest : IntegrationTestBase() {
  @Autowired
  private lateinit var ppudAutomationApiClient: PpudAutomationApiClient

  @Test
  fun `retrieves licence matches`() {
    // given
    val croNumber = "123456/12A"
    val nomsId = "AB234A"

    ppudAutomationSearchApiMatchResponse(nomsId, croNumber)

    // when
    val actual = ppudAutomationApiClient.search(
      PpudSearchRequest(
        croNumber = croNumber,
        nomsId = nomsId,
        familyName = "Bloggs",
        dateOfBirth = LocalDate.of(2023, 1, 1),
      ),
    ).block()

    // then
    assertThat(actual?.results?.get(0)?.croNumber, equalTo(croNumber))
  }

  @Test
  fun `retrieve details`() {
    // given
    val id = "12345678"

    ppudAutomationDetailsMatchResponse(id)

    // when
    val actual = ppudAutomationApiClient.details(id).block()

    // then
    assertThat(actual?.offender?.id, equalTo(id))
  }

  @Test
  fun `create offender to ppud`() {
    // given
    val id = "12345678"
    val ppudCreateOffenderRequest = PpudCreateOffenderRequest(
      croNumber = "A/2342",
      nomsId = "A897",
      prisonNumber = "123",
      firstNames = "Joe",
      familyName = "Bloggs",
      indexOffence = "bad language",
      ethnicity = "W",
      gender = "M",
      mappaLevel = "",
      custodyType = "Determinate",
      establishment = "HMP Brixton",
      isInCustody = true,
      dateOfBirth = LocalDate.of(2004, 1, 1),
      dateOfSentence = LocalDate.of(2004, 1, 2),
      additionalAddresses = listOf(),
      address = PpudAddress(premises = "", line1 = "No Fixed Abode", line2 = "", postcode = "", phoneNumber = ""),
    )

    ppudAutomationCreateOffenderApiMatchResponse(id, ppudCreateOffenderRequest)

    // when
    val actual = ppudAutomationApiClient.createOffender(ppudCreateOffenderRequest).block()

    // then
    assertThat(actual?.offender?.id, equalTo(id))
  }

  @Test
  fun `update offender to ppud`() {
    // given
    val offenderId = "123"
    val ppudUpdateOffenderRequest = PpudUpdateOffenderRequest(
      croNumber = "A/2342",
      nomsId = "A897",
      prisonNumber = "123",
      firstNames = "Joe",
      familyName = "Bloggs",
      ethnicity = "W",
      gender = "M",
      isInCustody = true,
      dateOfBirth = LocalDate.of(2004, 1, 1),
      additionalAddresses = listOf(),
      address = PpudAddress(premises = "", line1 = "No Fixed Abode", line2 = "", postcode = "", phoneNumber = ""),
      establishment = "HMP Brixton",
    )

    ppudAutomationUpdateOffenderApiMatchResponse(offenderId, ppudUpdateOffenderRequest)

    // when
    ppudAutomationApiClient.updateOffender(
      offenderId,
      ppudUpdateOffenderRequest,
    ).block()

    // then
    // no exception
  }

  @Test
  fun `create sentence to ppud`() {
    // given
    val offenderId = "123"
    val id = "12345678"
    val createSentenceRequest = PpudCreateOrUpdateSentenceRequest(
      custodyType = "Determinate",
      dateOfSentence = LocalDate.of(2004, 1, 2),
      licenceExpiryDate = LocalDate.of(2004, 1, 3),
      mappaLevel = "1",
      releaseDate = LocalDate.of(2004, 1, 4),
      sentenceLength = SentenceLength(1, 1, 1),
      sentenceExpiryDate = LocalDate.of(2004, 1, 5),
      sentencingCourt = "sentencing court",
      sentencedUnder = "Legislation 123",
    )

    ppudAutomationCreateSentenceApiMatchResponse(offenderId, createSentenceRequest, id)

    // when
    val actual = ppudAutomationApiClient.createSentence(offenderId, createSentenceRequest).block()

    // then
    assertThat(actual?.sentence?.id, equalTo("12345678"))
  }

  @Test
  fun `update sentence to ppud`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"
    val updateSentenceRequest = PpudCreateOrUpdateSentenceRequest(
      custodyType = "Determinate",
      dateOfSentence = LocalDate.of(2004, 1, 2),
      licenceExpiryDate = LocalDate.of(2004, 1, 3),
      mappaLevel = "1",
      releaseDate = LocalDate.of(2004, 1, 4),
      sentenceLength = SentenceLength(1, 1, 1),
      sentenceExpiryDate = LocalDate.of(2004, 1, 5),
      sentencingCourt = "sentencing court",
      sentencedUnder = "Legislation 123",
    )

    ppudAutomationUpdateSentenceApiMatchResponse(offenderId, sentenceId, updateSentenceRequest)

    // when
    ppudAutomationApiClient.updateSentence(offenderId, sentenceId, updateSentenceRequest).block()

    // then
    // no exception
  }

  @Test
  fun `update offence in ppud`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"

    ppudAutomationUpdateOffenceApiMatchResponse(offenderId, sentenceId)

    // when
    ppudAutomationApiClient.updateOffence(
      offenderId,
      sentenceId,
      PpudUpdateOffenceRequest(
        indexOffence = "Index offence",
        indexOffenceComment = "Index offence comment",
        dateOfIndexOffence = LocalDate.of(2016, 1, 1),
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `update offence in ppud with index null comment`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"

    ppudAutomationUpdateOffenceApiMatchResponse(offenderId, sentenceId)

    // when
    ppudAutomationApiClient.updateOffence(
      offenderId,
      sentenceId,
      PpudUpdateOffenceRequest(
        indexOffence = "Index offence",
        indexOffenceComment = null,
        dateOfIndexOffence = LocalDate.of(2016, 1, 1),
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `update release to ppud`() {
    // given
    val offenderId = "123"
    val sentenceId = "456"
    val id = "12345678"
    val updateReleaseRequest = PpudCreateOrUpdateReleaseRequest(
      dateOfRelease = LocalDate.of(2016, 1, 1),
      postRelease = PpudUpdatePostRelease(
        assistantChiefOfficer = PpudContact(
          name = "Mr A",
          faxEmail = "1234",
        ),
        offenderManager = PpudContactWithTelephone(
          name = "Mr B",
          faxEmail = "567",
          telephone = "1234",
        ),
        probationService = "Argyl",
        spoc = PpudContact(
          name = "Mr C",
          faxEmail = "123",
        ),
      ),
      releasedFrom = "Hull",
      releasedUnder = "Legislation 123",
    )

    ppudAutomationUpdateReleaseApiMatchResponse(offenderId, sentenceId, updateReleaseRequest, id)

    // when
    val actual = ppudAutomationApiClient.createOrUpdateRelease(offenderId, sentenceId, updateReleaseRequest).block()

    // then
    assertThat(actual?.release?.id, equalTo(id))
  }

  @Test
  fun `create recall to ppud`() {
    // given
    val offenderId = "123"
    val releaseId = "456"
    val id = "12345678"

    ppudAutomationCreateRecallApiMatchResponse(offenderId, releaseId, id)

    // when
    val actual = ppudAutomationApiClient.createRecall(
      offenderId,
      releaseId,
      PpudCreateRecallRequest(
        decisionDateTime = LocalDateTime.of(2024, 1, 1, 12, 0),
        isExtendedSentence = false,
        isInCustody = true,
        mappaLevel = "Level 1",
        policeForce = "police force",
        probationArea = "probation area",
        receivedDateTime = LocalDateTime.of(2024, 1, 1, 14, 0),
        recommendedTo = PpudUser("", ""),
        riskOfContrabandDetails = "some details",
      ),
    ).block()

    // then
    assertThat(actual?.recall?.id, equalTo(id))
  }

  @Test
  fun `update mandatory document in ppud`() {
    val documentId = UUID.randomUUID()
    // given
    val recallId = "123"

    ppudAutomationUploadMandatoryDocumentApiMatchResponse(recallId)

    // when
    ppudAutomationApiClient.uploadMandatoryDocument(
      recallId,
      PpudUploadMandatoryDocumentRequest(
        documentId = documentId,
        category = DocumentCategory.PartA,
        owningCaseworker = PpudUser("", ""),
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `update additional document in ppud`() {
    val documentId = UUID.randomUUID()
    // given
    val recallId = "123"

    ppudAutomationUploadAdditionalDocumentApiMatchResponse(recallId)

    // when
    ppudAutomationApiClient.uploadAdditionalDocument(
      recallId,
      PpudUploadAdditionalDocumentRequest(
        documentId = documentId,
        title = "some title",
        owningCaseworker = PpudUser("", ""),
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `create minute in ppud`() {
    val documentId = UUID.randomUUID()
    // given
    val recallId = "123"

    ppudAutomationCreateMinuteApiMatchResponse(recallId)

    // when
    ppudAutomationApiClient.createMinute(
      recallId,
      PpudCreateMinuteRequest(
        subject = "some subject",
        text = "some title",
      ),
    ).block()

    // then
    // no exception
  }

  @Test
  fun `reference list`() {
    // given
    ppudAutomationReferenceListApiMatchResponse("custody-types")

    // when
    val actual = ppudAutomationApiClient.retrieveList("custody-types").block()

    // then
    assertThat(actual?.values, equalTo(listOf("one", "two")))
  }

  @Test
  fun `search active users`() {
    // given
    val searchReq = PpudUserSearchRequest("User Name", "UserName")
    var teamName = "TeamName"

    ppudAutomationSearchActiveUsersApiMatchResponse(searchReq.fullName!!, searchReq.userName!!, teamName)

    // when
    val actual = ppudAutomationApiClient.searchActiveUsers(searchReq).block()

    // then
    assertThat(actual?.results, equalTo(listOf(PpudUser(searchReq.fullName!!, teamName))))
  }
}
