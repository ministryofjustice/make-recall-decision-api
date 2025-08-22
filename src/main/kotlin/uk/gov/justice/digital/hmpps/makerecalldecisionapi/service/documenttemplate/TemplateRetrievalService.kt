package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateConfiguration
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DocumentTemplateSetting
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
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
    val templateSettings = selectTemplateSettings(templateSettingsList, documentType, recommendationMetaData.partADocumentCreated)

    log.info("Retrieving ${documentType.name} - template name: ${templateSettings.templateName} - resolved path: ${templateSettings.templatePath}")
    return ClassPathResource(templateSettings.templatePath!!)
  }

  private fun selectTemplateSettingsList(documentType: DocumentType): List<DocumentTemplateSetting> = when (documentType) {
    DocumentType.PART_A_DOCUMENT -> documentTemplateConfig.partATemplateSettings
    DocumentType.PREVIEW_PART_A_DOCUMENT -> documentTemplateConfig.partAPreviewTemplateSettings
    DocumentType.DNTR_DOCUMENT -> documentTemplateConfig.dntrTemplateSettings
  }

  private fun selectTemplateSettings(templateSettingsList: List<DocumentTemplateSetting>, documentType: DocumentType, ppDocumentCreated: LocalDateTime?): DocumentTemplateSetting {
    val currentDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
    val zonedCreatedDate = if (ppDocumentCreated !== null) ZonedDateTime.ofLocal(ppDocumentCreated, ZoneId.of("UTC"), ZoneOffset.UTC) else null
    val templateTargetDate =
      if (zonedCreatedDate === null) {
        currentDateTime
      } else if (zonedCreatedDate > currentDateTime) {
        log.error("Recommendation identified with future created date: $zonedCreatedDate - Current date/time: $currentDateTime")
        currentDateTime
      } else {
        zonedCreatedDate
      }

    val settings = templateSettingsList.filter { it.startDateTime.isBefore(templateTargetDate) }.maxByOrNull { it.startDateTime }
      ?: DocumentTemplateSetting(startDateTime = currentDateTime, templateName = "default", templatePath = "")

    val templatePath = when (documentType) {
      DocumentType.PART_A_DOCUMENT -> "partA/${settings.templateName}/Part A Template.docx"
      DocumentType.PREVIEW_PART_A_DOCUMENT -> "partA/${settings.templateName}/Preview Part A Template.docx"
      DocumentType.DNTR_DOCUMENT -> "dntr/${settings.templateName}/DNTR Template.docx"
    }

    println(" Current date: $currentDateTime")
    println("Document date: $ppDocumentCreated")
    println("   Zoned date: $zonedCreatedDate")
    println("template target date: $templateTargetDate")
    println("settings list: $templateSettingsList")
    println("settings: $settings")
    println("template path: $templatePath")

    return DocumentTemplateSetting(startDateTime = settings.startDateTime, templateName = settings.templateName, templatePath = "templates/$templatePath")
  }
}
