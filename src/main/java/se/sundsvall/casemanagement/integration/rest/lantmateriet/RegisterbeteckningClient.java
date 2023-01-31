package se.sundsvall.casemanagement.integration.rest.lantmateriet;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.configuration.LantmaterietConfiguration;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.model.Registerbeteckningsreferens;

import java.util.List;

//@Path("/v4")
@FeignClient(name = LantmaterietConfiguration.REGISTRATION_ID, url = "${integration.lantmateriet.registerbeteckning.url}", configuration = LantmaterietConfiguration.class)
@CircuitBreaker(name = LantmaterietConfiguration.REGISTRATION_ID)

public interface RegisterbeteckningClient {

    @GetMapping(path = "/referens/fritext/{beteckning}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Registerbeteckningsreferens> getRegisterbeteckningsreferenser(@PathVariable String beteckning,
                                                                       @RequestParam(name = "statusFastighet") String status, @RequestParam(name = "maxHits") int maxHits);

}
