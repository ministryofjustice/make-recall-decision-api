package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateSetting
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import java.time.ZonedDateTime

@Service
class TemplateRetrievalService(
  @Autowired private var documentTemplateConfig: DocumentTemplateConfiguration,
) {

  fun loadDocumentTemplate(documentType: DocumentType): ClassPathResource {
    val templateSettingsList = selectTemplateSettingsList(documentType)
    val currentTemplateSettings = selectCurrentTemplateSettings(templateSettingsList)

    return ClassPathResource(currentTemplateSettings.templateName)
  }

  private fun selectTemplateSettingsList(documentType: DocumentType): List<DocumentTemplateSetting> = when (documentType) {
    DocumentType.PART_A_DOCUMENT -> documentTemplateConfig.partATemplateSettings
    DocumentType.PREVIEW_PART_A_DOCUMENT -> documentTemplateConfig.partAPreviewTemplateSettings
    DocumentType.DNTR_DOCUMENT -> documentTemplateConfig.dntrTemplateSettings
  }

  private fun selectCurrentTemplateSettings(templateSettingsList: List<DocumentTemplateSetting>): DocumentTemplateSetting {
    val currentDateTime = ZonedDateTime.now()

    return templateSettingsList.filter { it.startDateTime.isBefore(currentDateTime) }.maxByOrNull { it.startDateTime }!!
  }
}
