package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.apache.poi.hwpf.HWPFDocument
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.FileOutputStream

@Service
internal class PoiDocumentPlaceholderReplacementService2 {

  fun generateDocFromTemplate() {
    val resource = ClassPathResource("NAT Recall Part A London - obtained 131021 edited.doc")

    val doc = HWPFDocument(resource.inputStream)
    val range = doc.range

    // Create a map of values for the template
    val map = hashMapOf(
      "forename" to "Jack"
      // "surname" to "Maloney"
    )

    for (entry in map.entries.iterator()) {
      range.replaceText("\$forename", "Jack")
    }

    val os =
      FileOutputStream("/Users/jack.maloney/Development/make-recall-decision-api/src/main/resources/test.doc")
    doc.write(os)
  }
}
