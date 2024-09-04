package se.sundsvall.casemanagement.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
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

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.casedata.CaseDataService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CaseService;
import se.sundsvall.casemanagement.util.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Validated
@RequestMapping("/")
@Tag(name = "Cases", description = "Cases operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class CaseResource {

	private final CaseMappingService caseMappingService;

	private final CaseService caseService;

	private final CaseDataService caseDataService;

	private final ByggrService byggrService;

	CaseResource(final CaseMappingService caseMappingService, final CaseService caseService,
		final CaseDataService caseDataService, final ByggrService byggrService) {
		this.caseMappingService = caseMappingService;
		this.caseService = caseService;
		this.caseDataService = caseDataService;
		this.byggrService = byggrService;
	}

	@Operation(description = "Creates a case in ByggR or Ecos2 based on caseType. Also persists a connection between externalCaseId and the created case.")
	@PostMapping(path = "cases", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "200", description = "OK")
	public ResponseEntity<CaseResourceResponseDTO> postCases(
		@Schema(oneOf = {ByggRCaseDTO.class, EcosCaseDTO.class, OtherCaseDTO.class}, example = Constants.POST_CASES_REQUEST_BODY_EXAMPLE)
		@RequestBody
		@Valid CaseDTO caseDTOInput) {

		// Validates that it doesn't exist any case with the same oep-ID.
		caseMappingService.validateUniqueCase(caseDTOInput.getExternalCaseId());
		caseService.handleCase(caseDTOInput);

		return ResponseEntity.ok(new CaseResourceResponseDTO("Inskickat"));

	}

	@Operation(description = "Update a case. Only available for cases created in CaseData.")
	@PutMapping(path = "cases/{externalCaseId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
	@ApiResponse(responseCode = "204", description = "No content")
	public ResponseEntity<Void> putCase(
		@PathVariable String externalCaseId,
		@Schema(oneOf = {ByggRCaseDTO.class, EcosCaseDTO.class, OtherCaseDTO.class}, example = Constants.POST_CASES_REQUEST_BODY_EXAMPLE)
		@RequestBody
		@Valid CaseDTO caseDTOInput) {

		if (caseDTOInput instanceof ByggRCaseDTO byggRCaseDTO) {
			if (byggRCaseDTO.getCaseType().equalsIgnoreCase("NEIGHBORHOOD_NOTIFICATION")) {
				byggrService.updateByggRCase(byggRCaseDTO);
				return ResponseEntity.noContent().build();
			} else {
				throw Problem.valueOf(Status.BAD_REQUEST, "Only ByggR cases of type NEIGHBORHOOD_NOTIFICATION can be updated.");
			}
		} else if (caseDTOInput instanceof OtherCaseDTO otherCaseDTO) {
			caseDataService.putErrand(Long.valueOf(caseMappingService.getCaseMapping(externalCaseId).getCaseId()), otherCaseDTO);
			return ResponseEntity.noContent().build();
		} else {
			throw Problem.valueOf(Status.BAD_REQUEST, "Only cases created in CaseData can be updated.");
		}
	}
}
