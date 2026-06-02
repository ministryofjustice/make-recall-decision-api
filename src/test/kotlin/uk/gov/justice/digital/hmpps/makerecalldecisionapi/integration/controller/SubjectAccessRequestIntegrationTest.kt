package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.controller

import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.MrdTestDataBuilder
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper
import uk.gov.justice.digital.hmpps.subjectaccessrequest.rendering.RenderRequestInfo
import uk.gov.justice.digital.hmpps.subjectaccessrequest.templates.InlineAttachment
import uk.gov.justice.digital.hmpps.subjectaccessrequest.templates.TemplateDataFetcherFacade
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@ExperimentalCoroutinesApi
@Import(SubjectAccessRequestIntegrationTest.SarTestConfig::class)
class SubjectAccessRequestIntegrationTest : IntegrationTestBase() {

  @TestConfiguration
  open class SarTestConfig {
    // JwtAuthorisationHelper auto-configuration is excluded globally in application-test.yml to prevent it
    // overriding the project's JwtDecoder in all other test contexts. We provide it explicitly here so only
    // the SAR test context uses the library's key pair and matching decoders.
    @Bean
    open fun jwtAuthorisationHelper(): JwtAuthorisationHelper = JwtAuthorisationHelper()

    @Bean
    open fun jwtDecoder(jwtAuthorisationHelper: JwtAuthorisationHelper): JwtDecoder = jwtAuthorisationHelper.jwtDecoder()

    @Bean
    open fun reactiveJwtDecoder(jwtAuthorisationHelper: JwtAuthorisationHelper): ReactiveJwtDecoder = jwtAuthorisationHelper.reactiveJwtDecoder()

    @Bean
    open fun sarIntegrationTestHelper(
      jwtAuthorisationHelper: JwtAuthorisationHelper,
      @Value("\${hmpps.sar.tests.expected-api-response.path:}") expectedApiResponsePath: String,
      @Value("\${hmpps.sar.tests.expected-render-result.path:}") expectedRenderResultPath: String,
      @Value("\${hmpps.sar.tests.attachments-expected:false}") attachmentsExpected: Boolean,
    ): SarIntegrationTestHelper = SarIntegrationTestHelper(
      jwtAuthHelper = jwtAuthorisationHelper,
      expectedApiResponsePath = expectedApiResponsePath,
      expectedRenderResultPath = expectedRenderResultPath,
      attachmentsExpected = attachmentsExpected,
      expectedFlywaySchemaVersion = "0",
      expectedJpaEntitySchemaPath = "",
      templateDataFetcherFacade = object : TemplateDataFetcherFacade {
        override fun findPrisonNameByPrisonId(prisonId: String): String = prisonId
        override fun findUserLastNameByUsername(username: String): String = username
        override fun findLocationNameByNomisId(nomisId: Int): String = nomisId.toString()
        override fun findLocationNameByDpsId(dpsId: String): String = dpsId
        override fun getRenderableAttachment(attachment: InlineAttachment, renderRequestInfo: RenderRequestInfo): ByteArray = ByteArray(0)
      },
    )
  }

  @Autowired
  lateinit var sarIntegrationTestHelper: SarIntegrationTestHelper

  private fun setupTestData() {
    val fixedDate = LocalDate.of(2024, 1, 1)
    val fixedDateTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0)

    val entity = MrdTestDataBuilder.recommendationDataEntityData(crn)
    val fixedData = entity.data.copy(
      decisionDateTime = fixedDateTime,
      dateVloInformed = fixedDate,
      bookRecallToPpud = entity.data.bookRecallToPpud?.copy(
        decisionDateTime = fixedDateTime,
        receivedDateTime = fixedDateTime,
        sentenceDate = fixedDate,
      ),
      nomisIndexOffence = entity.data.nomisIndexOffence?.copy(
        allOptions = entity.data.nomisIndexOffence?.allOptions?.map { offence ->
          offence.copy(
            sentenceDate = fixedDate,
            sentenceStartDate = fixedDate,
            sentenceEndDate = fixedDate,
            sentenceSequenceExpiryDate = fixedDate,
          )
        },
      ),
    )
    repository.deleteAll()
    repository.save(entity.copy(data = fixedData))
  }

  @Test
  fun `SAR API should return expected data`() {
    setupTestData()

    val response = sarIntegrationTestHelper.requestSarData(null, crn, null, null, webTestClient)
    if (System.getenv("SAR_GENERATE_ACTUAL").toBoolean()) {
      sarIntegrationTestHelper.saveSarApiResponse(response)
    } else {
      assertThatJson(sarIntegrationTestHelper.toJson(response)).`as`("Response content json")
        .isEqualTo(sarIntegrationTestHelper.getExpectedSarJson())
      assertThat(response.attachments?.isNotEmpty() == true).`as`("Response has attachments")
        .isEqualTo(sarIntegrationTestHelper.attachmentsExpected)
    }
  }

  @Test
  fun `SAR report should render as expected`() {
    setupTestData()

    val dataResponse = sarIntegrationTestHelper.requestSarData(null, crn, null, null, webTestClient)
    val templateResponse = sarIntegrationTestHelper.requestSarTemplate(webTestClient)

    val renderResult = sarIntegrationTestHelper.renderServiceReport(
      data = dataResponse.content,
      templateVersion = "1.0",
      template = templateResponse,
    )

    sarIntegrationTestHelper.renderAndSaveReportAsPdf(renderResult, null, crn)
    sarIntegrationTestHelper.saveGeneratedReport(renderResult)

    if (System.getenv("SAR_GENERATE_ACTUAL").toBoolean()) {
      // fixture already saved above — nothing more to do
    } else {
      sarIntegrationTestHelper.assertHtmlEquals(renderResult, sarIntegrationTestHelper.getExpectedRenderResult())
    }
  }
}
