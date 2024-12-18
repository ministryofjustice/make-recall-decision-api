package uk.gov.justice.digital.hmpps.makerecalldecisionapi.integration.responses.automation

fun ppudAutomationSearchActiveUsersResponse(fullName: String, teamName: String) = """
{
   "results": [
        {
            "fullName": "$fullName",
            "teamName": "$teamName",
            "formattedFullNameAndTeam${"$"}hmpps_ppud_automation_api": "$fullName($teamName)"
        }
   ]
}
""".trimIndent()
