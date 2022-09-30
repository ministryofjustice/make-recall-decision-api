package uk.gov.justice.digital.hmpps.makerecalldecisionapi.reader

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContactGroupsCsvReaderTest {

  private lateinit var csvReader: ContactGroupsCsvReader

  @BeforeEach
  fun setup() {
    csvReader = ContactGroupsCsvReader()
  }

  @Test
  fun `given a csv file then read the contents and map to list of contact groups`() {
    val result = ContactGroupsCsvReader.getContactGroups()
    assertEquals(344, result.size)
    assertEquals("1", result.get(0).groupId)
    assertEquals("Administrative", result.get(0).groupName)
    assertEquals("C070", result.get(0).code)
    assertEquals("Case Allocated", result.get(0).contactName)
    assertEquals("2", result.get(56).groupId)
    assertEquals("Anti-terrorism", result.get(56).groupName)
    assertEquals("PREVENT", result.get(56).code)
    assertEquals("Prevent Activity", result.get(56).contactName)
  }
}
