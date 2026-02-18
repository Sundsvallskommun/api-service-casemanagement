package se.sundsvall.casemanagement.integration.lantmateriet;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casemanagement.integration.lantmateriet.configuration.LantmaterietConfiguration;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;

import static se.sundsvall.casemanagement.integration.lantmateriet.configuration.LantmaterietConfiguration.REGISTRATION_ID;

@FeignClient(name = REGISTRATION_ID, url = "${integration.lantmateriet.registerbeteckning.url}", configuration = LantmaterietConfiguration.class)
@CircuitBreaker(name = REGISTRATION_ID)
public interface RegisterbeteckningClient {

	@GetMapping(path = "/referens/fritext")
	List<Registerbeteckningsreferens> getRegisterbeteckningsreferenser(
		@RequestParam String beteckning,
		@RequestParam String status,
		@RequestParam int maxHits);
}
