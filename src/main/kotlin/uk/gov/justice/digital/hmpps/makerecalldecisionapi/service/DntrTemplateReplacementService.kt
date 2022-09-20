package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.deepoove.poi.XWPFTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.DntrData
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.MrdTextConstants.Constants.EMPTY_STRING
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.RecommendationToDocumentMapper

@Service
internal class DntrTemplateReplacementService {
  val resource = ClassPathResource("DNTR Template.docx")

  fun generateDocFromTemplate(recommendation: RecommendationEntity): String {
    val dntrData = RecommendationToDocumentMapper.mapRecommendationDataToDNTRDocumentData(recommendation)

    val file = XWPFTemplate.compile(resource.inputStream).render(
      mappingsForTemplate(dntrData)
    )
//    ).writeToFile("dntr_template.docx")

    return writeDataToFile(file)
  }

  fun mappingsForTemplate(dntrData: DntrData): HashMap<String, String> {
    val mappings = hashMapOf(
      "why_considered_recall" to (dntrData.whyConsideredRecall ?: EMPTY_STRING)
    )
    return mappings
  }
}
