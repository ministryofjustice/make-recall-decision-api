package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.CommunityApiClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.CaseSummaryOverviewResponse
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Offence
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.PersonDetails
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Risk
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.NoActiveConvictionsException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import java.time.LocalDate

@Service
class CaseSummaryOverviewService(
  @Qualifier("communityApiClientUserEnhanced") private val communityApiClient: CommunityApiClient
) { // TODO remove suspend / or use await again
  suspend fun getOverview(crn: String): CaseSummaryOverviewResponse {
    val offenderDetails = getValue(communityApiClient.getAllOffenderDetails(crn))!!
    val activeConvictions = getValue(communityApiClient.getActiveConvictions(crn)) ?: emptyList()
    val age = offenderDetails.dateOfBirth?.until(LocalDate.now())?.years
    val firstName = offenderDetails.firstName ?: ""
    val surname = offenderDetails.surname ?: ""
    val name = if (firstName.isEmpty()) {
      surname
    } else "$firstName $surname"
    val registrations = communityApiClient.getRegistrations(crn).awaitFirstOrNull()?.registrations
    val activeRegistrations = registrations?.filter { it.active ?: false }
    val riskFlags = activeRegistrations?.map { it.type?.description ?: "" } ?: emptyList()

    val offences: List<Offence> = activeConvictions
      .map { it.offences }
      .flatMap { it!!.toList() }
      .map {
        Offence(
          mainOffence = it.mainOffence, description = it.detail?.description ?: "", code = it.detail?.code ?: ""
        )
      }

    return CaseSummaryOverviewResponse(
      personalDetailsOverview = PersonDetails(
        name = name,
        dateOfBirth = offenderDetails.dateOfBirth,
        age = age,
        gender = offenderDetails.gender ?: "",
        crn = crn
      ),
      offences = offences.filter { it.mainOffence == true },
      risk = Risk(flags = riskFlags)
    )
  }

  private fun <T : Any> getValue(mono: Mono<T>?): T? {
    return try {
      val value = mono?.block()
      value ?: value
    } catch (wrappedException: RuntimeException) {
      when (wrappedException.cause) {
        is ClientTimeoutException -> throw wrappedException.cause as ClientTimeoutException
        is PersonNotFoundException -> throw wrappedException.cause as PersonNotFoundException
        is NoActiveConvictionsException -> throw wrappedException.cause as NoActiveConvictionsException
        else -> throw wrappedException
      }
    }
  }
}
