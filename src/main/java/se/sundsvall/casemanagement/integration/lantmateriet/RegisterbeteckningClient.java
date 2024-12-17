package se.sundsvall.casemanagement.integration.lantmateriet;

import static se.sundsvall.casemanagement.integration.lantmateriet.configuration.LantmaterietConfiguration.REGISTRATION_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casemanagement.integration.lantmateriet.configuration.LantmaterietConfiguration;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;

@FeignClient(name = REGISTRATION_ID, url = "${integration.lantmateriet.registerbeteckning.url}", configuration = LantmaterietConfiguration.class)
@CircuitBreaker(name = REGISTRATION_ID)
public interface RegisterbeteckningClient {

	@GetMapping(path = "/referens/fritext", produces = MediaType.APPLICATION_JSON_VALUE)
	List<Registerbeteckningsreferens> getRegisterbeteckningsreferenser(@RequestParam(name = "beteckning") String beteckning,
		@RequestParam(name = "status") String status, @RequestParam(name = "maxHits") int maxHits);

}
