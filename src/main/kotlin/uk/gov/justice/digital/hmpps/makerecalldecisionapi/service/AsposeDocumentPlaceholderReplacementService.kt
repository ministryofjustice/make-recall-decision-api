package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.spire.doc.*
import com.spire.doc.documents.Paragraph
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class AsposeDocumentPlaceholderReplacementService {

  fun generateDocFromTemplate() {
    val resource = ClassPathResource("NAT Recall Part A London - obtained 131021 edited.doc")

    val document = Document(resource.inputStream)
    // Get the first section
    // val section = document.getSections().get(0)
    // //Get the first table in the section
    // val table = section.getTables().get(0)

    // Create a map of values for the template
    val map = hashMapOf(
      "forename" to "Jack",
      "surname" to "Maloney"
    )

    replaceTextinDocumentBody(map, document)
    // Save the result document
    document.saveToFile("CreateByReplacingPlaceholder.doc", FileFormat.Doc)
  }

  // Replace text in document body
  fun replaceTextinDocumentBody(map: Map<String, String?>, document: Document) {
    for (section in document.sections as Iterable<Section>) {
      for (para in section.paragraphs as Iterable<Paragraph?>) {
        for (entry in map.entries.iterator()) {
          para?.replace("\${$entry.key}", entry.value, false, true)
        }
      }
    }
  }
}
