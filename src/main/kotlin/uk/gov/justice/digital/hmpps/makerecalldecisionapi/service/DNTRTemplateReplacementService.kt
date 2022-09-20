package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.deepoove.poi.XWPFTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DNTRData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.RecommendationToDocumentMapper

@Service
internal class DNTRTemplateReplacementService {
  val resource = ClassPathResource("DNTR Template.docx")

  fun generateDocFromTemplate(recommendation: RecommendationEntity): String {
    val dntrData = RecommendationToDocumentMapper.mapRecommendationDataToDNTRDocumentData(recommendation)

    val file = XWPFTemplate.compile(resource.inputStream).render(
      mappingsForTemplate(dntrData)
    )
//    ).writeToFile("out_template.docx")

    return writeDataToFile(file)
  }

  fun mappingsForTemplate(dntrData: DNTRData): HashMap<String, String?> {
    val mappings = hashMapOf(
      "some_field" to dntrData.someField
    )
    return mappings
  }
}
