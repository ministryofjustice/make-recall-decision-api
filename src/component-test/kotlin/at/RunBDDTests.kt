package at;

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.ClassRule
import org.junit.runner.RunWith
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.DockerComposeContainer
import java.io.File

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["classpath:features/"],
  glue = ["at"],
    plugin = [
    "pretty",
    "json:build/cucumber-reports/json/cucumber.json",
    "html:build/cucumber-reports/html/cucumber.html"
    ]
)

//(
////this code will only look into "features/" folder for features
//features={"classpath:features/"},
//glue = { "com.mycompany.cucumber.stepdefinitions", "com.mycompany.cucumber.hooks" },
//format = { "com.mycompany.cucumber.formatter.RuntimeInfoCatcher", "json:target/cucumber.json" },
//tags = { "@working" }
//)
class RunBDDTests
