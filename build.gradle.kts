plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.5"
  kotlin("jvm") version "1.9.23"
  id("org.unbroken-dome.test-sets") version "4.1.0"
  id("jacoco")
  kotlin("plugin.jpa") version "1.9.23"
  id("org.sonarqube") version "5.0.0.4638"
  kotlin("plugin.spring") version "1.9.23"
}

jacoco.toolVersion = "0.8.11"

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencyCheck {
  suppressionFiles.add("suppressions.xml")
}

testSets {
  "testSmoke"()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-actuator:3.2.4")
  implementation("io.micrometer:micrometer-registry-prometheus:1.12.5")
  implementation("io.opentelemetry:opentelemetry-api:1.37.0")
  implementation("joda-time:joda-time:2.12.7")
  implementation("com.deepoove:poi-tl:1.12.2") {
    // exclude apache.xmlgraphics batik due to vulnerabilities when imported with poi-tl
    exclude("org.apache.xmlgraphics", "batik-codec")
    exclude("org.apache.xmlgraphics", "batik-transcoder")
    implementation("org.apache.commons:commons-compress:1.26.1") // Address CVE-2024-25710 and CVE-2024-26308 present in v1.21
  }
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  implementation("org.flywaydb:flyway-core:10.11.1")
  implementation("org.flywaydb:flyway-database-postgresql:10.11.1")
  implementation("org.postgresql:postgresql:42.7.3")

  implementation("io.sentry:sentry-spring-boot-starter-jakarta:7.8.0")
  implementation("io.sentry:sentry-logback:7.8.0")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
  implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.7.4")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:3.1.2")
  implementation("org.json:json:20240303")

  testImplementation("org.awaitility:awaitility-kotlin:4.2.1")
  testImplementation("org.mock-server:mockserver-netty:5.15.0")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("io.jsonwebtoken:jjwt:0.12.5")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
  testImplementation("org.flywaydb.flyway-test-extensions:flyway-spring-test:10.0.0")

  testImplementation("io.rest-assured:rest-assured")
  testImplementation("io.rest-assured:json-path")
  testImplementation("io.rest-assured:xml-path")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}

tasks.test {
  jvmArgs("-XX:+EnableDynamicAgentLoading")
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
  }
}

val SourceSet.kotlin: SourceDirectorySet
  get() = project.extensions.getByType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>().sourceSets.getByName(
    name,
  ).kotlin

sourceSets {
  create("functional-test") {
    kotlin.srcDirs("src/functional-test")
    compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
    runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
  }
}

task<Test>("functional-test-light") {
  description = "Runs the functional test, will require start-local-development.sh " +
    "and docker compose-postgres.yml to be started manually"
  group = "verification"
  testClassesDirs = sourceSets["functional-test"].output.classesDirs
  classpath = sourceSets["functional-test"].runtimeClasspath
  useJUnitPlatform()
}

tasks.withType<Jar> {
  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
