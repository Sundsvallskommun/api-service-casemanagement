package se.sundsvall.casemanagement.integration.opene;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import callback.ConfirmDelivery;
import callback.ConfirmDeliveryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.casemanagement.integration.opene.configuration.OpeneConfiguration;

@FeignClient(name = "opene", url = "${integration.opene.url}", configuration = OpeneConfiguration.class)
@CircuitBreaker(name = "opene")
public interface OpeneClient {

	String TEXT_XML_UTF_8 = "text/xml; charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF_8, produces = TEXT_XML_UTF_8)
	ConfirmDeliveryResponse confirmDelivery(@RequestBody ConfirmDelivery confirmDelivery);
}
