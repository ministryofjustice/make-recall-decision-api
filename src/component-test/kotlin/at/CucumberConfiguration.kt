package at

import io.cucumber.java.Before
import io.cucumber.spring.CucumberContextConfiguration
import org.junit.ClassRule
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

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
