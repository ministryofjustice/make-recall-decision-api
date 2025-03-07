package uk.gov.justice.digital.hmpps.makerecalldecisionapi.controller.establishmentmapping

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.service.establishmentmapping.EstablishmentMappingService

@ExtendWith(MockitoExtension::class)
class EstablishmentMappingControllerTest {

  @InjectMocks
  private lateinit var controller: EstablishmentMappingController

  @Mock
  private lateinit var establishmentMappingService: EstablishmentMappingService

  @Test
  fun `gets all establishment mappings and includes them in the response`() {
    runTest {
      // given
      val expectedEstablishmentMappings = mapOf("1" to "one", "2" to "two", "3" to "three")
      given(establishmentMappingService.getEstablishmentMappings())
        .willReturn(expectedEstablishmentMappings)

      // when
      val response = controller.getEstablishmentMappings()

      // then
      assertThat(response.body?.establishmentMappings).isEqualTo(expectedEstablishmentMappings)
    }
  }
}
