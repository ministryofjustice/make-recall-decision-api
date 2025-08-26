package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.ZonedDateTime

/**
 * Configuration of the document templates to use for Part A, Preview Part A and DNTR documents. Each entry comes with
 * a timezoned date and time indicating when the service should start to use the associated template. The latest date
 * and time from those in the past is selected. Future dates and times can be specified to program switching to a new
 * template in advance, instead of relying on timing the deployment.
 */
@ConfigurationProperties(prefix = "document-template")
data class DocumentTemplateConfiguration(
  val partATemplateSettings: List<DocumentTemplateSetting> = listOf(),
  val dntrTemplateSettings: List<DocumentTemplateSetting> = listOf(),
)

data class DocumentTemplateSetting(
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mmZ")
  val startDateTime: ZonedDateTime,
  val templateName: String,
)
