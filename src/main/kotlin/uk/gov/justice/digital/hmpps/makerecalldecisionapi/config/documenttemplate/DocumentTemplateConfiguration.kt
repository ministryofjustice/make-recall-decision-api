package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@ConfigurationProperties(prefix = "document-template")
data class DocumentTemplateConfiguration(
  val partATemplateSettings: List<DocumentTemplateSetting>,
  val partAPreviewTemplateSettings: List<DocumentTemplateSetting>,
  val dntrTemplateSettings: List<DocumentTemplateSetting>,
)

data class DocumentTemplateSetting(
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  val startDateTime: LocalDateTime,
  val templateName: String,
)
