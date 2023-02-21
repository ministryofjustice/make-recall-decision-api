package at

//import org.freemarker
import freemarker.template.Configuration
import freemarker.template.Version
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.PendingException
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.restassured.RestAssured
import io.restassured.response.Response
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import java.io.File
import java.io.StringWriter
import java.util.function.Consumer
import java.util.stream.Collectors

@Suppress("FunctionNaming")
@ContextConfiguration(classes = [CucumberConfiguration::class])
class StepDefinitions {

    private lateinit var lastResponse: Response
    private val token = "Bearer ${getToken()}"

    @Suppress("UnusedPrivateMember")
    @Given("the following pets are available in the store:")
    fun the_following_pets_are_available_in_the_store(dataTable: DataTable) {
        // nothing to do here!!
    }

    @When("I add a pet:")
    fun i_add_a_pet(dataTable: DataTable) {
        convertDataTableToJsonArray(dataTable).forEach {
            val next: JSONObject = it as JSONObject
            lastResponse = RestAssured
                .given()
                .contentType(APPLICATION_JSON_VALUE)
                .header("Authorization", token)
                .body(next.toString())
                .post("http://127.0.0.1:9000/catalog/add")
        }
    }

    @When("I search for {string}")
    fun i_search_for(name: String?) {
        lastResponse = RestAssured
            .given()
            .header("Authorization", token)
            .get("http://127.0.0.1:9000/catalog/search?petName=$name")
//        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.OK.value())
    }

    @Suppress("UnusedPrivateMember")
    @Given("the following pet is available in the store:")
    fun the_following_pet_is_available_in_the_store(dataTable: DataTable?) {
        // nothing to do here!!
    }

    @When("I make a request to update the price of {string} to {string}")
    fun i_make_a_request_to_update_the_price_of_to(name: String, price: String) {
        val request = JSONObject(mapOf(Pair("price", price)))
        lastResponse = RestAssured
            .given()
            .header("Authorization", token)
            .contentType(APPLICATION_JSON_VALUE)
            .body(request.toString())
            .pathParam("name", name)
            .patch("http://127.0.0.1:9000/catalog/update/{name}")
        //assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.OK.value())
    }

    @When("I ask for page {string} using a page size of {string} records")
    fun i_ask_for_page_using_a_page_size_of_records(pageNo: String?, pageSize: String?) {
        lastResponse = RestAssured
            .given()
            .header("Authorization", token)
            .queryParam("pageSize", pageSize)
            .queryParam("pageNo", pageNo)
            .get("http://127.0.0.1:9000/catalog/list")
        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.OK.value())
    }

    @When("I make a request to delete {string}")
    fun i_make_a_request_to_delete(name: String?) {
        lastResponse = RestAssured
            .given()
            .header("Authorization", token)
            .pathParam("name", name)
            .delete("http://127.0.0.1:9000/catalog/delete/{name}")
        //assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.OK.value())
    }

    @Then("the following result should be returned:")
    fun the_following_result_should_be_returned(dataTable: DataTable) {
        val expected = JSONObject(convertDataTableToJsonArray(dataTable)[0].toString())
        val actual = JSONObject(lastResponse.body().asString())
        assertEquals(expected, actual, LENIENT)
    }

    @Then("the following results should be returned:")
    fun the_following_results_should_be_returned(dataTable: DataTable) {
        val expected = convertDataTableToJsonArray(dataTable)
        val actual = JSONArray(lastResponse.body().asString())
        assertEquals(expected, actual, LENIENT)
    }

    @Then("nothing should be returned")
    fun nothing_should_be_returned() {
        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Then("no pet should be added")
    fun no_pet_should_be_added() {
        assertThat(lastResponse.body().asString()).isEqualTo("Duplicate pet")
        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
    }

    @Then("no pet {string} should be deleted")
    fun no_pet_should_be_deleted(name: String) {
        assertThat(lastResponse.body().asString()).isEqualTo("Pet $name not found")
        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Then("no pet {string} should be updated")
    fun no_pet_should_be_updated(name: String) {
        assertThat(lastResponse.body().asString()).isEqualTo("Pet $name doesn't exist")
        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    @Then("no pet is created")
    fun no_pet_is_created() {
        assertThat(lastResponse.body().asString()).isEqualTo("Pet doesn't exist")
        assertThat(lastResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND.value())
    }

    companion object {
        private var containerStarted: Boolean = false
        const val client_id = "petshopapi"
        const val client_secret = "7riSklHZfjhmEGrDFakimD2heGOBImCs"
        const val authPath = "http://localhost:8180/realms/petshoprealm/protocol/openid-connect/token"

        private fun getToken(): String {
            val tokenResponse =
                RestAssured.given()
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("grant_type", "client_credentials")
                    .formParam("client_id", client_id)
                    .formParam("client_secret", client_secret)
                    .post(authPath)
            assertThat(tokenResponse.statusCode).isEqualTo(HttpStatus.OK.value())
            return JSONObject(tokenResponse.body().asString()).getString("access_token")
        }

        private val dockerComposeContainer: DockerComposeContainer<*> =
            DockerComposeContainer<Nothing>(File("docker-compose-arm64.yml")) // for ARM64 (uses non-bitnami images)
                .waitingFor("myservice", HostPortWaitStrategy())


        @JvmStatic
        @Before
        fun beforeAll() {
            if (!containerStarted) {
                dockerComposeContainer.withLocalCompose(true) // version in testcontainers library v 1.17.5 buggy
                dockerComposeContainer.start()
                containerStarted = true
                Thread.sleep(20000) // workaround for ARM64
            }
            dockerComposeContainer.withRemoveImages(DockerComposeContainer.RemoveImages.ALL)
        }

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
