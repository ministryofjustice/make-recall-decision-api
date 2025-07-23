package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.documentTemplateSettings
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class TemplateRetrievalServiceTest {

  private lateinit var templateRetrievalService: TemplateRetrievalService

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
    // given
    val documentTemplateConfiguration = documentTemplateConfiguration(
      partATemplateSettings = documentTemplateSettingsList,
    )
    templateRetrievalService = TemplateRetrievalService(documentTemplateConfiguration)

    val expectedClassPathResource = ClassPathResource(currentDocumentTemplateSetting.templateName)

    // when
    val actualClassPathResource = templateRetrievalService.loadDocumentTemplate(DocumentType.PART_A_DOCUMENT)

    // then
    assertThat(actualClassPathResource).isEqualTo(expectedClassPathResource)
  }

  @Test
  fun `retrieves correct Preview Part A template`() {
    // given
    val documentTemplateConfiguration = documentTemplateConfiguration(
      partAPreviewTemplateSettings = documentTemplateSettingsList,
    )
    templateRetrievalService = TemplateRetrievalService(documentTemplateConfiguration)

    val expectedClassPathResource = ClassPathResource(currentDocumentTemplateSetting.templateName)

    // when
    val actualClassPathResource = templateRetrievalService.loadDocumentTemplate(DocumentType.PREVIEW_PART_A_DOCUMENT)

    // then
    assertThat(actualClassPathResource).isEqualTo(expectedClassPathResource)
  }

  @Test
  fun `retrieves correct DNTR template`() {
    // given
    val documentTemplateConfiguration = documentTemplateConfiguration(
      dntrTemplateSettings = documentTemplateSettingsList,
    )
    templateRetrievalService = TemplateRetrievalService(documentTemplateConfiguration)

    val expectedClassPathResource = ClassPathResource(currentDocumentTemplateSetting.templateName)

    // when
    val actualClassPathResource = templateRetrievalService.loadDocumentTemplate(DocumentType.DNTR_DOCUMENT)

    // then
    assertThat(actualClassPathResource).isEqualTo(expectedClassPathResource)
  }
}
