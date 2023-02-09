package uk.gov.justice.digital.hmpps.makerecalldecisionapi.domain.csv

class ContactGroup(groups: List<String>) {
  val groupId: String = groups[0]
  val groupName: String = groups[1]
  val code: String = groups[2]
  val contactName: String = groups[3]
}
