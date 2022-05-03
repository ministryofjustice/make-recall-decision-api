plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.1.5"
  kotlin("plugin.spring") version "1.6.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  "5.7.3".let { sentryVersion ->
    implementation("io.sentry:sentry-spring-boot-starter:$sentryVersion")
    implementation("io.sentry:sentry-logback:$sentryVersion")

    testImplementation("org.mock-server:mockserver-netty:5.13.2")
  }

  implementation("io.opentelemetry:opentelemetry-api:1.13.0")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:1.6.7")
  implementation("org.springdoc:springdoc-openapi-ui:1.6.7")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.6.7")
  implementation("org.springdoc:springdoc-openapi-data-rest:1.6.7")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.natpryce:hamkrest:1.8.0.1")
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
    vendor.set(JvmVendorSpec.matching("AdoptOpenJDK"))
  }
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}
