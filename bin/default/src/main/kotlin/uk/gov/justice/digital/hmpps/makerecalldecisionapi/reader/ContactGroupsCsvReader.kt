package uk.gov.justice.digital.hmpps.makerecalldecisionapi.reader

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.csv.ContactGroup

@Component
class ContactGroupsCsvReader {
  companion object {
    private val resource: Resource = ClassPathResource("contact-groups-for-system-generated-contacts.csv")
    private var contactGroupsForSystemGeneratedContacts: List<ContactGroup>

    private val log = LoggerFactory.getLogger(this::class.java)

    fun getContactGroupsForSystemGeneratedContacts(): List<ContactGroup> {
      return contactGroupsForSystemGeneratedContacts
    }

    init {
      log.info("Building contact groups list on application startup")

      contactGroupsForSystemGeneratedContacts = csvReader().open(resource.inputStream) {
        readAllAsSequence().drop(1).map {
          ContactGroup(it)
        }.iterator().asSequence().toList()
      }
    }
  }
}
