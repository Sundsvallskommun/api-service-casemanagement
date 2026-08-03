package se.sundsvall.casemanagement.integration.casedata;

import feign.form.FormData;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataConfiguration;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.case-data.url}", configuration = CaseDataConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface CaseDataClient {

	@CircuitBreaker(name = "caseDataMetadata")
	@GetMapping(path = "/{municipalityId}/{namespace}/metadata/casetypes")
	List<generated.client.casedata.CaseType> getCaseTypes(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace);

	@PostMapping(path = "/{municipalityId}/{namespace}/errands")
	ResponseEntity<Void> postErrands(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@RequestBody @Valid Errand errand);

	/**
	 * Creates an attachment on an errand. CaseData expects the attachment metadata as a JSON part named 'attachment' and
	 * the binary content as a file part named 'file'.
	 *
	 * @param attachment the attachment metadata, serialized as JSON
	 * @param file       the decoded (binary) file content
	 */
	@PostMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments", consumes = MULTIPART_FORM_DATA_VALUE, produces = ALL_VALUE)
	ResponseEntity<Void> postAttachment(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long errandId,
		@RequestPart("attachment") final FormData attachment,
		@RequestPart("file") final FormData file);

	@Operation(description = "Get attachment metadata for an errand.")
	@GetMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}/attachments")
	List<Attachment> getAttachmentsByErrandId(
		@PathVariable final String municipalityId,
		@PathVariable final String namespace,
		@PathVariable final Long errandId);

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
		@RequestParam @Parameter(
			description = "CaseData uses pagination but we always need to fetch every errand for the given filter. Set the size to a high number to fetch all errands.",
			example = "1000",
			schema = @Schema(implementation = String.class)) final String size);

}
