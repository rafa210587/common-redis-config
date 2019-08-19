package comum.redis.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@EnableCaching
@Configuration
@ComponentScan
@Primary
public class RedisConfig {

	@Value("${redis.host}")
	private String host;

	@Value("${redis.port}")
	private Integer port;

	@Value("${redis.password}")
	private String password;

	@Value("${redis.time}")
	private Integer time;

	/**
	 * General configuration to set host, port and password
	 * 
	 * @return LettuceConnectionFactory
	 */
	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration();
		redisConf.setHostName(host);
		redisConf.setPort(port);
		redisConf.setPassword(RedisPassword.of(password));
		return new LettuceConnectionFactory(redisConf);
	}

	/**
	 * Configuration to set 15 seconds of life time and dont register if it's a null
	 * value
	 * 
	 * @return cacheConfig
	 */
	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		if (time < 600) {
			RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
					.entryTtl(Duration.ofSeconds(time)).disableCachingNullValues();
			return cacheConfig;
		} else {
			RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
					.disableCachingNullValues();
			return cacheConfig;
		}
	}

	@Bean
	public RedisCacheManager cacheManager() {
		RedisCacheManager rcm = RedisCacheManager.builder(redisConnectionFactory()).cacheDefaults(cacheConfiguration())
				.transactionAware().build();
		return rcm;
	}
}