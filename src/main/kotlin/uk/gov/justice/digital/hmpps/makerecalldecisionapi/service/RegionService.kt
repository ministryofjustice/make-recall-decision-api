package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.PersonNotFoundException

@Service
internal class RegionService(
  private val deliusClient: DeliusClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getRegionName(regionCode: String?): String {
    if (regionCode.isNullOrBlank()) {
      return ""
    }

    return try {
      // Regions are called Providers in Delius
      deliusClient.getProvider(regionCode).name
    } catch (ex: PersonNotFoundException) {
      log.warn("Unrecognised region code '$regionCode'")
      regionCode
    }
  }
}
