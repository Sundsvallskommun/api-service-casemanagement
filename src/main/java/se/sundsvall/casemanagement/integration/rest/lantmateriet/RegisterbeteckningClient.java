package se.sundsvall.casemanagement.integration.rest.lantmateriet;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import se.sundsvall.casemanagement.integration.rest.lantmateriet.configuration.LantmaterietConfiguration;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.model.Registerbeteckningsreferens;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@FeignClient(name = LantmaterietConfiguration.REGISTRATION_ID, url = "${integration.lantmateriet.registerbeteckning.url}", configuration = LantmaterietConfiguration.class)
@CircuitBreaker(name = LantmaterietConfiguration.REGISTRATION_ID)

public interface RegisterbeteckningClient {

    @GetMapping(path = "/referens/fritext", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Registerbeteckningsreferens> getRegisterbeteckningsreferenser(@RequestParam(name = "beteckning") String beteckning,
        @RequestParam(name = "status") String status, @RequestParam(name = "maxHits") int maxHits);

}
