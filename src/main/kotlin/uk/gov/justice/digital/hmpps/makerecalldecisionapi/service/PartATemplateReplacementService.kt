package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import com.deepoove.poi.XWPFTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.entity.RecommendationEntity
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.util.RecommendationToPartADataMapper
import java.io.ByteArrayOutputStream
import java.util.Base64

@Service
internal class PartATemplateReplacementService {

  fun generateDocFromTemplate(recommendation: RecommendationEntity): String {
    val resource = ClassPathResource("NAT Recall Part A London Template - obtained 131021.docx")

    val partAData = RecommendationToPartADataMapper.mapRecommendationDataToPartAData(recommendation)

    val file = XWPFTemplate.compile(resource.inputStream).render(
      hashMapOf(
        "custody_status" to partAData.custodyStatus,
      )
    )

    val out = ByteArrayOutputStream()
    file.write(out)
    file.close()

    return Base64.getEncoder().encodeToString(out.toByteArray())
  }
}
