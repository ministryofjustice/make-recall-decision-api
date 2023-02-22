package at

//import org.freemarker
import freemarker.template.Configuration
import freemarker.template.Version
import io.cucumber.datatable.DataTable
import io.cucumber.java.PendingException
import io.cucumber.java.en.Given
import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import java.io.StringWriter
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.HashMap

@Suppress("FunctionNaming")
@ContextConfiguration(classes = [CucumberConfiguration::class])
class StepDefinitions {

    private lateinit var lastResponse: Response
//    private val token = "Bearer ${getToken()}"


  @Given("I get a token")
  fun i_get_a_token() {
    val token = "Bearer ${getToken()}"
    println(token)
    throw PendingException()
  }


    companion object {
        private var containerStarted: Boolean = false
        const val client_id = "make-recall-decision-api"
        const val client_secret = "clientsecret"
        const val authPath = "http://localhost:9090/auth/oauth/token?grant_type=client_credentials&username=MAKE_RECALL_DECISION_USER"


//      val client_id = System.getenv("SYSTEM_CLIENT_ID")
//      val client_secret = System.getenv("SYSTEM_CLIENT_SECRET")
      val base64EncodedClientCreds = Base64.getEncoder().encodeToString("$client_id:$client_secret".toByteArray())
      val authHeaderValue = "Basic $base64EncodedClientCreds"
//      val authPath = "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username=${System.getenv("USER_NAME")}"
//      val path = "http://127.0.0.1:8080"

//      @BeforeAll
      private fun getToken(): String {
        val tokenResponse = RestAssured
          .given()
          .contentType(MediaType.APPLICATION_JSON_VALUE)
          .header("Authorization", authHeaderValue)
          .post(authPath)
        assertThat(tokenResponse.statusCode).isEqualTo(HttpStatus.OK.value())
        return JSONObject(tokenResponse.body().asString()).getString("access_token")
      }

//        private fun getToken(): String {
//            val tokenResponse =
//                RestAssured.given()
//                    .contentType("application/x-www-form-urlencoded")
//                    .formParam("grant_type", "client_credentials")
//                    .formParam("client_id", client_id)
//                    .formParam("client_secret", client_secret)
//                    .post(authPath)
//            assertThat(tokenResponse.statusCode).isEqualTo(HttpStatus.OK.value())
//            return JSONObject(tokenResponse.body().asString()).getString("access_token")
//        }

//        private val dockerComposeContainer: DockerComposeContainer<*> =
//            DockerComposeContainer<Nothing>(File("docker-compose-arm64.yml")) // for ARM64 (uses non-bitnami images)
//                .waitingFor("myservice", HostPortWaitStrategy())


//        @JvmStatic
//        @Before
//        fun beforeAll() {
//            if (!containerStarted) {
//                dockerComposeContainer.withLocalCompose(true) // version in testcontainers library v 1.17.5 buggy
//                dockerComposeContainer.start()
//                containerStarted = true
//                Thread.sleep(20000) // workaround for ARM64
//            }
//            dockerComposeContainer.withRemoveImages(DockerComposeContainer.RemoveImages.ALL)
//        }

        fun convertDataTableToJsonArray(
            dataTable: DataTable,
            vararg columnsToIgnore: String
        ): JSONArray {
            return JSONArray(dataTable
                .asMaps()
                .stream()
                .map { map: Map<String, String> ->
                    removeColumns(
                        map,
                        *columnsToIgnore
                    )
                }
                .collect(Collectors.toList()))
        }

        private fun removeColumns(
            originalMapOfDataTable: Map<String, String>,
            vararg columnsToIgnore: String
        ): HashMap<String, String> {
            val modifiedMap = HashMap(originalMapOfDataTable)
            listOf(*columnsToIgnore)
                .forEach(Consumer { key: String? -> modifiedMap.remove(key) })
            return modifiedMap
        }

        fun createRequestFromDataTable(dataTable: DataTable, templateFile: String): String {
            var response: String
            val cfg = Configuration(Version("2.3.23"))
            cfg.setClassForTemplateLoading(Companion::class.java, "/")
            cfg.defaultEncoding = "UTF-8"
            val template = cfg.getTemplate(templateFile)
            StringWriter().use {
                template.process(dataTable.asMaps()[0], it)
                response = it.buffer.toString()
                it.flush()
            }
            return response
        }
    }

}
