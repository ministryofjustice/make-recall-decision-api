package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import ch.qos.logback.classic.Level
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.core.io.ClassPathResource
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.DntrTemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.PartATemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.documenttemplate.TemplateVersion
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DocumentType
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.DocumentTemplateNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.recommendation.RecommendationMetaData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.findLogAppender
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomEnum
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.testutil.randomZonedDateTime

@ExtendWith(MockitoExtension::class)
class TemplateRetrievalServiceTest {

  @InjectMocks
  private lateinit var templateRetrievalService: TemplateRetrievalService

  @Mock
  private lateinit var templateVersionRetrievalService: TemplateVersionRetrievalService

  private val logAppender = findLogAppender(TemplateRetrievalService::class.java)

  @ParameterizedTest
  @EnumSource
  fun `retrieves the expected document template when a valid template version was found`(
    documentType: DocumentType,
  ) {
    // given
    val dateTimeOfFirstDownload = randomZonedDateTime()
    val expectedTemplateVersion: TemplateVersion =
      if (documentType == DocumentType.PART_A_DOCUMENT || documentType == DocumentType.PREVIEW_PART_A_DOCUMENT) {
        val expectedPartATemplateVersion = randomEnum<PartATemplateVersion>()
        whenever(templateVersionRetrievalService.partATemplateVersion(dateTimeOfFirstDownload)).thenReturn(
          expectedPartATemplateVersion,
        )
        expectedPartATemplateVersion
      } else {
        val expectedDntrTemplateVersion = randomEnum<DntrTemplateVersion>()
        whenever(templateVersionRetrievalService.dntrTemplateVersion(dateTimeOfFirstDownload)).thenReturn(
          expectedDntrTemplateVersion,
        )
        expectedDntrTemplateVersion
      }

    val expectedTemplatePath = when (documentType) {
      DocumentType.PART_A_DOCUMENT -> "partA/${expectedTemplateVersion.path}/Part A Template.docx"
      DocumentType.PREVIEW_PART_A_DOCUMENT -> "partA/${expectedTemplateVersion.path}/Preview Part A Template.docx"
      DocumentType.DNTR_DOCUMENT -> "dntr/${expectedTemplateVersion.path}/DNTR Template.docx"
    }
    val expectedClassPathResource = ClassPathResource("templates/$expectedTemplatePath")

    // when
    val actualClassPathResource = templateRetrievalService.loadDocumentTemplate(
      documentType,
      RecommendationMetaData(partADocumentCreated = dateTimeOfFirstDownload),
    )

    // then
    assertThat(actualClassPathResource).isEqualTo(expectedClassPathResource)
    with(logAppender.list) {
      assertThat(size).isEqualTo(1)
      with(get(0)) {
        assertThat(level).isEqualTo(Level.INFO)
        assertThat(message).isEqualTo("Retrieving ${documentType.name} - template name: ${expectedTemplateVersion.path} - resolved path: templates/$expectedTemplatePath")
      }
    }
  }

  @ParameterizedTest
  @EnumSource
  fun `allows the exception raised by the version retrieval service to bubble up`(
    documentType: DocumentType,
  ) {
    // given
    val dateTimeOfFirstDownload = randomZonedDateTime()

    val documentTemplateNotFoundException = DocumentTemplateNotFoundException(randomString())
    if (documentType == DocumentType.PART_A_DOCUMENT || documentType == DocumentType.PREVIEW_PART_A_DOCUMENT) {
      whenever(templateVersionRetrievalService.partATemplateVersion(dateTimeOfFirstDownload)).thenThrow(
        documentTemplateNotFoundException,
      )
    } else {
      whenever(templateVersionRetrievalService.dntrTemplateVersion(dateTimeOfFirstDownload)).thenThrow(
        documentTemplateNotFoundException,
      )
    }

    // when
    assertThatThrownBy {
      templateRetrievalService.loadDocumentTemplate(
        documentType,
        RecommendationMetaData(partADocumentCreated = dateTimeOfFirstDownload),
      )
    }
      // then
      .isEqualTo(documentTemplateNotFoundException)
  }
}
