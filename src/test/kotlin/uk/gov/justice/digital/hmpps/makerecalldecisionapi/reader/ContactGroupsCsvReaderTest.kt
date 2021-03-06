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
    assertEquals(44, result.size)
    assertEquals("1", result.get(0).groupId)
    assertEquals("Appointment", result.get(0).groupName)
    assertEquals("COAI", result.get(0).code)
    assertEquals("Initial Appointment - In office (NS)", result.get(0).contactName)
    assertEquals("2", result.get(4).groupId)
    assertEquals("Home Visit", result.get(4).groupName)
    assertEquals("C253", result.get(4).code)
    assertEquals("Home Visit - Not Notified", result.get(4).contactName)
  }
}
