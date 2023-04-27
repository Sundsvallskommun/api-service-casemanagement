package se.sundsvall.casemanagement.service.util;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;

import java.text.MessageFormat;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;

import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
import se.sundsvall.casemanagement.api.model.PlanningPermissionFacilityDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validators.EnvironmentalConstraints;
import se.sundsvall.casemanagement.api.validators.PersonConstraints;
import se.sundsvall.casemanagement.api.validators.PlanningConstraints;

@Component
public class Validator {
    
    public void validateByggrErrand(PlanningPermissionCaseDTO pCase) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            javax.validation.Validator validator = factory.getValidator();
            
            Set<ConstraintViolation<PlanningPermissionCaseDTO>> caseViolations = validator.validate(pCase, PlanningConstraints.class);
            
            if (!caseViolations.isEmpty()) {
                throw new ConstraintViolationException(caseViolations);
            }
            
            // Validation for person. This is necessary because the role CONTROL_OFFICIAL doesn't have the same validation
            // as the other roles.
            for (StakeholderDTO stakeholderDTO : pCase.getStakeholders()) {
                if (stakeholderDTO instanceof PersonDTO personDTO
                    && !stakeholderDTO.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL)) {
                    Set<ConstraintViolation<PersonDTO>> personViolations = validator.validate(personDTO, PersonConstraints.class);
                    
                    if (!personViolations.isEmpty()) {
                        throw new ConstraintViolationException(personViolations);
                    }
                }
            }
            
            // Validates that FacilityTypes is compatible with the CaseType
            validateFacilityTypes(pCase);
        }
    }
    
    /**
     * Validates that the FacilityTypes are compatible with the CaseType.
     */
    private void validateFacilityTypes(PlanningPermissionCaseDTO pCase) {
        boolean anmmalanAttefallFacilityType = false;
        FacilityType facilityType = null;
        
        for (PlanningPermissionFacilityDTO facility : pCase.getFacilities()) {
            
            facilityType = facility.getFacilityType();
            
            if (facilityType == null && CaseType.caseTypesWithNullableFacilityType().contains(pCase.getCaseType())) {
                return;
            } else if (facilityType == null) {
                throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType is not allowed to be null for CaseType {0}", pCase.getCaseType()));
            }
            anmmalanAttefallFacilityType = switch (facility.getFacilityType()) {
                case FURNISHING_OF_ADDITIONAL_DWELLING, ANCILLARY_BUILDING, ANCILLARY_HOUSING_BUILDING, DORMER, EXTENSION ->
                    true;
                default -> false;
            };
        }
        
        if ((pCase.getCaseType() == ANMALAN_ATTEFALL && !anmmalanAttefallFacilityType) || (pCase.getCaseType() == NYBYGGNAD_ANSOKAN_OM_BYGGLOV && anmmalanAttefallFacilityType)) {
            throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType {0} is not compatible with CaseType {1}", facilityType, pCase.getCaseType()));
        }
    }
    
    
    public void validateEcosErrand(EnvironmentalCaseDTO eCase) {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            javax.validation.Validator validator = factory.getValidator();
            
            if (eCase.getCaseType().equals(CaseType.UPPDATERING_RISKKLASSNING)) {
                return;
            }
            Set<ConstraintViolation<EnvironmentalCaseDTO>> violations = validator.validate(eCase, EnvironmentalConstraints.class);
            
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        }
    }
}
