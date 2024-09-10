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

import generated.client.casedata.AttachmentDTO;
import generated.client.casedata.ErrandDTO;
import generated.client.casedata.PatchErrandDTO;
import generated.client.casedata.StakeholderDTO;
import generated.client.casedata.StatusDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@FeignClient(name = CaseDataConfiguration.REGISTRATION_ID, url = "${integration.case-data.url}", configuration = CaseDataConfiguration.class)
@CircuitBreaker(name = CaseDataConfiguration.REGISTRATION_ID)
public interface CaseDataClient {

	@PostMapping(path = "/{municipalityId}/errands", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> postErrands(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@RequestBody @Valid ErrandDTO errandDTO);

	@PostMapping(path = "/{municipalityId}/attachments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> postAttachment(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@RequestBody @Valid AttachmentDTO attachmentDTO);

	@PutMapping(path = "/{municipalityId}/attachments/{attachmentId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> putAttachment(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "attachmentId") final Long attachmentId,
		@RequestBody @Valid AttachmentDTO attachmentDTO);

	@Operation(description = "Get all attachments.")
	@GetMapping(path = "/{municipalityId}/attachments/errand/{errandNumber}", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	List<AttachmentDTO> getAttachmentsByErrandNumber(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "errandNumber") String errandNumber);

	@Operation(description = "Delete attachment.")
	@DeleteMapping(path = "/{municipalityId}/attachments/{id}", produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> deleteAttachment(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "id") final Long id);

	@GetMapping(path = "/{municipalityId}/errands/{id}", consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	ErrandDTO getErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "id") final Long id);

	@Operation(description = "Update errand.")
	@PatchMapping(path = "/{municipalityId}/errands/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "id") final Long id,
		@RequestBody @Valid PatchErrandDTO patchErrandDTO);

	@Operation(description = "Add/replace status on errand.")
	@PutMapping(path = "/{municipalityId}/errands/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStatusOnErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "id") final Long id,
		@RequestBody @Valid List<StatusDTO> statusDTOList);

	@Operation(description = "Replace stakeholders on errand.")
	@PutMapping(path = "/{municipalityId}/errands/{id}/stakeholders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStakeholdersOnErrand(
		@PathVariable(name = "municipalityId") final String municipalityId,
		@PathVariable(name = "id") final Long id,
		@RequestBody @Valid List<StakeholderDTO> stakeholderDTOList);

}
