package uk.gov.justice.digital.hmpps.makerecalldecisionapi.client

import com.fasterxml.jackson.annotation.JsonFormat
import io.micrometer.core.instrument.Counter
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.documentmapper.RecommendationDataToDocumentMapper.Companion.joinToString
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.Address
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.makerecalldecisions.recommendation.PersonOnProbation
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.ndelius.LicenceCondition
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.ClientTimeoutException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RegionNotFoundException
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.getValueAndHandleWrappedException
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.concurrent.TimeoutException

class DeliusClient(
  private val webClient: WebClient,
  @Value("\${ndelius.client.timeout}") private val nDeliusTimeout: Long,
  private val timeoutCounter: Counter,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPersonalDetails(crn: String): PersonalDetails = getBody("/case-summary/$crn/personal-details")

  fun getUserInfo(name: String): UserInfo = getBody("/user/$name")

  fun getProvider(code: String): Provider = getBody("/provider/$code") { RegionNotFoundException(it) }

  fun getOverview(crn: String): Overview = getBody("/case-summary/$crn/overview")

  fun getLicenceConditions(crn: String): LicenceConditions = getBody("/case-summary/$crn/licence-conditions")

  fun getMappaAndRoshHistory(crn: String): MappaAndRoshHistory = getBody("/case-summary/$crn/mappa-and-rosh-history")

  fun getRecommendationModel(crn: String): RecommendationModel = getBody("/case-summary/$crn/recommendation-model")

  fun getContactHistory(
    crn: String,
    query: String? = null,
    from: LocalDate? = null,
    to: LocalDate? = null,
    typeCodes: List<String> = emptyList(),
    includeSystemGenerated: Boolean = true,
  ): ContactHistory = getBody(
    endpoint = "/case-summary/$crn/contact-history",
    parameters = mapOf(
      "query" to listOfNotNull(query),
      "from" to listOfNotNull(from),
      "to" to listOfNotNull(to),
      "type" to typeCodes,
      "includeSystemGenerated" to listOfNotNull(includeSystemGenerated),
    ),
  )

  fun getUserAccess(username: String, crn: String): UserAccess = getBody("/user/$username/access/$crn")

  fun getDocument(crn: String, id: String): ResponseEntity<Resource> =
    get("/document/$crn/$id") { PersonNotFoundException(it) }

  private inline fun <reified T : Any> getBody(
    endpoint: String,
    parameters: Map<String, List<Any>> = emptyMap(),
    crossinline notFoundExceptionFunction: (String) -> Throwable = { PersonNotFoundException(it) },
  ) =
    get<T>(endpoint, parameters, notFoundExceptionFunction).body!!

  private inline fun <reified T : Any> get(
    endpoint: String,
    parameters: Map<String, List<Any>> = emptyMap(),
    crossinline notFoundExceptionFunction: (String) -> Throwable,
  ): ResponseEntity<T> {
    log.info(normalizeSpace("About to call $endpoint"))
    val result = webClient.get()
      .uri {
        it.path(endpoint).also { uri -> parameters.forEach { param -> uri.queryParam(param.key, param.value) } }.build()
      }
      .retrieve()
      .onStatus(
        { it == HttpStatus.NOT_FOUND },
        { throw notFoundExceptionFunction("No details available for endpoint: $endpoint") },
      )
      .toEntity(T::class.java)
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
          "No response within $nDeliusTimeout seconds",
        )
      }
    }
  }

  data class Name(
    val forename: String,
    val middleName: String?,
    val surname: String,
  )

  data class Offence(
    val date: LocalDate?,
    val code: String,
    val description: String,
  )

  data class Sentence(
    val description: String,
    val length: Int?,
    val lengthUnits: String?,
    val isCustodial: Boolean,
    val custodialStatusCode: String?,
    val licenceExpiryDate: LocalDate?,
    val sentenceExpiryDate: LocalDate?,
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
      val bookingNumber: String?,
    )
  }

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

  data class UserInfo(
    val email: String? = null,
  )

  data class Provider(
    val code: String = "",
    val name: String = "",
  )

  data class PersonalDetails(
    val personalDetails: PersonalDetailsOverview,
    val mainAddress: Address?,
    val communityManager: Manager?,
  ) {
    data class Manager(
      val staffCode: String,
      val name: Name,
      val provider: Provider,
      val team: Team,
    ) {
      data class Provider(
        val code: String,
        val name: String,
      )

      data class Team(
        val code: String,
        val name: String,
        val localAdminUnit: String,
        val telephone: String?,
        val email: String?,
      )
    }
  }

  data class Release(
    val releaseDate: LocalDate,
    val recallDate: LocalDate?,
  )

  data class Conviction(
    val number: String? = null,
    val sentence: Sentence?,
    val mainOffence: Offence,
    val additionalOffences: List<Offence>,
  )

  data class Overview(
    val personalDetails: PersonalDetailsOverview,
    val registerFlags: List<String>,
    val lastRelease: Release?,
    val activeConvictions: List<Conviction>,
  )

  data class LicenceConditions(
    val personalDetails: PersonalDetailsOverview,
    val activeConvictions: List<ConvictionWithLicenceConditions>,
  ) {
    data class ConvictionWithLicenceConditions(
      val number: String? = null,
      val sentence: Sentence?,
      val mainOffence: Offence,
      val additionalOffences: List<Offence>,
      val licenceConditions: List<LicenceCondition>,
    )
  }

  data class Mappa(
    val category: Int?,
    val level: Int?,
    val startDate: LocalDate,
  )

  data class MappaAndRoshHistory(
    val personalDetails: PersonalDetailsOverview,
    val mappa: Mappa?,
    val roshHistory: List<Rosh>,
  ) {
    data class Rosh(
      val active: Boolean,
      val type: String,
      val typeDescription: String,
      val notes: String?,
      val startDate: LocalDate,
    )
  }

  data class RecommendationModel(
    val personalDetails: PersonalDetailsOverview,
    val mainAddress: Address?,
    val lastRelease: Release?,
    val lastReleasedFromInstitution: Institution?,
    val mappa: Mappa?,
    val activeConvictions: List<Conviction>,
    val activeCustodialConvictions: List<ConvictionDetails>,
  ) {
    data class Institution(
      val name: String?,
    )

    data class ConvictionDetails(
      val number: String? = null,
      val sentence: ExtendedSentence,
      val mainOffence: Offence,
      val additionalOffences: List<Offence>,
    )

    data class ExtendedSentence(
      val description: String,
      val length: Int?,
      val lengthUnits: String?,
      val secondLength: Int?,
      val secondLengthUnits: String?,
      val isCustodial: Boolean,
      val custodialStatusCode: String?,
      val startDate: LocalDate?,
      val licenceExpiryDate: LocalDate?,
      val sentenceExpiryDate: LocalDate?,
    )
  }

  data class ContactHistory(
    val personalDetails: PersonalDetailsOverview,
    val contacts: List<Contact>,
    val summary: ContactSummary,
  ) {
    data class Contact(
      val description: String?,
      val documents: List<DocumentReference>,
      val enforcementAction: String?,
      val notes: String?,
      val outcome: String?,
      val sensitive: Boolean?,
      val startDateTime: ZonedDateTime,
      val type: Type,
    ) {
      data class Type(val code: String, val description: String, val systemGenerated: Boolean)
      data class DocumentReference(val id: String, val name: String, val lastUpdated: ZonedDateTime)
    }

    data class ContactSummary(
      val types: List<ContactTypeSummary>,
      val hits: Int,
      val total: Int = types.sumOf { it.total },
    )
  }

  data class ContactTypeSummary(val code: String, val description: String, val total: Int)

  data class UserAccess(
    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,
    val userNotFound: Boolean = false,
    val userExcluded: Boolean,
    val userRestricted: Boolean,
  )
}

fun DeliusClient.RecommendationModel.toPersonOnProbation() = PersonOnProbation(
  croNumber = personalDetails.identifiers.croNumber,
  mostRecentPrisonerNumber = personalDetails.identifiers.bookingNumber,
  nomsNumber = personalDetails.identifiers.nomsNumber,
  pncNumber = personalDetails.identifiers.pncNumber,
  name = joinToString(personalDetails.name.forename, personalDetails.name.surname),
  firstName = personalDetails.name.forename,
  middleNames = personalDetails.name.middleName,
  surname = personalDetails.name.surname,
  gender = personalDetails.gender,
  ethnicity = personalDetails.ethnicity ?: "",
  primaryLanguage = personalDetails.primaryLanguage,
  dateOfBirth = personalDetails.dateOfBirth,
  addresses = mainAddress.toAddresses(),
)

fun DeliusClient.Address?.toAddresses() = listOfNotNull(
  this?.let {
    Address(
      line1 = joinToString(buildingName, addressNumber, streetName),
      line2 = district ?: "",
      town = town ?: "",
      postcode = postcode ?: "",
      noFixedAbode = isNoFixedAbode(it),
    )
  },
)

private fun isNoFixedAbode(it: DeliusClient.Address): Boolean {
  val postcodeUppercaseNoWhiteSpace = it.postcode?.filter { !it.isWhitespace() }?.uppercase()
  return postcodeUppercaseNoWhiteSpace == "NF11NF" || it.noFixedAbode == true
}
