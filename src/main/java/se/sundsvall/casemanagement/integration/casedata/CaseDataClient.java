package se.sundsvall.casemanagement.integration.casedata;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import javax.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import generated.client.casedata.AttachmentDTO;
import generated.client.casedata.ErrandDTO;
import generated.client.casedata.PatchErrandDTO;
import generated.client.casedata.StakeholderDTO;
import generated.client.casedata.StatusDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataConfiguration;

@FeignClient(name = CaseDataConfiguration.REGISTRATION_ID, url = "${integration.case-data.url}", configuration = CaseDataConfiguration.class)
@CircuitBreaker(name = CaseDataConfiguration.REGISTRATION_ID)
public interface CaseDataClient {

	@PostMapping(path = "/errands", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> postErrands(ErrandDTO errandDTO);

	@PatchMapping(path = "/errands/{id}/attachments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_PROBLEM_JSON_VALUE)
	ResponseEntity<Void> patchErrandWithAttachment(@PathVariable Long id, AttachmentDTO attachmentDTO);

	@GetMapping(path = "/errands/{id}", consumes = APPLICATION_JSON_VALUE, produces = { APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
	ErrandDTO getErrand(@PathVariable Long id);

	@Operation(description = "Update errand.")
	@PatchMapping(path = "/errands/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> patchErrand(@PathVariable Long id, @RequestBody @Valid PatchErrandDTO patchErrandDTO);

	@Operation(description = "Add/replace status on errand.")
	@PutMapping(path = "/errands/{id}/statuses", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStatusOnErrand(@PathVariable Long id, @RequestBody @Valid List<StatusDTO> statusDTOList);

	@Operation(description = "Replace attachments on errand.")
	@PutMapping(path = "/errands/{id}/attachments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putAttachmentsOnErrand(@PathVariable Long id, @RequestBody @Valid List<AttachmentDTO> attachmentDTOList);

	@Operation(description = "Replace stakeholders on errand.")
	@PutMapping(path = "/errands/{id}/stakeholders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = { APPLICATION_PROBLEM_JSON_VALUE })
	@ApiResponse(responseCode = "204", description = "No content - Successful operation")
	ResponseEntity<Void> putStakeholdersOnErrand(@PathVariable Long id, @RequestBody @Valid List<StakeholderDTO> stakeholderDTOList);
}
