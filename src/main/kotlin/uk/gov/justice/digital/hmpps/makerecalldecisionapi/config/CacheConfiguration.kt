package uk.gov.justice.digital.hmpps.makerecalldecisionapi.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.support.NoOpCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import uk.gov.justice.digital.hmpps.makerecalldecisionapi.config.CacheConstants.USER_ACCESS_CACHE_KEY
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfiguration(private val buildProperties: BuildProperties) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun cacheManager(
    connectionFactory: RedisConnectionFactory,
    @Value("\${spring.cache.type:none}") isCacheEnabled: String,
  ): CacheManager {
    return if (isCacheEnabled.equals("none", ignoreCase = true)) {
      log.info("NoOpCacheManager in use")
      NoOpCacheManager()
    } else if (isCacheEnabled.equals("redis", ignoreCase = true)) {
      log.info("RedisCacheManager in use")
      RedisCacheManager.builder(connectionFactory)
        .withCacheConfiguration(USER_ACCESS_CACHE_KEY, getCacheConfiguration(Duration.ofMinutes(60)))
        .build()
    } else {
      throw RuntimeException("Unsupported cache type: '$isCacheEnabled'")
    }
  }

  private fun getCacheConfiguration(ttl: Duration): RedisCacheConfiguration {
    val customObjectMapper = jacksonObjectMapper()
      .activateDefaultTyping(jacksonObjectMapper().polymorphicTypeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    val jackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(customObjectMapper)

    return RedisCacheConfiguration.defaultCacheConfig()
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer))
      .prefixCacheNameWith("${buildProperties.version}-")
      .entryTtl(ttl)
  }
}
