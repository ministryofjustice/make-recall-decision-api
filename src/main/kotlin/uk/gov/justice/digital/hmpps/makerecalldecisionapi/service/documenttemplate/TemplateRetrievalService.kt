package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.TemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import java.time.ZonedDateTime

@Service
class TemplateRetrievalService(
  @Autowired private var templateVersionRetrievalService: TemplateVersionRetrievalService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun loadDocumentTemplate(
    documentType: DocumentType,
    recommendationMetaData: RecommendationMetaData,
  ): ClassPathResource {
    val templateVersion: TemplateVersion =
      retrieveTemplateVersion(documentType, recommendationMetaData.partADocumentCreated)
    val templatePath =
      selectTemplatePath(templateVersion, documentType)

    return ClassPathResource(templatePath)
  }

  private fun retrieveTemplateVersion(
    documentType: DocumentType,
    dateTimeOfFirstDownload: ZonedDateTime?,
  ): TemplateVersion = when (documentType) {
    DocumentType.PART_A_DOCUMENT, DocumentType.PREVIEW_PART_A_DOCUMENT -> templateVersionRetrievalService.partATemplateVersion(
      dateTimeOfFirstDownload,
    )

    DocumentType.DNTR_DOCUMENT -> templateVersionRetrievalService.dntrTemplateVersion(dateTimeOfFirstDownload)
  }

  private fun selectTemplatePath(
    templateVersion: TemplateVersion,
    documentType: DocumentType,
  ): String {
    val templatePath = "templates/${
      when (documentType) {
        DocumentType.PART_A_DOCUMENT -> "partA/${templateVersion.path}/Part A Template.docx"
        DocumentType.PREVIEW_PART_A_DOCUMENT -> "partA/${templateVersion.path}/Preview Part A Template.docx"
        DocumentType.DNTR_DOCUMENT -> "dntr/${templateVersion.path}/DNTR Template.docx"
      }
    }"

    log.info("Retrieving ${documentType.name} - template name: ${templateVersion.path} - resolved path: $templatePath")
    return templatePath
  }
}
