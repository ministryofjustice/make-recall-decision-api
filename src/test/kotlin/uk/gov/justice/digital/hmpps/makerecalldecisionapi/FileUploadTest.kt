package uk.gov.justice.digital.hmpps.makerecalldecisionapi

import org.apache.http.HttpEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.util.*


class FileUploadTest {
  @Test
  fun test() {

    val file = File("test.pdf")

    val httpClient: CloseableHttpClient = HttpClients.createDefault()
    val uploadFile = HttpPost("http://localhost:8080/test/269230831/file-upload?redirect=http://localhost:3000")
    val builder = MultipartEntityBuilder.create()
    builder.addTextBody("field1", "yes", ContentType.TEXT_PLAIN)
    builder.addBinaryBody("files", FileInputStream(file), ContentType.APPLICATION_OCTET_STREAM, file.getName())

    val multipart: HttpEntity = builder.build()
    uploadFile.setEntity(multipart)
    val response: CloseableHttpResponse = httpClient.execute(uploadFile)
    val responseEntity: HttpEntity = response.getEntity()


  }
}