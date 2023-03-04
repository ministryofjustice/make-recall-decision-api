package bat

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@CucumberContextConfiguration
class CucumberConfiguration {
    //https://www.wwt.com/article/using-testcontainers-for-unit-tests-with-spring-and-kotlin
    private constructor()



//        .withExposedService("mariadb", 3306);

//companion object {
//    @Container
//    var dockerComposeContainer: DockerComposeContainer<*> =
//        DockerComposeContainer<Nothing>(File("docker-compose.yml"))
//    @JvmStatic
//    fun start() {
//        dockerComposeContainer.start()
//    }
//}



}
