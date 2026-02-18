package se.sundsvall.casemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}/cases/case-mappings")
@Tag(name = "CaseMappings", description = "CaseMapping operations")
@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseMappingResource {

	private final CaseMappingService caseMappingService;

	CaseMappingResource(final CaseMappingService caseMappingService) {
		this.caseMappingService = caseMappingService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(description = "Returns the connection between externalCaseId and the case in the underlying system.")
	ResponseEntity<List<CaseMapping>> getCaseMapping(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Parameter(name = "external-case-id", description = "External case id", example = "2281") @RequestParam(name = "external-case-id", required = false) final String externalCaseId) {

		if (externalCaseId != null) {
			return ok(List.of(caseMappingService.getCaseMapping(externalCaseId, municipalityId)));
		}

		return ok(caseMappingService.getAllCaseMappings());
	}
}
