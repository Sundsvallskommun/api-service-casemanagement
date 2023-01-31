package se.sundsvall.casemanagement.api;

import arendeexport.SaveNewArendeResponse2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseResourceResponseDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.service.ByggrService;
import se.sundsvall.casemanagement.service.CaseDataService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.EcosService;
import se.sundsvall.casemanagement.service.exceptions.ApplicationException;
import se.sundsvall.casemanagement.service.util.Constants;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@Validated
@RequestMapping("/")
@Tag(name = "Cases", description = "Cases operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "502", description = "Bad Gateway", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class CaseResource {

    private static final Logger log = LoggerFactory.getLogger(CaseResource.class);

    private final CaseMappingService caseMappingService;

    private final ByggrService byggrService;

    private final EcosService ecosService;

    private final CaseDataService caseDataService;

    public CaseResource(CaseMappingService caseMappingService, ByggrService byggrService, EcosService ecosService, CaseDataService caseDataService) {
        this.caseMappingService = caseMappingService;
        this.byggrService = byggrService;
        this.ecosService = ecosService;
        this.caseDataService = caseDataService;
    }

    @Operation(description = "Creates a case in ByggR or Ecos2 based on caseType. Also persists a connection between externalCaseId and the created case.")
    @PostMapping(path = "cases", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "200", description = "OK")
    public ResponseEntity<CaseResourceResponseDTO> postCases(
            @Schema(oneOf = {PlanningPermissionCaseDTO.class, EnvironmentalCaseDTO.class, OtherCaseDTO.class}, example = Constants.POST_CASES_REQUEST_BODY_EXAMPLE)
            @RequestBody
            @Valid CaseDTO caseDTOInput) throws ApplicationException {

        // Validates that it doesn't exist any case with the same oep-ID.
        caseMappingService.validateUniqueCase(caseDTOInput.getExternalCaseId());

        if (caseDTOInput instanceof PlanningPermissionCaseDTO pCase) {
            log.debug("instance of PlanningPermissionCase");
            SaveNewArendeResponse2 response = byggrService.postCase(pCase);
            return ResponseEntity.ok(new CaseResourceResponseDTO(response.getDnr()));
        } else if (caseDTOInput instanceof EnvironmentalCaseDTO eCase) {
            log.debug("instance of EnvironmentalCase");
            RegisterDocumentCaseResultSvcDto registerDocumentResult = ecosService.postCase(eCase);
            return ResponseEntity.ok(new CaseResourceResponseDTO(registerDocumentResult.getCaseNumber()));
        } else if (caseDTOInput instanceof OtherCaseDTO otherCase) {
            log.debug("instance of OtherCase");
            String errandNumber = caseDataService.postErrand(otherCase);
            return ResponseEntity.ok(new CaseResourceResponseDTO(errandNumber));
        }
        return ResponseEntity.internalServerError().build();
    }

    @Operation(description = "Update a case. Only available for cases created in CaseData.")
    @PutMapping(path = "cases/{externalCaseId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content")
    public ResponseEntity<Void> putCase(
            @PathVariable String externalCaseId,
            @Schema(oneOf = {PlanningPermissionCaseDTO.class, EnvironmentalCaseDTO.class, OtherCaseDTO.class}, example = Constants.POST_CASES_REQUEST_BODY_EXAMPLE)
            @RequestBody
            @Valid CaseDTO caseDTOInput) throws ApplicationException {

        if (caseDTOInput instanceof OtherCaseDTO otherCaseDTO) {
            caseDataService.putErrand(Long.valueOf(caseMappingService.getCaseMapping(externalCaseId).getCaseId()), otherCaseDTO);
            return ResponseEntity.noContent().build();
        } else {
            throw Problem.valueOf(Status.BAD_REQUEST, "Only cases created in CaseData can be updated.");
        }
    }
}
