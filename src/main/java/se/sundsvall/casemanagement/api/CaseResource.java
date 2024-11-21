package se.sundsvall.casemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CaseService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;
import static se.sundsvall.casemanagement.util.Constants.POST_CASES_REQUEST_BODY_EXAMPLE;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}", consumes = APPLICATION_JSON_VALUE, produces = {
	APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
})
@Tag(name = "Cases", description = "Cases operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseResource {

	private final CaseMappingService caseMappingService;
	private final CaseService caseService;
	private final CaseDataService caseDataService;

	CaseResource(final CaseMappingService caseMappingService, final CaseService caseService,
		final CaseDataService caseDataService) {
		this.caseMappingService = caseMappingService;
		this.caseService = caseService;
		this.caseDataService = caseDataService;
	}

	@PostMapping(path = "cases")
	@Operation(description = "Creates a case in ByggR or Ecos2 based on caseType. Also persists a connection between externalCaseId and the created case.")
	@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	ResponseEntity<CaseResourceResponseDTO> postCases(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable(name = "municipalityId") final String municipalityId,
		@Schema(oneOf = {
			ByggRCaseDTO.class, EcosCaseDTO.class, OtherCaseDTO.class
		}, example = POST_CASES_REQUEST_BODY_EXAMPLE) @RequestBody @Valid final CaseDTO caseDTOInput) {

		// Validates that it doesn't exist any case with the same oep-ID and municipalityId
		caseMappingService.validateUniqueCase(caseDTOInput, municipalityId);
		caseService.handleCase(caseDTOInput, municipalityId);

		return ResponseEntity.ok(new CaseResourceResponseDTO("Inskickat"));
	}

	@PutMapping(path = "cases/{externalCaseId}")
	@Operation(description = "Update a case.")
	@ApiResponse(responseCode = "204", description = "No content", useReturnTypeSchema = true)
	ResponseEntity<Void> putCase(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable(name = "municipalityId") final String municipalityId,
		@Parameter(name = "externalCaseId", description = "External case id", example = "1234") @PathVariable(name = "externalCaseId") final String externalCaseId,
		@Schema(oneOf = {
			ByggRCaseDTO.class, EcosCaseDTO.class, OtherCaseDTO.class
		}, example = POST_CASES_REQUEST_BODY_EXAMPLE) @RequestBody @Valid final CaseDTO caseDTOInput) {
		if (caseDTOInput instanceof final OtherCaseDTO otherCaseDTO) {
			caseDataService.putErrand(Long.valueOf(caseMappingService.getCaseMapping(externalCaseId, municipalityId).getCaseId()), otherCaseDTO, municipalityId);
			return noContent().build();
		} else {
			throw Problem.valueOf(Status.BAD_REQUEST, "No support for updating cases of the given type.");
		}
	}

}
