package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.sar.SubjectAccessRequestResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.jpa.repository.RecommendationRepository
import uk.gov.justice.hmpps.kotlin.sar.HmppsProbationSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Transactional
@Service
class SubjectAccessRequestService(
  private val recommendationRepository: RecommendationRepository,
) : HmppsProbationSubjectAccessRequestService {
  override fun getProbationContentFor(
    crn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? {
    val recommendations = recommendationRepository.findByCrnAndCreatedDate(crn, fromDate, toDate)

    if (recommendations.isEmpty()) {
      return null
    } else {
      return HmppsSubjectAccessRequestContent(
        content = SubjectAccessRequestResponse(
          crn = crn,
          recommendations = recommendations.map { rec -> rec.data },
        ),
      )
    }
  }
}
