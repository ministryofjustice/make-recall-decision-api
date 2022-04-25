plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.1.3"
  kotlin("plugin.spring") version "1.6.20"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  "5.7.3".let { sentryVersion ->
    implementation("io.sentry:sentry-spring-boot-starter:$sentryVersion")
    implementation("io.sentry:sentry-logback:$sentryVersion")
  }

  implementation("io.opentelemetry:opentelemetry-api:1.12.0")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "17"
    }
  }
}
