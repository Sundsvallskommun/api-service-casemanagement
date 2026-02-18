package se.sundsvall.casemanagement.integration.oepintegrator;

import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.ConfirmDeliveryRequest;
import generated.client.oep_integrator.InstanceType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casemanagement.integration.oepintegrator.configuration.OepIntegratorConfiguration;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casemanagement.integration.oepintegrator.configuration.OepIntegratorConfiguration.REGISTRATION_ID;

@FeignClient(name = REGISTRATION_ID, url = "${integration.oep-integrator.url}", configuration = OepIntegratorConfiguration.class, dismiss404 = true)
@CircuitBreaker(name = REGISTRATION_ID)
public interface OepIntegratorClient {

	@PostMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}/delivery", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> confirmDelivery(
		@PathVariable final String municipalityId,
		@PathVariable final InstanceType instanceType,
		@PathVariable final String flowInstanceId,
		@RequestBody final ConfirmDeliveryRequest confirmDeliveryRequest);

	@PutMapping(value = "{municipalityId}/{instanceType}/cases/{flowInstanceId}/status", consumes = APPLICATION_JSON_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> setStatus(
		@PathVariable final String municipalityId,
		@PathVariable final InstanceType instanceType,
		@PathVariable final String flowInstanceId,
		@RequestBody final CaseStatusChangeRequest setStatusRequest);
}
