package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import com.fasterxml.jackson.annotation.JsonFormat
import io.micrometer.core.instrument.Counter
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.getValueAndHandleWrappedException
import java.time.Duration
import java.time.LocalDate
import java.util.concurrent.TimeoutException

class DeliusClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  private val timeoutCounter: Counter
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonalDetails(crn: String): PersonalDetails = call("/case-summary/$crn/personal-details")
  fun getOverview(crn: String): Overview = call("/case-summary/$crn/overview")
  fun getLicenceConditions(crn: String): LicenceConditions = call("/case-summary/$crn/licence-conditions")
  fun getMappaAndRoshHistory(crn: String): MappaAndRoshHistory = call("/case-summary/$crn/mappa-and-rosh-history")

  private inline fun <reified T : Any> call(endpoint: String): T {
    log.info(normalizeSpace("About to call $endpoint"))
    val result = webClient
      .get()
      .uri(endpoint)
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw PersonNotFoundException("No details available for endpoint: $endpoint") }
      )
      .bodyToMono(T::class.java)
      .timeout(Duration.ofSeconds(nDeliusTimeout))
      .doOnError { handleTimeoutException(it, endpoint) }
    log.info(normalizeSpace("Returning $endpoint details"))
    return getValueAndHandleWrappedException(result)!!
  }

  private fun handleTimeoutException(exception: Throwable?, endPoint: String) {
    when (exception) {
      is TimeoutException -> {
        timeoutCounter.increment()
        throw ClientTimeoutException(
          "Delius integration client - $endPoint endpoint",
          "No response within $nDeliusTimeout seconds"
        )
      }
    }
  }

  data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String
  )
  data class Offence(
    val date: LocalDate?,
    val code: String,
    val description: String
  )
  data class Sentence(
    val description: String,
    val length: Int?,
    val lengthUnits: String?,
    val isCustodial: Boolean,
    val custodialStatusCode: String?,
    val licenceExpiryDate: LocalDate?,
    val sentenceExpiryDate: LocalDate?
  )

  data class PersonalDetailsOverview(
    val name: Name,
    val identifiers: Identifiers,
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    val dateOfBirth: LocalDate,
    val gender: String,
    val ethnicity: String?,
    val primaryLanguage: String?,
  ) {
    data class Identifiers(
      val pncNumber: String?,
      val croNumber: String?,
      val nomsNumber: String?,
      val bookingNumber: String?
    )
  }

  data class PersonalDetails(
    val personalDetails: PersonalDetailsOverview,
    val mainAddress: Address?,
    val communityManager: Manager?,
  ) {
    data class Address(
      val buildingName: String? = null,
      val addressNumber: String? = null,
      val streetName: String? = null,
      val district: String? = null,
      val town: String? = null,
      val county: String? = null,
      val postcode: String? = null,
      val noFixedAbode: Boolean? = null,
    )
    data class Manager(
      val staffCode: String,
      val name: Name,
      val provider: Provider,
      val team: Team
    ) {
      data class Provider(
        val code: String,
        val name: String
      )
      data class Team(
        val code: String,
        val name: String,
        val localAdminUnit: String,
        val telephone: String?,
        val email: String?
      )
    }
  }

  data class Overview(
    val personalDetails: PersonalDetailsOverview,
    val registerFlags: List<String>,
    val lastRelease: Release?,
    val activeConvictions: List<Conviction>
  ) {
    data class Release(
      val releaseDate: LocalDate,
      val recallDate: LocalDate?
    )
    data class Conviction(
      val number: String? = null,
      val sentence: Sentence?,
      val mainOffence: Offence,
      val additionalOffences: List<Offence>
    )
  }

  data class LicenceConditions(
    val personalDetails: PersonalDetailsOverview,
    val activeConvictions: List<ConvictionWithLicenceConditions>
  ) {
    data class ConvictionWithLicenceConditions(
      val number: String? = null,
      val sentence: Sentence?,
      val mainOffence: Offence,
      val additionalOffences: List<Offence>,
      val licenceConditions: List<LicenceCondition>
    )
    data class LicenceCondition(
      val mainCategory: LicenceConditionCategory,
      val subCategory: LicenceConditionCategory?,
      val notes: String?
    )
    data class LicenceConditionCategory(
      val code: String,
      val description: String
    )
  }

  data class MappaAndRoshHistory(
    val personalDetails: PersonalDetailsOverview,
    val mappa: Mappa?,
    val roshHistory: List<Rosh>
  ) {
    data class Mappa(
      val category: Int?,
      val level: Int?,
      val startDate: LocalDate
    )
    data class Rosh(
      val active: Boolean,
      val type: String,
      val typeDescription: String,
      val notes: String?,
      val startDate: LocalDate
    )
  }
}
