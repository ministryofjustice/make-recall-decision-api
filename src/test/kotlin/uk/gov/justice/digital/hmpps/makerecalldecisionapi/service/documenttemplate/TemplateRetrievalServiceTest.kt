package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateSetting
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateSettings
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import java.time.LocalDateTime
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class TemplateRetrievalServiceTest {

  private lateinit var templateRetrievalService: TemplateRetrievalService

  private val logAppender = findLogAppender(TemplateRetrievalService::class.java)

  private val currentDateTime: ZonedDateTime = ZonedDateTime.now()
  private val pastDateTime = currentDateTime.minusMonths(2)
  private val presentDateTime = currentDateTime.minusDays(2)
  private val futureDateTime = currentDateTime.plusMonths(2)
  private val pastDocumentTemplateSetting = documentTemplateSettings(startDateTime = pastDateTime, "past")
  private val currentDocumentTemplateSetting = documentTemplateSettings(startDateTime = presentDateTime, "present")
  private val futureDocumentTemplateSetting = documentTemplateSettings(startDateTime = futureDateTime, "future")
  private val defaultDocumentTemplateSetting = DocumentTemplateSetting(startDateTime = futureDateTime, "default")

  private val defaultSettingList =
    listOf(currentDocumentTemplateSetting, futureDocumentTemplateSetting, pastDocumentTemplateSetting)

  private val documentTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = defaultSettingList,
    partAPreviewTemplateSettings = defaultSettingList,
    dntrTemplateSettings = defaultSettingList,
  )

  private val noDocumentsTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = listOf(),
    partAPreviewTemplateSettings = listOf(),
    dntrTemplateSettings = listOf(),
  )

  private val futureOnlyDocumentsTemplateConfiguration = documentTemplateConfiguration(
    partATemplateSettings = listOf(futureDocumentTemplateSetting),
    partAPreviewTemplateSettings = listOf(futureDocumentTemplateSetting),
    dntrTemplateSettings = listOf(futureDocumentTemplateSetting),
  )

  val noExistingDocument: LocalDateTime? = null

  @Nested
  @DisplayName("Part A Documents")
  inner class PartADocument {
    @Test
    fun `retrieves the default Part A template when no settings provided`() {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        DocumentType.PART_A_DOCUMENT,
        noExistingDocument,
        defaultDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves the default Part A template when only configured setting is in the future`() {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        DocumentType.PART_A_DOCUMENT,
        currentDateTime.toLocalDateTime(),
        defaultDocumentTemplateSetting,
        true,
      )
    }

    @Test
    fun `retrieves correct Part A template - document created for the first time`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PART_A_DOCUMENT,
        noExistingDocument,
        currentDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct Part A template - document created against current template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PART_A_DOCUMENT,
        presentDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct Part A template - document created against past template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PART_A_DOCUMENT,
        pastDateTime.toLocalDateTime(),
        pastDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct Part A template - document created against future date still resolves to current when there is no future template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration(
          partATemplateSettings = listOf(pastDocumentTemplateSetting, currentDocumentTemplateSetting),
        ),
        DocumentType.PART_A_DOCUMENT,
        futureDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
        true,
      )
    }

    @Test
    fun `retrieves correct Part A template - document created against future date still resolves to current`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PART_A_DOCUMENT,
        futureDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
        true,
      )
    }
  }

  @Nested
  @DisplayName("Preview Part A Documents")
  inner class PreviewDocuments {
    @Test
    fun `retrieves the default Preview Part A template when no settings provided`() {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        noExistingDocument,
        defaultDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves the default Preview Part A template when only configured setting is in the future`() {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        currentDateTime.toLocalDateTime(),
        defaultDocumentTemplateSetting,
        true,
      )
    }

    @Test
    fun `retrieves correct Preview Part A template - document created for the first time`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        noExistingDocument,
        currentDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct Preview Part A template - document created against current template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        presentDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct Preview Part A template - document created against past template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        pastDateTime.toLocalDateTime(),
        pastDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct Preview Part A template - document created against future date still resolves to current when there is no future template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration(
          partAPreviewTemplateSettings = listOf(pastDocumentTemplateSetting, currentDocumentTemplateSetting),
        ),
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        futureDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
        true,
      )
    }

    @Test
    fun `retrieves correct Preview Part A template - document created against future date still resolves to current`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.PREVIEW_PART_A_DOCUMENT,
        futureDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
        true,
      )
    }
  }

  @Nested
  @DisplayName("Decision Not To Recall Documents")
  inner class DNTRDocuments {
    @Test
    fun `retrieves the default DNTR template when no settings provided`() {
      testLoadDocumentTemplate(
        noDocumentsTemplateConfiguration,
        DocumentType.DNTR_DOCUMENT,
        noExistingDocument,
        defaultDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves the default DNTR template when only configured setting is in the future`() {
      testLoadDocumentTemplate(
        futureOnlyDocumentsTemplateConfiguration,
        DocumentType.DNTR_DOCUMENT,
        currentDateTime.toLocalDateTime(),
        defaultDocumentTemplateSetting,
        true,
      )
    }

    @Test
    fun `retrieves correct DNTR template - document created for the first time`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.DNTR_DOCUMENT,
        noExistingDocument,
        currentDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct DNTR template - document created against current template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.DNTR_DOCUMENT,
        presentDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct DNTR template - document created against past template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.DNTR_DOCUMENT,
        pastDateTime.toLocalDateTime(),
        pastDocumentTemplateSetting,
      )
    }

    @Test
    fun `retrieves correct DNTR template - document created against future date still resolves to current when there is no future template`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration(
          dntrTemplateSettings = listOf(pastDocumentTemplateSetting, currentDocumentTemplateSetting),
        ),
        DocumentType.DNTR_DOCUMENT,
        futureDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
        true,
      )
    }

    @Test
    fun `retrieves correct DNTR template - document created against future date still resolves to current`() {
      testLoadDocumentTemplate(
        documentTemplateConfiguration,
        DocumentType.DNTR_DOCUMENT,
        futureDateTime.toLocalDateTime(),
        currentDocumentTemplateSetting,
        true,
      )
    }
  }

  private fun testLoadDocumentTemplate(
    documentTemplateConfiguration: DocumentTemplateConfiguration,
    documentType: DocumentType,
    documentCreated: LocalDateTime?,
    expectedSettings: DocumentTemplateSetting,
    expectFutureWarning: Boolean = false,
  ) {
    // given
    templateRetrievalService = TemplateRetrievalService(documentTemplateConfiguration)

    val expectedTemplatePath = when (documentType) {
      DocumentType.PART_A_DOCUMENT -> "partA/${expectedSettings.templateName}/Part A Template.docx"
      DocumentType.PREVIEW_PART_A_DOCUMENT -> "partA/${expectedSettings.templateName}/Preview Part A Template.docx"
      DocumentType.DNTR_DOCUMENT -> "dntr/${expectedSettings.templateName}/DNTR Template.docx"
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
        assertThat(message).isEqualTo("Retrieving ${documentType.name} - template name: ${expectedSettings.templateName} - resolved path: templates/$expectedTemplatePath")
      }
      if (expectFutureWarning) {
        with(get(0)) {
          assertThat(level).isEqualTo(Level.ERROR)
          assertThat(message).startsWith("Recommendation identified with future created date: ${documentCreated}Z[UTC] - Current date/time: ")
        }
      }
    }
  }
}
