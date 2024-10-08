package se.sundsvall.casemanagement.integration.casedata;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataConfiguration;

import generated.client.casedata.Attachment;
import generated.client.casedata.Errand;
import generated.client.casedata.PatchErrand;
import generated.client.casedata.Stakeholder;
import generated.client.casedata.Status;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@FeignClient(name = CaseDataConfiguration.REGISTRATION_ID, url = "${integration.case-data.url}", configuration = CaseDataConfiguration.class)
@CircuitBreaker(name = CaseDataConfiguration.REGISTRATION_ID)
public interface CaseDataClient {

	@PostMapping(path = "/{municipalityId}/{namespace}/errands", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> postErrands(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@RequestBody @Valid Errand errand);

	@PostMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> postAttachment(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@RequestBody @Valid Attachment attachment);

	@PutMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> putAttachment(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "attachmentId") final Long attachmentId,
		@RequestBody @Valid Attachment attachment);

	@Operation(description = "Get all attachments.")
	@GetMapping(path = "/{municipalityId}/{namespace}/attachments/errand/{errandNumber}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	List<Attachment> getAttachmentsByErrandNumber(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "errandNumber") String errandNumber);

	@Operation(description = "Delete attachment.")
	@DeleteMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteAttachment(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "errandId") final Long errandId,
		@PathVariable(name = "attachmentId") final Long attachmentId);

	@GetMapping(path = "/{municipalityId}/{namespace}/errands/{id}", consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	Errand getErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "id") final Long id);

	@Operation(description = "Update errand.")
	@PatchMapping(path = "/{municipalityId}/{namespace}/errands/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "id") final Long id,
		@RequestBody @Valid PatchErrand patchErrand);

	@Operation(description = "Add/replace status on errand.")
	@PutMapping(path = "/{municipalityId}/{namespace}/errands/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStatusOnErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "id") final Long id,
		@RequestBody @Valid List<Status> statusList);

	@Operation(description = "Replace stakeholders on errand.")
	@PutMapping(path = "/{municipalityId}/{namespace}/errands/{id}/stakeholders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStakeholdersOnErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "namespace") final String namespace,
		@PathVariable(name = "id") final Long id,
		@RequestBody @Valid List<Stakeholder> stakeholderList);

}
