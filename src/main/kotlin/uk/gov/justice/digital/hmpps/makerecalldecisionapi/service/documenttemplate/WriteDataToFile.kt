package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.documenttemplate

import com.deepoove.poi.XWPFTemplate
import java.io.ByteArrayOutputStream
import java.util.Base64

fun writeDataToFile(file: XWPFTemplate): String {
  val out = ByteArrayOutputStream()
  file.write(out)
  file.close()
  return Base64.getEncoder().encodeToString(out.toByteArray())
}
