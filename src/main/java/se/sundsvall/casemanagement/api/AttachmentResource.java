package se.sundsvall.casemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.ByggrService;
import se.sundsvall.casemanagement.service.CaseDataService;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.EcosService;
import se.sundsvall.casemanagement.service.exceptions.ApplicationException;
import se.sundsvall.casemanagement.service.util.Constants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

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

    public AttachmentResource(ByggrService byggrService, EcosService ecosService, CaseDataService caseDataService, CaseMappingService caseMappingService) {
        this.byggrService = byggrService;
        this.ecosService = ecosService;
        this.caseDataService = caseDataService;
        this.caseMappingService = caseMappingService;
    }

    @Operation(description = "Add attachments to existing case.")
    @PostMapping(path = "cases/{externalCaseId}/attachments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
    @ApiResponse(responseCode = "204", description = "No content - Successful request.")
    public ResponseEntity<Void> postAttachmentsToCase(@PathVariable String externalCaseId,
                                                @NotNull(message = Constants.REQUEST_BODY_MUST_NOT_BE_NULL)
                                          @RequestBody @Valid List<AttachmentDTO> attachmentDTOList) throws ApplicationException {

        CaseMapping caseMapping = caseMappingService.getCaseMapping(externalCaseId);

        switch (caseMapping.getSystem()) {
            case BYGGR -> byggrService.saveNewIncomingAttachmentHandelse(caseMapping.getCaseId(), attachmentDTOList);
            case ECOS -> ecosService.addDocumentsToCase(caseMapping.getCaseId(), attachmentDTOList);
            case CASE_DATA -> caseDataService.patchErrandWithAttachment(Long.valueOf(caseMapping.getCaseId()), attachmentDTOList);
            default ->
                    throw new ApplicationException("It should not be possible to reach this row. systemType was: " + caseMapping.getSystem());
        }

        return ResponseEntity.noContent().build();
    }
}