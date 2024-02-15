package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.featureflags.FeatureFlags
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationSupportingDocumentEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationSupportingDocumentRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class SupportingDocumentServiceTest {

  @Test
  fun `upload supporting document`() {
    val repository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val created = DateTimeHelper.utcNowDateTimeString()

    SupportingDocumentService(repository).uploadNewSupportingDocument(
      recommendationId = 123,
      type = "PPUDPartA",
      mimetype = "word",
      filename = "word.docx",
      created = created,
      createdBy = "daman",
      createdByUserFullName = "Da Man",
      data = "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=",
      flags = FeatureFlags(),
    )

    val captor = argumentCaptor<RecommendationSupportingDocumentEntity>()

    Mockito.verify(repository).save(captor.capture())

    val entity = captor.firstValue

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.mimetype).isEqualTo("word")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Da Man")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Da Man")
    assertThat(String(entity.data)).isEqualTo("The hills are alive with thet sound of music")
  }

  @Test
  fun `retrieve`() {
    val repository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)

    val created = DateTimeHelper.utcNowDateTimeString()

    given(repository.findByRecommendationId(123)).willReturn(
      listOf(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          createdBy = "daman",
          createdByUserFullName = "Da Man",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Da Man",
          uploaded = created,
          data = "".encodeToByteArray(),
        ),
      ),
    )

    val response = SupportingDocumentService(repository).fetchSupportingDocuments(123)

    assertThat(response.size).isEqualTo(1)

    val entity = response[0]

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")

    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Da Man")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Da Man")
  }
}
