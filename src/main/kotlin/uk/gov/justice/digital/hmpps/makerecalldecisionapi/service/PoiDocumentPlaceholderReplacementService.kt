package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

@Service
internal class PoiDocumentPlaceholderReplacementService {

  fun generateDocFromTemplate() {
    val resource = ClassPathResource("NAT Recall Part A London - obtained 131021 edited.doc")

    try {
      var fs = POIFSFileSystem(resource.inputStream)
      var doc = HWPFDocument(fs)
      doc = replaceText(doc, "\$forename", "Jack Maloney")
      saveWord(doc)
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  fun replaceText(doc: HWPFDocument, findText: String, replaceText: String): HWPFDocument {

    val r1 = doc.range

    for (item in 0..r1.numSections() - 1) {
      val section = r1.getSection(item)
      for (x in 0..section.numParagraphs() - 1) {
        val paragraph = section.getParagraph(x)
        for (z in 0..paragraph.numCharacterRuns() - 1) {
          val run = paragraph.getCharacterRun(z)
          val text = run.text()
          if (text.contains(findText)) {
            run.replaceText(findText, replaceText)
          }
        }
      }
    }
    return doc
  }

  fun saveWord(doc: HWPFDocument) {
    var out: FileOutputStream? = null
    try {
      out = FileOutputStream("/Users/jack.maloney/Development/make-recall-decision-api/src/main/resources/NAT Recall Part A London - obtained 131021 edited.doc")
      doc.write(out)
    } finally {
      out?.close()
    }
  }
}
