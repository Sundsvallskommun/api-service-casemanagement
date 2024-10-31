package se.sundsvall.casemanagement.integration.ecos;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentResponse;
import se.sundsvall.casemanagement.integration.ecos.configuration.MinutMiljoConfiguration;

@FeignClient(name = "minutmiljoV2", url = "${integration.minutmiljoV2.url}", configuration = MinutMiljoConfiguration.class)
@CircuitBreaker(name = "minutmiljoV2")
public interface MinutMiljoClientV2 {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V2/IMinutMiljoServiceV2/RegisterDocument"
	})
	RegisterDocumentResponse registerDocumentV2(
		RegisterDocument registerDocument);
}
