package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentMatcher
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argThat
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DntrTemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.PartATemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentTemplateNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.featureflag.FeatureFlagService
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomFutureZonedDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomPastZonedDateTime
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

@ExtendWith(MockitoExtension::class)
class TemplateVersionRetrievalServiceTest {

  @InjectMocks
  private lateinit var service: TemplateVersionRetrievalService

  @Mock
  private lateinit var featureFlagService: FeatureFlagService

  @Mock
  private lateinit var dateTimeFormatter: DateTimeFormatter

  private val logAppender = findLogAppender(TemplateVersionRetrievalService::class.java)

  @ParameterizedTest
  @EnumSource
  fun `returns expected enum value when retrieving part A version for previously downloaded document`(
    expectedPartATemplateVersion: PartATemplateVersion,
  ) {
    // given
    val dateTimeOfFirstDownload = randomPastZonedDateTime()
    val formattedDateTime = randomString()
    whenever(dateTimeFormatter.format(dateTimeOfFirstDownload)).thenReturn(formattedDateTime)
    whenever(
      featureFlagService.variant(
        "part-a-template-version",
        mapOf("dateTimeOfFirstDownload" to formattedDateTime),
      ),
    ).thenReturn(expectedPartATemplateVersion.flagVariantKey)

    // when
    val actualPartATemplateVersion = service.partATemplateVersion(dateTimeOfFirstDownload)

    // then
    assertThat(actualPartATemplateVersion).isEqualTo(expectedPartATemplateVersion)
  }

  @ParameterizedTest
  @EnumSource
  fun `returns expected enum value when retrieving part A version for first-time downloads`(partATemplateVersion: PartATemplateVersion) {
    // given
    val dateTimeOfFirstDownload = null
    val formattedDateTime = randomString()
    whenever(dateTimeFormatter.format(argThat(hasRecentCurrentDateTime()))).thenReturn(formattedDateTime)
    whenever(
      featureFlagService.variant(
        "part-a-template-version",
        mapOf("dateTimeOfFirstDownload" to formattedDateTime),
      ),
    ).thenReturn(partATemplateVersion.flagVariantKey)

    // when
    val actualPartATemplateVersion = service.partATemplateVersion(dateTimeOfFirstDownload)

    // then
    assertThat(actualPartATemplateVersion).isEqualTo(partATemplateVersion)
  }

  @ParameterizedTest
  @EnumSource
  fun `returns expected enum value when retrieving part A version for documents with future download date-times`(
    partATemplateVersion: PartATemplateVersion,
  ) {
    // given
    val dateTimeOfFirstDownload = randomFutureZonedDateTime()
    val formattedDateTime = randomString()
    whenever(dateTimeFormatter.format(argThat(hasRecentCurrentDateTime()))).thenReturn(formattedDateTime)
    whenever(
      featureFlagService.variant(
        "part-a-template-version",
        mapOf("dateTimeOfFirstDownload" to formattedDateTime),
      ),
    ).thenReturn(partATemplateVersion.flagVariantKey)

    // when
    val actualPartATemplateVersion = service.partATemplateVersion(dateTimeOfFirstDownload)

    // then
    assertThat(actualPartATemplateVersion).isEqualTo(partATemplateVersion)
    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.ERROR)
        assertThat(message).startsWith("Recommendation identified with future created date: $dateTimeOfFirstDownload - Current date/time: ")
      }
    }
  }

  @Test
  fun `raises an exception when retrieving part A version if feature flag variant returned is not recognised`() {
    // given
    val dateTimeOfFirstDownload = randomPastZonedDateTime()
    val formattedDateTime = randomString()
    whenever(dateTimeFormatter.format(dateTimeOfFirstDownload)).thenReturn(formattedDateTime)
    val variantKey = randomString()
    whenever(
      featureFlagService.variant(
        "part-a-template-version",
        mapOf("dateTimeOfFirstDownload" to formattedDateTime),
      ),
    ).thenReturn(variantKey)

    // when
    assertThatThrownBy {
      service.partATemplateVersion(dateTimeOfFirstDownload)
    }
      // then
      .isInstanceOf(DocumentTemplateNotFoundException::class.java)
      .hasMessage("No Part A template version found for variant key: $variantKey")
  }

  @Test
  fun `raises an exception when retrieving part A version if no feature flag variant is returned`() {
    // given
    val dateTimeOfFirstDownload = randomPastZonedDateTime()
    val formattedDateTime = randomString()
    whenever(dateTimeFormatter.format(dateTimeOfFirstDownload)).thenReturn(formattedDateTime)
    whenever(
      featureFlagService.variant(
        "part-a-template-version",
        mapOf("dateTimeOfFirstDownload" to formattedDateTime),
      ),
    ).thenReturn(null)

    // when
    assertThatThrownBy {
      service.partATemplateVersion(dateTimeOfFirstDownload)
    }
      // then
      .isInstanceOf(DocumentTemplateNotFoundException::class.java)
      .hasMessage("No Part A template version found (received null version)")
  }

  @Test
  fun `returns default DNTR version`() {
    // given
    val dateTimeOfFirstDownload = randomZonedDateTime()

    // when
    val actualDntrTemplateVersion = service.dntrTemplateVersion(dateTimeOfFirstDownload)

    // then
    assertThat(actualDntrTemplateVersion).isEqualTo(DntrTemplateVersion.DEFAULT)
  }

  // We can't get the exact time at which the method will get the date-time, so we check it's close enough
  private fun hasRecentCurrentDateTime() = ArgumentMatcher<TemporalAccessor> { actualDateTime ->
    LocalDateTime.now().isBefore(LocalDateTime.from(actualDateTime).plusSeconds(1))
  }
}
