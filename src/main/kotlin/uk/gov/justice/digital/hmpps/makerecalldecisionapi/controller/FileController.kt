package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller

import com.amazonaws.util.IOUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.FileNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.InvalidTokenException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.RecommendationFileService
import java.io.InputStream
import java.net.URI
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
internal class FileController(private val fileService: RecommendationFileService) {

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/recommendations/{recommendationId}/file-upload")
  fun handleFileUpload(
    @PathVariable recommendationId: String,
    @RequestParam("redirectSuccess") redirectSuccess: String,
    @RequestParam("redirectFailure") redirectFailure: String,
    @RequestParam("files") files: List<MultipartFile>?,
    @RequestParam("token") token: String,
    request: HttpServletRequest
  ): ResponseEntity<Void> {
    if (files == null) {
      return ResponseEntity.badRequest().build()
    }

    val file = files.get(0)
    if (file.size > 500 * 1024){
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(URI.create(redirectFailure + "?error=BAD_SIZE")).build();
    }

    try {
      fileService.uploadFile(token = token, multipartFile = files.get(0))
    } catch (e: InvalidTokenException) {
      return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(URI.create(redirectFailure + "?error=BAD_TOKEN")).build();
    }
    return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(URI.create(redirectSuccess)).build();
  }

  @GetMapping("/recommendations/{recommendationId}/file/{id}")
  fun handleFileDownload(@PathVariable recommendationId: String,
                         @PathVariable id: Long, response: HttpServletResponse,
                         @RequestParam("token") token: String) {

    try {
      val file = fileService.retrieve(recommendationId, id)

      if (file.token != token) {
        throw InvalidTokenException();
      }

      if (file.s3Id == null) {
        throw FileNotFoundException()
      }
      if (file.type == "application/pdf"){
        response.addHeader("Content-Disposition", "inline; filename=\"${file.category}.pdf\"")
      } else if ((file.type == "application/msword")) {
        response.addHeader("Content-Disposition", "attachment; filename=\"${file.category}.doc\"")
      } else {
        response.addHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
      }

      response.contentType = file.type
      val inputStream: InputStream = fileService.downloadFile(file.s3Id!!)
      IOUtils.copy(inputStream, response.outputStream)
      response.flushBuffer()

    } catch (e: FileNotFoundException) {
      response.sendError(HttpStatus.NOT_FOUND.value(), "file not found")
    } catch (e: InvalidTokenException) {
      response.sendError(HttpStatus.UNAUTHORIZED.value(), "invalid token")
    }
  }
}