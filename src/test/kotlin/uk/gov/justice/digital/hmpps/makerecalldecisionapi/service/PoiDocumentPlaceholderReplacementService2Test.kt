package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PoiDocumentPlaceholderReplacementService2Test : ServiceTestBase() {

  private lateinit var documentPlaceholderReplacementService: PoiDocumentPlaceholderReplacementService2

  @BeforeEach
  fun setup() {
    documentPlaceholderReplacementService = PoiDocumentPlaceholderReplacementService2()
  }

  @Test
  fun `placeholder test`() {
    runTest {
      documentPlaceholderReplacementService.generateDocFromTemplate()
    }
  }
}
