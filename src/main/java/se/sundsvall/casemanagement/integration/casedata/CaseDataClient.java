package se.sundsvall.casemanagement.integration.casedata;

import static se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataConfiguration.CLIENT_ID;

import generated.client.casedata.Attachment;
import generated.client.casedata.Errand;
import generated.client.casedata.PatchErrand;
import generated.client.casedata.Stakeholder;
import generated.client.casedata.Status;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.case-data.url}", configuration = CaseDataConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface CaseDataClient {

	@PostMapping(path = "/{municipalityId}/{namespace}/errands")
	ResponseEntity<Void> postErrands(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@RequestBody @Valid Errand errand);

	@PostMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments")
	ResponseEntity<Void> postAttachment(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long errandId,
		@RequestBody @Valid Attachment attachment);

	@PutMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}")
	ResponseEntity<Void> putAttachment(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long errandId,
		@PathVariable final Long attachmentId,
		@RequestBody @Valid Attachment attachment);

	@Operation(description = "Get all attachments.")
	@GetMapping(path = "/{municipalityId}/{namespace}/attachments/errand/{errandNumber}")
	List<Attachment> getAttachmentsByErrandNumber(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable String errandNumber);

	@Operation(description = "Delete attachment.")
	@DeleteMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments/{attachmentId}")
	ResponseEntity<Void> deleteAttachment(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long errandId,
		@PathVariable final Long attachmentId);

	@GetMapping(path = "/{municipalityId}/{namespace}/errands/{id}")
	Errand getErrand(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long id);

	@Operation(description = "Update errand.")
	@PatchMapping(path = "/{municipalityId}/{namespace}/errands/{id}")
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchErrand(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long id,
		@RequestBody @Valid PatchErrand patchErrand);

	@Operation(description = "Add/replace status on errand.")
	@PatchMapping(path = "/{municipalityId}/{namespace}/errands/{id}/status")
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchStatusOnErrand(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long id,
		@RequestBody @Valid Status status);

	@Operation(description = "Replace stakeholders on errand.")
	@PutMapping(path = "/{municipalityId}/{namespace}/errands/{id}/stakeholders")
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStakeholdersOnErrand(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long id,
		@RequestBody @Valid List<Stakeholder> stakeholderList);

	@GetMapping(path = "/{municipalityId}/{namespace}/errands")
	Page<Errand> getErrands(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@RequestParam @Parameter(
			description = "Syntax description: [spring-filter](https://github.com/turkraft/spring-filter/blob/85730f950a5f8623159cc0eb4d737555f9382bb7/README.md#syntax)",
			example = "caseType:'PARKING_PERMIT' and stakeholders.firstName~'*mar*' and applicationReceived>'2022-09-08T12:18:03.747+02:00'",
			schema = @Schema(implementation = String.class)) final String filter,
		PageRequest pageRequest);

}
