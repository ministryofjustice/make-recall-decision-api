package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest
import com.amazonaws.services.s3.model.UploadPartRequest
import com.amazonaws.services.s3.model.UploadPartResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller.AuthenticationFacade
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.RecommendationFileResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.FileNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidTokenException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationFileEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.toRecommendationFileResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationFileRepository
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.DateTimeHelper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

@Transactional
@Service
internal class RecommendationFileService(
  private val amazonS3: AmazonS3,
  private val recommendationFileRepository: RecommendationFileRepository,
  private val authenticationFacade: AuthenticationFacade,
  private val generator: NumberGenerator,
  private @Value("\${s3.bucketName}") val bucketName: String,
  private @Value("\${s3.multipartBufferSizeInMb}") val multipartBufferSizeInMb: Int
) {

  fun fetchRecommendationFiles(recommendationId: Long): List<RecommendationFileResponse> {

    return recommendationFileRepository.findByRecommendationId(recommendationId)
      .map { it.toRecommendationFileResponse() }
  }

  suspend fun create(recommendationId: Long, category: String): RecommendationFileResponse {

    val createdBy = authenticationFacade.currentUserId!!
    val createdByUserFullName = authenticationFacade.currentNameOfUser!!

    return withContext(Dispatchers.IO) {
      recommendationFileRepository.save(
        RecommendationFileEntity(
          recommendationId = recommendationId,
          token = generator.generateToken(),
          createdBy = createdBy,
          createdByUserFullName = createdByUserFullName,
          created = DateTimeHelper.utcNowDateTimeString(),
          category = category,
        )
      )
    }.toRecommendationFileResponse()
  }

  fun uploadFile(token: String, multipartFile: MultipartFile) {

    val file = recommendationFileRepository.findByToken(token)

    if (file?.token != token) {
      throw InvalidTokenException();
    }

    file.s3Id = UUID.randomUUID()
    file.name = multipartFile.originalFilename
    file.size = multipartFile.size
    file.type = multipartFile.contentType

    multiPartUpload(file.s3Id!!, multipartFile.inputStream)

    recommendationFileRepository.save(file)
  }

  private fun multiPartUpload(itemId: UUID, inputStream: InputStream) {
    try {
      val bytes = ByteArray(multipartBufferSizeInMb * 1024 * 1024)
      val key = itemId.toString()
      val uploadRequest = InitiateMultipartUploadRequest(bucketName, key)
      val uploadId: String = amazonS3.initiateMultipartUpload(uploadRequest).getUploadId()
      var partNumber = 1
      val results = ArrayList<UploadPartResult>()
      var bytesRead = inputStream.read(bytes)
      while (bytesRead >= 0) {
        val part = UploadPartRequest()
          .withBucketName(bucketName)
          .withKey(key)
          .withUploadId(uploadId)
          .withPartNumber(partNumber)
          .withInputStream(ByteArrayInputStream(bytes, 0, bytesRead))
          .withPartSize(bytesRead.toLong())
        results.add(amazonS3.uploadPart(part))
        bytesRead = inputStream.read(bytes)
        partNumber++
      }

      val completeRequest = CompleteMultipartUploadRequest()
        .withBucketName(bucketName)
        .withKey(key)
        .withUploadId(uploadId)
        .withPartETags(results)

      amazonS3.completeMultipartUpload(completeRequest)
    } catch (e: IOException) {
      throw RuntimeException("Error uploading file.", e)
    }
  }

  fun retrieve(recommendationId: String, id: Long): RecommendationFileEntity {
    return recommendationFileRepository.findById(id).orElseThrow { FileNotFoundException() }
  }

  fun downloadFile(s3Id: UUID): InputStream {
    return amazonS3.getObject(bucketName, s3Id.toString()).objectContent
  }
}