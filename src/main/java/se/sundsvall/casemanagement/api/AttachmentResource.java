package se.sundsvall.casemanagement.api;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/")
@Tag(name = "Attachments", description = "Attachment operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "501", description = "Not Implemented", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class AttachmentResource {

	private final ByggrService byggrService;

	private final EcosService ecosService;

	private final CaseDataService caseDataService;

	private final CaseMappingService caseMappingService;

	public AttachmentResource(final ByggrService byggrService, final EcosService ecosService, final CaseDataService caseDataService, final CaseMappingService caseMappingService) {
		this.byggrService = byggrService;
		this.ecosService = ecosService;
		this.caseDataService = caseDataService;
		this.caseMappingService = caseMappingService;
	}

	@Operation(description = "Add attachments to existing case.")
	@PostMapping(path = "cases/{externalCaseId}/attachments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content - Successful request.")
	public ResponseEntity<Void> postAttachmentsToCase(@PathVariable final String externalCaseId,
		@NotNull(message = Constants.REQUEST_BODY_MUST_NOT_BE_NULL)
		@RequestBody @Valid final List<AttachmentDTO> attachmentDTOList) {

		final CaseMapping caseMapping = caseMappingService.getCaseMapping(externalCaseId);

		switch (caseMapping.getSystem()) {
			case BYGGR ->
				byggrService.saveNewIncomingAttachmentHandelse(caseMapping.getCaseId(), attachmentDTOList);
			case ECOS -> ecosService.addDocumentsToCase(caseMapping.getCaseId(), attachmentDTOList);
			case CASE_DATA ->
				caseDataService.patchErrandWithAttachment(caseMapping.getExternalCaseId(), attachmentDTOList);
			default ->
				throw Problem.valueOf(Status.BAD_REQUEST, "It should not be possible to reach this row. systemType was: " + caseMapping.getSystem());
		}

		return ResponseEntity.noContent().build();
	}

}
