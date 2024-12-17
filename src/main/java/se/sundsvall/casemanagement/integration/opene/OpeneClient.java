package se.sundsvall.casemanagement.integration.opene;

import static se.sundsvall.casemanagement.integration.opene.configuration.OpeneConfiguration.CLIENT_ID;

import callback.ConfirmDelivery;
import callback.SetStatus;
import callback.SetStatusResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casemanagement.integration.opene.configuration.OpeneConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.opene.url}", configuration = OpeneConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpeneClient {

	String TEXT_XML_UTF_8 = "text/xml; charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF_8, produces = TEXT_XML_UTF_8)
	void confirmDelivery(@RequestBody ConfirmDelivery confirmDelivery);

	@PostMapping(consumes = TEXT_XML_UTF_8, produces = TEXT_XML_UTF_8)
	SetStatusResponse setStatus(@RequestBody SetStatus status);
}
