package se.sundsvall.casemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.service.StatusService;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.casemanagement.util.Constants.ORGNR_PATTERN_MESSAGE;
import static se.sundsvall.casemanagement.util.Constants.ORGNR_PATTERN_REGEX;

@RestController
@Validated
@RequestMapping(value = "/{municipalityId}", produces = {
	APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE
})
@Tag(name = "Status", description = "Status operations")
@ApiResponse(responseCode = "200", description = "OK - Successful operation", useReturnTypeSchema = true)
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseStatusResource {

	private final StatusService statusService;

	CaseStatusResource(final StatusService statusService) {
		this.statusService = statusService;
	}

	@GetMapping(path = "/organization/{organizationNumber}/cases/status")
	@Operation(description = "Returns the latest status for each of the cases where the specified organization has the role \"applicant\".")
	ResponseEntity<List<CaseStatusDTO>> getStatusByOrgNr(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable(name = "municipalityId") final String municipalityId,
		@Pattern(regexp = ORGNR_PATTERN_REGEX, message = ORGNR_PATTERN_MESSAGE) @Schema(description = "Organization number with 10 or 12 digits.", example = "20220622-2396") @Parameter(name = "organizationNumber",
			description = "OrganizationNumber") @PathVariable(name = "organizationNumber") final String organizationNumber) {
		var caseStatuses = statusService.getStatusByOrgNr(municipalityId, organizationNumber);

		return caseStatuses.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(caseStatuses);
	}

	@GetMapping(path = "/cases/{externalCaseId}/status")
	@Operation(description = "Returns the latest status for the case in the underlying system connected to the specified externalCaseId.")
	ResponseEntity<CaseStatusDTO> getStatusByExternalCaseId(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable(name = "municipalityId") final String municipalityId,
		@Parameter(name = "externalCaseId", description = "External case id") @PathVariable(name = "externalCaseId") final String externalCaseId) {
		return ResponseEntity.ok(statusService.getStatusByExternalCaseId(municipalityId, externalCaseId));
	}

	@GetMapping(path = "/{partyId}/statuses")
	@Operation(description = "Returns the case status for all cases where the specified party is involved.")
	ResponseEntity<List<CaseStatusDTO>> getStatusesByPartyId(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable(name = "municipalityId") final String municipalityId,
		@Parameter(name = "partyId", description = "Party id") @ValidUuid @PathVariable(name = "partyId") final String partyId) {
		return ResponseEntity.ok(statusService.getStatusesByPartyId(municipalityId, partyId));
	}

}
