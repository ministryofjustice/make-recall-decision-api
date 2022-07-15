package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class PoiDocumentPlaceholderReplacementServiceTest : ServiceTestBase() {

  private lateinit var documentPlaceholderReplacementService: PoiDocumentPlaceholderReplacementService

  @BeforeEach
  fun setup() {
    documentPlaceholderReplacementService = PoiDocumentPlaceholderReplacementService()
  }

  @Test
  fun `placeholder test`() {
    runTest {
      documentPlaceholderReplacementService.generateDocFromTemplate()
    }
  }
}
