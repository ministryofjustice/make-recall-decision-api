package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomLocalDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import java.time.LocalDateTime

internal fun documentTemplateConfiguration(
  partATemplateSettings: List<DocumentTemplateSetting> = List(2, { documentTemplateSettings() }),
  partAPreviewTemplateSettings: List<DocumentTemplateSetting> = List(2, { documentTemplateSettings() }),
  dntrTemplateSettings: List<DocumentTemplateSetting> = List(2, { documentTemplateSettings() }),
) = DocumentTemplateConfiguration(partATemplateSettings, partAPreviewTemplateSettings, dntrTemplateSettings)

internal fun documentTemplateSettings(
  startDateTime: LocalDateTime = randomLocalDateTime(),
  templateName: String = randomString(),
) = DocumentTemplateSetting(startDateTime, templateName)
