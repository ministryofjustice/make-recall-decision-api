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
import java.util.*

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
class SupportingDocumentServiceTest {

  @Test
  fun `upload supporting document`() {
    val repository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)
    val created = DateTimeHelper.utcNowDateTimeString()

    given(repository.save(Mockito.any())).willReturn(
      RecommendationSupportingDocumentEntity(
        id = 456,
        recommendationId = 123,
        mimetype = "word",
        filename = "word.docx",
        created = created,
        createdBy = "daman",
        createdByUserFullName = "Inspector Morris",
        data = "while I pondered, weak and weary".encodeToByteArray(),
        uploadedBy = null,
        uploadedByUserFullName = null,
        type = "PPUDPartA",
      ),
    )

    val id = SupportingDocumentService(repository).uploadNewSupportingDocument(
      recommendationId = 123,
      type = "PPUDPartA",
      mimetype = "word",
      filename = "word.docx",
      created = created,
      createdBy = "daman",
      createdByUserFullName = "Inspector Morris",
      data = "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=",
      flags = FeatureFlags(),
    )

    assertThat(id).isEqualTo(456)

    val captor = argumentCaptor<RecommendationSupportingDocumentEntity>()

    Mockito.verify(repository).save(captor.capture())

    val entity = captor.firstValue

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.mimetype).isEqualTo("word")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris")
    assertThat(String(entity.data)).isEqualTo("The hills are alive with thet sound of music")
  }

  @Test
  fun retrieve() {
    val repository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)

    val created = DateTimeHelper.utcNowDateTimeString()

    given(repository.findByRecommendationId(123)).willReturn(
      listOf(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
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
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris")
  }

  @Test
  fun replace() {
    val repository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)

    val created = DateTimeHelper.utcNowDateTimeString()

    given(repository.findById(123)).willReturn(
      Optional.of(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
          uploaded = created,
          data = "".encodeToByteArray(),
        ),
      ),
    )

    SupportingDocumentService(repository).replaceSupportingDocument(
      123,
      mimetype = "word2",
      uploadedBy = "daman2",
      uploadedByUserFullName = "Inspector Morris2",
      uploaded = created,
      data = "VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=",
      filename = "word.docx",
      flags = FeatureFlags(),
    )

    val captor = argumentCaptor<RecommendationSupportingDocumentEntity>()

    Mockito.verify(repository).save(captor.capture())

    val entity = captor.firstValue

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.mimetype).isEqualTo("word2")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.created).isEqualTo(created)
    assertThat(entity.createdBy).isEqualTo("daman")
    assertThat(entity.createdByUserFullName).isEqualTo("Inspector Morris")
    assertThat(entity.uploaded).isEqualTo(created)
    assertThat(entity.uploadedBy).isEqualTo("daman2")
    assertThat(entity.uploadedByUserFullName).isEqualTo("Inspector Morris2")
    assertThat(String(entity.data)).isEqualTo("The hills are alive with thet sound of music")
  }

  @Test
  fun `get`() {
    val repository = Mockito.mock(RecommendationSupportingDocumentRepository::class.java)

    val created = DateTimeHelper.utcNowDateTimeString()

    given(repository.findById(123)).willReturn(
      Optional.of(
        RecommendationSupportingDocumentEntity(
          id = 1,
          recommendationId = 123,
          createdBy = "daman",
          createdByUserFullName = "Inspector Morris",
          created = created,
          filename = "word.docx",
          type = "PPUDPartA",
          mimetype = "word",
          uploadedBy = "daman",
          uploadedByUserFullName = "Inspector Morris",
          uploaded = created,
          data = "The hills are alive with thet sound of music".encodeToByteArray(),
        ),
      ),
    )

    val entity = SupportingDocumentService(repository).getSupportingDocument(123, FeatureFlags())

    assertThat(entity.recommendationId).isEqualTo(123)
    assertThat(entity.type).isEqualTo("PPUDPartA")
    assertThat(entity.filename).isEqualTo("word.docx")
    assertThat(entity.data).isEqualTo("VGhlIGhpbGxzIGFyZSBhbGl2ZSB3aXRoIHRoZXQgc291bmQgb2YgbXVzaWM=")
  }
}
