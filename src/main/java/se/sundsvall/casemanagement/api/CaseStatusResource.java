package se.sundsvall.casemanagement.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
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
@Tag(name = "Status", description = "Status operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseStatusResource {

	private final ByggrService byggrService;

	private final EcosService ecosService;

	private final CaseDataService caseDataService;

	private final CaseMappingService caseMappingService;

	CaseStatusResource(final ByggrService byggrService, final EcosService ecosService,
		final CaseDataService caseDataService, final CaseMappingService caseMappingService) {
		this.byggrService = byggrService;
		this.ecosService = ecosService;
		this.caseDataService = caseDataService;
		this.caseMappingService = caseMappingService;
	}

	@GetMapping(path = "organization/{organizationNumber}/cases/status", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(description = "Returns the latest status for each of the cases where the specified organization has the role \"applicant\".")
	@ApiResponse(responseCode = "200", description = "OK - Successful operation")
	public ResponseEntity<List<CaseStatusDTO>> getStatusByOrgNr(@Pattern(regexp = Constants.ORGNR_PATTERN_REGEX, message = Constants.ORGNR_PATTERN_MESSAGE)
	@Schema(description = "Organization number with 10 or 12 digits.", example = "20220622-2396")
	@PathVariable final String organizationNumber) {

		final List<CaseStatusDTO> caseStatusDTOList = new ArrayList<>();

		caseStatusDTOList.addAll(byggrService.getByggrStatusByOrgNr(organizationNumber));
		caseStatusDTOList.addAll(ecosService.getEcosStatusByOrgNr(organizationNumber));

		if (caseStatusDTOList.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(caseStatusDTOList);
	}

	@GetMapping(path = "cases/{externalCaseId}/status", produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@Operation(description = "Returns the latest status for the case in the underlying system connected to the specified externalCaseId.")
	@ApiResponse(responseCode = "200", description = "OK - Successful operation")
	public ResponseEntity<CaseStatusDTO> getStatusByExternalCaseId(@PathVariable final String externalCaseId) {

		final CaseMapping caseMapping = caseMappingService.getCaseMapping(externalCaseId);

		final CaseStatusDTO caseStatusDTO = switch (caseMapping.getSystem()) {
			case BYGGR -> byggrService.toByggrStatus(caseMapping);
			case ECOS ->
				ecosService.getStatus(caseMapping.getCaseId(), caseMapping.getExternalCaseId());
			case CASE_DATA -> caseDataService.getStatus(caseMapping);
		};

		return ResponseEntity.ok(caseStatusDTO);
	}

}
