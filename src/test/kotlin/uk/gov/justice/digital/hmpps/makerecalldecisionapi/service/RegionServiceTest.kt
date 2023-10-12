package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.client.DeliusClient
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.exception.RegionNotFoundException

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class RegionServiceTest : ServiceTestBase() {

  private lateinit var regionService: RegionService

  @BeforeEach
  fun setup() {
    regionService = RegionService(deliusClient)
  }

  @Test
  fun `given valid region code when getRegionName is invoked then matching name is returned`() {
    runTest {
      val regionCode = "A11"
      val regionName = "Region Name"
      given(deliusClient.getProvider(regionCode))
        .willReturn(DeliusClient.Provider(regionCode, regionName))

      val response = regionService.getRegionName(regionCode)

      assertThat(response).isEqualTo(regionName)
    }
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = ["  ", "\t", "\n"])
  fun `given null or blank region code when getRegionName is invoked then empty string is returned`(regionCode: String?) {
    runTest {
      val response = regionService.getRegionName(regionCode)

      assertThat(response).isEqualTo("")
    }
  }

  @Test
  fun `given unrecognised region code when getRegionName is invoked then region code is returned`() {
    runTest {
      val regionCode = "Unrecognised"
      given(deliusClient.getProvider(regionCode))
        .willThrow(RegionNotFoundException("Intentional Test exception"))

      val response = regionService.getRegionName(regionCode)

      assertThat(response).isEqualTo(regionCode)
    }
  }
}
