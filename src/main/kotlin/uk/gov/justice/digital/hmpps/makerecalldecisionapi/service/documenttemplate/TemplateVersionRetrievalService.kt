package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DntrTemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.PartATemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentTemplateNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.featureflag.FeatureFlagService
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class TemplateVersionRetrievalService(
  @Autowired private var featureFlagService: FeatureFlagService,
  @Autowired @Qualifier("fliptDateTimeFormatter") private var dateTimeFormatter: DateTimeFormatter,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun partATemplateVersion(dateTimeOfFirstDownload: ZonedDateTime?): PartATemplateVersion {
    val currentDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
    val templateTargetDate =
      if (dateTimeOfFirstDownload === null) {
        currentDateTime
      } else if (dateTimeOfFirstDownload > currentDateTime) {
        log.error("Recommendation identified with future created date: $dateTimeOfFirstDownload - Current date/time: $currentDateTime")
        currentDateTime
      } else {
        dateTimeOfFirstDownload
      }
    val variantKey = featureFlagService.variant(
      "part-a-template-version",
      mapOf("dateTimeOfFirstDownload" to templateTargetDate.format(dateTimeFormatter)),
    )
    if (variantKey != null) {
      try {
        return PartATemplateVersion.forVariantKey(variantKey)
      } catch (_: NoSuchElementException) {
        throw DocumentTemplateNotFoundException("No Part A template version found for variant key: $variantKey")
      }
    }
    throw DocumentTemplateNotFoundException("No Part A template version found (received null version)")
  }

  fun dntrTemplateVersion(dateTimeOfFirstDownload: ZonedDateTime?): DntrTemplateVersion = DntrTemplateVersion.DEFAULT
}
