package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateSetting
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateSettings
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import java.time.ZoneId
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class TemplateRetrievalServiceTest {

  private lateinit var templateRetrievalService: TemplateRetrievalService

  private val logAppender = findLogAppender(TemplateRetrievalService::class.java)

  private val currentDateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
  private val pastDate = currentDateTime.minusMonths(2)
  private val presentDate = currentDateTime.minusDays(2)
  private val futureDate = currentDateTime.plusMonths(2)

  private val noExistingCreated: ZonedDateTime? = null

  // The document needs to be created after the date a timm  period starts as an equal time fall into the previous timeframe
  private val pastDocumentCreated = pastDate.plusDays(1)
  private val presentDocumentCreated = presentDate.plusDays(1)
  private val futureDocumentCreated = futureDate.plusDays(1)

  private val defaultDocumentTemplateSetting = DocumentTemplateSetting(startDateTime = futureDate, "default")
  private val pastDocumentTemplateSetting = documentTemplateSettings(startDateTime = pastDate, "past")
  private val presentDocumentTemplateSetting = documentTemplateSettings(startDateTime = presentDate, "present")
  private val futureDocumentTemplateSetting = documentTemplateSettings(startDateTime = futureDate, "future")

  private val defaultSettingList =
    listOf(presentDocumentTemplateSetting, futureDocumentTemplateSetting, pastDocumentTemplateSetting)

  private val documentTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = defaultSettingList,
    dntrTemplateSettings = defaultSettingList,
  )
  private val noDocumentsTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = listOf(),
    dntrTemplateSettings = listOf(),
  )
  private val noFutureDocumentsTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = listOf(pastDocumentTemplateSetting, presentDocumentTemplateSetting),
    dntrTemplateSettings = listOf(pastDocumentTemplateSetting, presentDocumentTemplateSetting),
  )
  private val futureOnlyDocumentsTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = listOf(futureDocumentTemplateSetting),
    dntrTemplateSettings = listOf(futureDocumentTemplateSetting),
  )

  @Nested
  @DisplayName("when no document template config provided")
  inner class RetrieveDefaultNoSettings {
    @ParameterizedTest
    @EnumSource
    fun `and the document has not been created before then the default template is retrieved`(
      documentType: DocumentType,
    ) {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        documentType,
        noExistingCreated,
        defaultDocumentTemplateSetting.templateName,
      )
    }

    @ParameterizedTest
    @EnumSource
    fun `and the document created in the past then the default template is retrieved`(documentType: DocumentType) {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        documentType,
        pastDocumentCreated,
        defaultDocumentTemplateSetting.templateName,
      )
    }

    @ParameterizedTest
    @EnumSource
    fun `and the document has been created in the present then the default template is retrieved`(
      documentType: DocumentType,
    ) {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        documentType,
        presentDocumentCreated,
        defaultDocumentTemplateSetting.templateName,
      )
    }

    @ParameterizedTest
    @EnumSource
    fun `and the document has created in the future then the default template is retrieved`(
      documentType: DocumentType,
    ) {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        documentType,
        futureDocumentCreated,
        defaultDocumentTemplateSetting.templateName,
        true,
      )
    }
  }

  @Nested
  @DisplayName("when the only document template config is in the future")
  inner class RetrieveDefaultFutureOnly {
    @ParameterizedTest
    @EnumSource
    fun `and the document has not been created before then the default template is retrieved`(
      documentType: DocumentType,
    ) {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        documentType,
        noExistingCreated,
        defaultDocumentTemplateSetting.templateName,
      )
    }

    @ParameterizedTest
    @EnumSource
    fun `and the document created in the past then the default template is retrieved`(documentType: DocumentType) {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        documentType,
        pastDocumentCreated,
        defaultDocumentTemplateSetting.templateName,
      )
    }

    @ParameterizedTest
    @EnumSource
    fun `and the document has been created in the present then the default template is retrieved`(
      documentType: DocumentType,
    ) {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        documentType,
        presentDocumentCreated,
        defaultDocumentTemplateSetting.templateName,
      )
    }

    @ParameterizedTest
    @EnumSource
    fun `and the document has created in the future then the default template is retrieved`(
      documentType: DocumentType,
    ) {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        documentType,
        futureDocumentCreated,
        defaultDocumentTemplateSetting.templateName,
        true,
      )
    }
  }

  @ParameterizedTest
  @EnumSource
  fun `retrieves correct past template - document created in a past time frame`(documentType: DocumentType) {
    testLoadDocumentTemplate(
      documentTemplateConfiguration,
      documentType,
      pastDocumentCreated,
      pastDocumentTemplateSetting.templateName,
    )
  }

  @ParameterizedTest
  @EnumSource
  fun `retrieves correct current template - document created for the first time`(documentType: DocumentType) {
    testLoadDocumentTemplate(
      documentTemplateConfiguration,
      documentType,
      noExistingCreated,
      presentDocumentTemplateSetting.templateName,
    )
  }

  @ParameterizedTest
  @EnumSource
  fun `retrieves correct current template - document created within the current time frame`(documentType: DocumentType) {
    testLoadDocumentTemplate(
      documentTemplateConfiguration,
      documentType,
      presentDocumentCreated,
      presentDocumentTemplateSetting.templateName,
    )
  }

  @ParameterizedTest
  @EnumSource
  fun `retrieves correct Part A template - document created against future date still resolves to current`(documentType: DocumentType) {
    testLoadDocumentTemplate(
      documentTemplateConfiguration,
      documentType,
      futureDocumentCreated,
      presentDocumentTemplateSetting.templateName,
      true,
    )
  }

  @ParameterizedTest
  @EnumSource
  fun `retrieves correct current template - document created against future date still resolves to current when there is no future template`(documentType: DocumentType) {
    testLoadDocumentTemplate(
      noFutureDocumentsTemplateConfiguration,
      documentType,
      futureDocumentCreated,
      presentDocumentTemplateSetting.templateName,
      true,
    )
  }

  private fun testLoadDocumentTemplate(
    documentTemplateConfiguration: DocumentTemplateConfiguration,
    documentType: DocumentType,
    documentCreated: ZonedDateTime?,
    expectedTemplate: String,
    expectFutureWarning: Boolean = false,
  ) {
    // given
    templateRetrievalService = TemplateRetrievalService(documentTemplateConfiguration)

    val expectedTemplatePath = when (documentType) {
      DocumentType.PART_A_DOCUMENT -> "partA/$expectedTemplate/Part A Template.docx"
      DocumentType.PREVIEW_PART_A_DOCUMENT -> "partA/$expectedTemplate/Preview Part A Template.docx"
      DocumentType.DNTR_DOCUMENT -> "dntr/$expectedTemplate/DNTR Template.docx"
    }
    val expectedClassPathResource = ClassPathResource("templates/$expectedTemplatePath")

    // when
    val actualClassPathResource = templateRetrievalService.loadDocumentTemplate(
      documentType,
      RecommendationMetaData(partADocumentCreated = documentCreated),
    )

    // then
    assertThat(actualClassPathResource).isEqualTo(expectedClassPathResource)
    with(logAppender.list) {
      assertThat(size).isEqualTo(if (expectFutureWarning) 2 else 1)
      with(get(if (expectFutureWarning) 1 else 0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Retrieving ${documentType.name} - template name: $expectedTemplate - resolved path: templates/$expectedTemplatePath")
      }
      if (expectFutureWarning) {
        with(get(0)) {
          assertThat(level).isEqualTo(Level.ERROR)
          assertThat(message).startsWith("Recommendation identified with future created date: $documentCreated - Current date/time: ")
        }
      }
    }
  }
}
