package se.sundsvall.casemanagement.service.util;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.WITH_NULLABLE_FACILITY_TYPE;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;

import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validation.EnvironmentalConstraints;
import se.sundsvall.casemanagement.api.validation.PersonConstraints;
import se.sundsvall.casemanagement.api.validation.PlanningConstraints;

@Component
public class Validator {

	public void validateByggrErrand(ByggRCaseDTO pCase) {
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			final jakarta.validation.Validator validator = factory.getValidator();

			final Set<ConstraintViolation<ByggRCaseDTO>> caseViolations = validator.validate(pCase, PlanningConstraints.class);

			if (!caseViolations.isEmpty()) {
				throw new ConstraintViolationException(caseViolations);
			}

			// Validation for person. This is necessary because the role CONTROL_OFFICIAL doesn't have the same validation
			// as the other roles.
			for (final StakeholderDTO stakeholderDTO : pCase.getStakeholders()) {
				if (stakeholderDTO instanceof final PersonDTO personDTO
					&& !stakeholderDTO.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString())) {
					final Set<ConstraintViolation<PersonDTO>> personViolations = validator.validate(personDTO, PersonConstraints.class);

					if (!personViolations.isEmpty()) {
						throw new ConstraintViolationException(personViolations);
					}
				}
			}
		}
		// Validates that FacilityTypes is compatible with the CaseType
		validateFacilityTypes(pCase);
	}

	/**
	 * Validates that the FacilityTypes are compatible with the CaseType.
	 */
	private void validateFacilityTypes(ByggRCaseDTO pCase) {
		boolean attefallFacilityType = false;
		String facilityType = null;

		for (final var facility : pCase.getFacilities()) {

			facilityType = facility.getFacilityType();

			if ((facilityType == null) && WITH_NULLABLE_FACILITY_TYPE.contains(pCase.getCaseType())) {
				return;
			}
			if (facilityType == null) {
				throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType is not allowed to be null for CaseType {0}", pCase.getCaseType()));
			}

			attefallFacilityType = switch (FacilityType.valueOf(facilityType)) {
				case FURNISHING_OF_ADDITIONAL_DWELLING, ANCILLARY_BUILDING, ANCILLARY_HOUSING_BUILDING, DORMER, EXTENSION ->
					true;
				default -> false;
			};
		}

		if (((Objects.equals(pCase.getCaseType(), ANMALAN_ATTEFALL.toString())) && !attefallFacilityType) || ((Objects.equals(pCase.getCaseType(), NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString())) && attefallFacilityType)) {
			throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType {0} is not compatible with CaseType {1}", facilityType, pCase.getCaseType()));
		}
	}

	public void validateEcosErrand(EcosCaseDTO eCase) {
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			final var validator = factory.getValidator();

			if (CaseType.UPPDATERING_RISKKLASSNING.toString().equals(eCase.getCaseType())) {
				return;
			}

			final Set<ConstraintViolation<EcosCaseDTO>> violations = validator.validate(eCase, EnvironmentalConstraints.class);

			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}

			for (final var facilityDTO : eCase.getFacilities()) {
				if ((facilityDTO.getFacilityCollectionName() == null) && !WITH_NULLABLE_FACILITY_TYPE.contains(eCase.getCaseType())) {
					throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType is not allowed to be null for CaseType {0}", eCase.getCaseType()));
				}
			}
		}
	}

}
