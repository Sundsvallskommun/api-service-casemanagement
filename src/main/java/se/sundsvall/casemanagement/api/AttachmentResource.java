package se.sundsvall.casemanagement.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.casemanagement.util.Constants.REQUEST_BODY_MUST_NOT_BE_NULL;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.ecos.EcosService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}", consumes = APPLICATION_JSON_VALUE, produces = {
	APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
})
@Tag(name = "Attachments", description = "Attachment operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class AttachmentResource {

	private final ByggrService byggrService;
	private final EcosService ecosService;
	private final CaseDataService caseDataService;
	private final CaseMappingService caseMappingService;

	AttachmentResource(final ByggrService byggrService, final EcosService ecosService,
		final CaseDataService caseDataService, final CaseMappingService caseMappingService) {
		this.byggrService = byggrService;
		this.ecosService = ecosService;
		this.caseDataService = caseDataService;
		this.caseMappingService = caseMappingService;
	}

	@Operation(description = "Post attachments to a case", responses = {
		@ApiResponse(responseCode = "204", description = "No content - Successful request", useReturnTypeSchema = true)
	})
	@PostMapping(path = "cases/{externalCaseId}/attachments")
	ResponseEntity<Void> postAttachmentsToCase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable(name = "municipalityId") final String municipalityId,
		@Parameter(name = "externalCaseId", description = "External case id", example = "1234") @PathVariable(name = "externalCaseId") final String externalCaseId,
		@NotNull(message = REQUEST_BODY_MUST_NOT_BE_NULL) @RequestBody @Valid final List<AttachmentDTO> attachmentDTOList) {

		final CaseMapping caseMapping = caseMappingService.getCaseMapping(externalCaseId, municipalityId);

		switch (caseMapping.getSystem()) {
			case BYGGR -> byggrService.saveNewIncomingAttachmentHandelse(caseMapping.getCaseId(), attachmentDTOList);
			case ECOS -> ecosService.addDocumentsToCase(caseMapping.getCaseId(), attachmentDTOList);
			case CASE_DATA -> caseDataService.patchErrandWithAttachment(caseMapping, attachmentDTOList, municipalityId);
			default -> throw Problem.valueOf(Status.BAD_REQUEST, "It should not be possible to reach this row. systemType was: " + caseMapping.getSystem());
		}
		return ResponseEntity.noContent().build();
	}

}
