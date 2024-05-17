package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import io.micrometer.core.instrument.Counter
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.WebClientConfiguration.Companion.withRetry
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeoutException

class DocumentManagementClient(
  private val webClient: WebClient,
  @Value("\${document-management.client.timeout}") private val documentManagementClientTimeout: Long,
  private val timeoutCounter: Counter,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun downloadFileAsByteArray(documentUuid: String?, filename: String? = null): Mono<ByteArray> {
    log.info(StringUtils.normalizeSpace("Fetching document with UUID $documentUuid"))
    val contentDisposition = "inline; filename=$filename"
    return webClient.get()
      .uri("/documents/$documentUuid/file")
      .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
      .header("Service-Name", "Consider a recall")
      .header(HttpHeaders.ACCEPT, "application/*")
      .retrieve()
      .bodyToMono(ByteArray::class.java)
      .timeout(Duration.ofSeconds(documentManagementClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "/documents/$documentUuid/file",
        )
      }
      .withRetry()
  }

  fun deleteFile(documentUuid: String?): Mono<Void> {
    log.info(StringUtils.normalizeSpace("Deleting document with UUID $documentUuid"))
    return webClient.delete()
      .uri("/documents/$documentUuid")
      .header("Service-Name", "Consider a recall")
      .retrieve()
      .toBodilessEntity()
      .then()
      .timeout(Duration.ofSeconds(documentManagementClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "/documents/$documentUuid",
        )
      }
      .withRetry()
  }

  fun uploadFile(crn: String?, file: ByteArray, filename: String? = null, documentUuid: String, mimeType: String?): Mono<UUID> {
    log.info(StringUtils.normalizeSpace("About to upload file for crn $crn"))
    val responseType = object : ParameterizedTypeReference<DocumentUploadResponse>() {}
    val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
    val contentDisposition = ContentDisposition
      .builder("form-data")
      .name("file")
      .filename(filename)
      .build()
    fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
    val contentType = mimeType ?: "application/octet-stream"
    val fileEntity: HttpEntity<ByteArray> = HttpEntity(file, fileMap)

    val result = webClient
      .post()
      .uri("/documents/PPUD_RECALL/$documentUuid")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .header("Service-Name", "Consider a recall")
      .body(
        BodyInserters.fromValue(
          MultipartBodyBuilder()
            .apply {
              part("file", fileEntity, MediaType.parseMediaType(contentType))
              part("metadata", """{ "crn": "$crn" }""")
            }
            .build(),
        ),
      )
      .retrieve()
      .bodyToMono(responseType)
      .map { it.documentUuid }
      .timeout(Duration.ofSeconds(documentManagementClientTimeout))
      .doOnError { ex ->
        handleTimeoutException(
          exception = ex,
          endPoint = "/documents/PPUD_RECALL/$documentUuid",
        )
      }
      .withRetry()
    log.info(StringUtils.normalizeSpace("Returning UUID of document uploaded for $crn"))
    return result
  }

  private fun handleTimeoutException(exception: Throwable?, endPoint: String) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()
        throw ClientTimeoutException(
          "Document Management client - $endPoint endpoint",
          "No response within $documentManagementClientTimeout seconds",
        )
      }
    }
  }
}

data class DocumentUploadResponse(
  val documentUuid: UUID,
)
