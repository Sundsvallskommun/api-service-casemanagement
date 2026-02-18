package se.sundsvall.casemanagement.service.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validation.ByggRConstraints;
import se.sundsvall.casemanagement.api.validation.ByggRFacilityConstraints;
import se.sundsvall.casemanagement.api.validation.EcosConstraints;
import se.sundsvall.casemanagement.api.validation.PersonConstraints;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.BYGGR_ADDITIONAL_DOCUMENTS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.BYGGR_ADD_CERTIFIED_INSPECTOR;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NEIGHBORHOOD_NOTIFICATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.WITH_NULLABLE_FACILITY_TYPE;

@Component
public class Validator {

	private static final Set<String> NULLABLE_FACILITY_CASE_TYPES = Set.of(NEIGHBORHOOD_NOTIFICATION, BYGGR_ADD_CERTIFIED_INSPECTOR, BYGGR_ADDITIONAL_DOCUMENTS);

	public void validateByggrErrand(ByggRCaseDTO byggRCase) {
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			final jakarta.validation.Validator validator = factory.getValidator();

			if (!NULLABLE_FACILITY_CASE_TYPES.contains(byggRCase.getCaseType())) {
				final Set<ConstraintViolation<ByggRCaseDTO>> facilityViolations = validator.validate(byggRCase, ByggRFacilityConstraints.class);
				if (!facilityViolations.isEmpty()) {
					throw new ConstraintViolationException(facilityViolations);
				}
			}

			final Set<ConstraintViolation<ByggRCaseDTO>> caseViolations = validator.validate(byggRCase, ByggRConstraints.class);

			if (!caseViolations.isEmpty()) {
				throw new ConstraintViolationException(caseViolations);
			}

			// Validation for person. This is necessary because the role CONTROL_OFFICIAL doesn't have the same validation
			// as the other roles.
			for (final StakeholderDTO stakeholderDTO : byggRCase.getStakeholders()) {
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
		if (byggRCase.getFacilities() != null) {
			validateFacilityTypes(byggRCase);
		}
	}

	/**
	 * Validates that the FacilityTypes are compatible with the CaseType.
	 */
	private void validateFacilityTypes(ByggRCaseDTO byggRCase) {
		boolean attefallFacilityType = false;
		String facilityType = null;

		for (final var facility : byggRCase.getFacilities()) {

			facilityType = facility.getFacilityType();

			if ((facilityType == null) && WITH_NULLABLE_FACILITY_TYPE.contains(byggRCase.getCaseType())) {
				return;
			}
			if (facilityType == null) {
				throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType is not allowed to be null for CaseType {0}", byggRCase.getCaseType()));
			}

			attefallFacilityType = switch (FacilityType.valueOf(facilityType)) {
				case FURNISHING_OF_ADDITIONAL_DWELLING, ANCILLARY_BUILDING,
					ANCILLARY_HOUSING_BUILDING, DORMER, EXTENSION -> true;
				default -> false;
			};
		}

		if (((Objects.equals(byggRCase.getCaseType(), ANMALAN_ATTEFALL.toString())) && !attefallFacilityType) || ((Objects.equals(byggRCase.getCaseType(), NYBYGGNAD_ANSOKAN_OM_BYGGLOV.toString())) && attefallFacilityType)) {
			throw Problem.valueOf(Status.BAD_REQUEST, MessageFormat.format("FacilityType {0} is not compatible with CaseType {1}", facilityType, byggRCase.getCaseType()));
		}
	}

	public void validateEcosErrand(EcosCaseDTO eCase) {
		try (var factory = Validation.buildDefaultValidatorFactory()) {
			final var validator = factory.getValidator();

			if (CaseType.UPPDATERING_RISKKLASSNING.toString().equals(eCase.getCaseType())) {
				return;
			}

			final Set<ConstraintViolation<EcosCaseDTO>> violations = validator.validate(eCase, EcosConstraints.class);

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
