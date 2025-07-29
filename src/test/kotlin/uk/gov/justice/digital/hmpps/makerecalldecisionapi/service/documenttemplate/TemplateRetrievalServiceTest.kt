package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateSettings
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class TemplateRetrievalServiceTest {

  private lateinit var templateRetrievalService: TemplateRetrievalService

  private val logAppender = findLogAppender(TemplateRetrievalService::class.java)

  private val currentDateTime: ZonedDateTime = ZonedDateTime.now()
  private val pastDocumentTemplateSetting = documentTemplateSettings(startDateTime = currentDateTime.minusMonths(2))
  private val currentDocumentTemplateSetting =
    documentTemplateSettings(startDateTime = currentDateTime.minusDays(2))
  private val futureDocumentTemplateSetting =
    documentTemplateSettings(startDateTime = currentDateTime.plusMonths(2))

  private val documentTemplateSettingsList =
    listOf(currentDocumentTemplateSetting, futureDocumentTemplateSetting, pastDocumentTemplateSetting)

  @Test
  fun `retrieves correct Part A template`() {
    val documentTemplateConfiguration = documentTemplateConfiguration(
      partATemplateSettings = documentTemplateSettingsList,
    )
    testLoadDocumentTemplate(documentTemplateConfiguration, DocumentType.PART_A_DOCUMENT)
  }

  @Test
  fun `retrieves correct Preview Part A template`() {
    val documentTemplateConfiguration = documentTemplateConfiguration(
      partAPreviewTemplateSettings = documentTemplateSettingsList,
    )
    testLoadDocumentTemplate(documentTemplateConfiguration, DocumentType.PREVIEW_PART_A_DOCUMENT)
  }

  @Test
  fun `retrieves correct DNTR template`() {
    val documentTemplateConfiguration = documentTemplateConfiguration(
      dntrTemplateSettings = documentTemplateSettingsList,
    )
    testLoadDocumentTemplate(documentTemplateConfiguration, DocumentType.DNTR_DOCUMENT)
  }

  private fun testLoadDocumentTemplate(
    documentTemplateConfiguration: DocumentTemplateConfiguration,
    documentType: DocumentType
  ) {
    // given
    templateRetrievalService = TemplateRetrievalService(documentTemplateConfiguration)

    val expectedTemplateName = currentDocumentTemplateSetting.templateName
    val expectedClassPathResource = ClassPathResource(expectedTemplateName)

    // when
    val actualClassPathResource = templateRetrievalService.loadDocumentTemplate(documentType)

    // then
    assertThat(actualClassPathResource).isEqualTo(expectedClassPathResource)
    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Retrieving ${documentType.name} template: $expectedTemplateName")
      }
    }
  }
}
