package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun amazonS3(@Value("\${s3.local.endpoint}") endpoint: String?, @Value("\${s3.local.region}") region: String?): AmazonS3 {
//    System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true")

    if (endpoint != null && endpoint.isNotEmpty()) {
      log.info("Using localstack with endpoint: $endpoint")
      return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .withPathStyleAccessEnabled(true)
        //TODO test with this lines commented out.
        .withCredentials(AWSStaticCredentialsProvider(BasicAWSCredentials("foobar", "foobar")))
        .build()
    }

    log.info("Using Real S3 with IRSA")
    return AmazonS3ClientBuilder.standard()
      .withRegion(region)
      .withPathStyleAccessEnabled(true)
      .withCredentials(
        WebIdentityTokenCredentialsProvider(),
      ).build()
  }
}