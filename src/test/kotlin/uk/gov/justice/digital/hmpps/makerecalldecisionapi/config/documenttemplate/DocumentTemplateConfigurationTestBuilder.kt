package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate

import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime
import java.time.ZonedDateTime

internal fun documentTemplateConfiguration(
  partATemplateSettings: List<DocumentTemplateSetting> = List(2, { documentTemplateSettings() }),
  dntrTemplateSettings: List<DocumentTemplateSetting> = List(2, { documentTemplateSettings() }),
) = DocumentTemplateConfiguration(partATemplateSettings, dntrTemplateSettings)

internal fun documentTemplateSettings(
  startDateTime: ZonedDateTime = randomZonedDateTime(),
  templateName: String = randomString(),
) = DocumentTemplateSetting(startDateTime, templateName)
