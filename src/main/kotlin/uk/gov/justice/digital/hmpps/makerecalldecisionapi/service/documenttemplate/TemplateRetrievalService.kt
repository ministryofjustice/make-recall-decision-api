package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateSetting
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class TemplateRetrievalService(
  @Autowired private var documentTemplateConfig: DocumentTemplateConfiguration,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun loadDocumentTemplate(documentType: DocumentType, recommendationMetaData: RecommendationMetaData): ClassPathResource {
    val templateSettingsList = selectTemplateSettingsList(documentType)
    val templatePath = selectTemplatePath(templateSettingsList, documentType, recommendationMetaData.partADocumentCreated)

    return ClassPathResource(templatePath)
  }

  private fun selectTemplateSettingsList(documentType: DocumentType): List<DocumentTemplateSetting> = when (documentType) {
    DocumentType.PART_A_DOCUMENT -> documentTemplateConfig.partATemplateSettings
    DocumentType.PREVIEW_PART_A_DOCUMENT -> documentTemplateConfig.partATemplateSettings
    DocumentType.DNTR_DOCUMENT -> documentTemplateConfig.dntrTemplateSettings
  }

  private fun selectTemplatePath(templateSettingsList: List<DocumentTemplateSetting>, documentType: DocumentType, ppDocumentCreated: ZonedDateTime?): String {
    val currentDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
    val templateTargetDate =
      if (ppDocumentCreated === null) {
        currentDateTime
      } else if (ppDocumentCreated > currentDateTime) {
        log.error("Recommendation identified with future created date: $ppDocumentCreated - Current date/time: $currentDateTime")
        currentDateTime
      } else {
        ppDocumentCreated
      }

    val templateName = templateSettingsList.filter { it.startDateTime.isBefore(templateTargetDate) }.maxByOrNull { it.startDateTime }?.templateName ?: "default"

    val templatePath = "templates/${
      when (documentType) {
        DocumentType.PART_A_DOCUMENT -> "partA/$templateName/Part A Template.docx"
        DocumentType.PREVIEW_PART_A_DOCUMENT -> "partA/$templateName/Preview Part A Template.docx"
        DocumentType.DNTR_DOCUMENT -> "dntr/$templateName/DNTR Template.docx"
      }}"

    log.info("Retrieving ${documentType.name} - template name: $templateName - resolved path: $templatePath")
    return templatePath
  }
}
