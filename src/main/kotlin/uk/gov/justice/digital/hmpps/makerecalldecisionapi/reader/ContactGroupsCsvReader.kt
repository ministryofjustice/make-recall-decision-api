package uk.gov.justice.digital.hmpps.makerecalldecisionapi.reader

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.csv.ContactGroup
import java.io.File

@Component
class ContactGroupsCsvReader {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  private val resource: Resource = ClassPathResource("csv/contact-groups.csv")
  private val file: File = resource.file
  private lateinit var contactGroups: List<ContactGroup>

  init {
    log.info("Building contact groups list on application startup")
    contactGroups = csvReader().open(file) {
      readAllAsSequence().drop(1).map {
        ContactGroup(it)
      }.iterator().asSequence().toList()
    }
  }

  fun getContactGroups(): List<ContactGroup> {
    return contactGroups
  }
}
