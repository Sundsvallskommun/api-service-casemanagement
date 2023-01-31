package se.sundsvall.casemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.exceptions.ApplicationException;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@RestController
@Validated
@RequestMapping("/cases/case-mappings")
@Tag(name = "CaseMappings", description = "CaseMapping operations")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "404", description = "Not found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
public class CaseMappingResource {

    private final CaseMappingService caseMappingService;

    public CaseMappingResource(CaseMappingService caseMappingService) {
        this.caseMappingService = caseMappingService;
    }

    @GetMapping(produces = {APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @Operation(description = "Returns the connection between externalCaseId and the case in the underlying system.")
    @ApiResponse(responseCode = "200", description = "OK - Successful operation")
    public ResponseEntity<List<CaseMapping>> getCaseMapping(@RequestParam(name = "external-case-id", required = false) String externalCaseId) throws ApplicationException {
        if (externalCaseId != null) {
            return ResponseEntity.ok(List.of(caseMappingService.getCaseMapping(externalCaseId)));
        } else {
            return ResponseEntity.ok(caseMappingService.getAllCaseMappings());
        }
    }

}
