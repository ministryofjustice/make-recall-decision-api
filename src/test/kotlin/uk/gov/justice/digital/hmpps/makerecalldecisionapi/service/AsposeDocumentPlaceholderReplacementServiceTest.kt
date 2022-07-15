package uk.gov.justice.digital.hmpps.makerecalldecisionapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
@ExperimentalCoroutinesApi
internal class AsposeDocumentPlaceholderReplacementServiceTest : ServiceTestBase() {

  private lateinit var documentPlaceholderReplacementService: AsposeDocumentPlaceholderReplacementService

  @BeforeEach
  fun setup() {
    documentPlaceholderReplacementService = AsposeDocumentPlaceholderReplacementService()
  }

  @Test
  fun `placeholder test`() {
    runTest {
      documentPlaceholderReplacementService.generateDocFromTemplate()
    }
  }
}
