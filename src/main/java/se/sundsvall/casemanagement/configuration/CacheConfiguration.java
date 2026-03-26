package se.sundsvall.casemanagement.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

	@Bean
	CacheManager cacheManager() {
		final var manager = new CaffeineCacheManager("caseDataCaseTypes");
		manager.setCaffeine(Caffeine.newBuilder()
			.expireAfterWrite(15, TimeUnit.MINUTES)
			.maximumSize(100));
		return manager;
	}

}
