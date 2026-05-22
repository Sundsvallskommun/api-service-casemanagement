package se.sundsvall.casemanagement.service.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.validation.AttachmentConstraints;
import se.sundsvall.casemanagement.api.validation.ByggRConstraints;
import se.sundsvall.casemanagement.api.validation.ByggRFacilityConstraints;
import se.sundsvall.casemanagement.api.validation.EcosConstraints;
import se.sundsvall.casemanagement.api.validation.PersonConstraints;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.EcosCaseTypeConfigRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeEntity;
import se.sundsvall.casemanagement.integration.db.model.EcosFacilityHandler;
import se.sundsvall.casemanagement.service.CaseTypeRegistry;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class Validator {

	private final CaseTypeRepository caseTypeRepository;
	private final EcosCaseTypeConfigRepository ecosCaseTypeConfigRepository;
	private final CaseTypeRegistry caseTypeRegistry;

	public Validator(final CaseTypeRepository caseTypeRepository, final EcosCaseTypeConfigRepository ecosCaseTypeConfigRepository, final CaseTypeRegistry caseTypeRegistry) {
		this.caseTypeRepository = caseTypeRepository;
		this.ecosCaseTypeConfigRepository = ecosCaseTypeConfigRepository;
		this.caseTypeRegistry = caseTypeRegistry;
	}

	public void validateByggrErrand(final ByggRCaseDTO byggRCase) {
		final CaseTypeEntity caseTypeEntity = caseTypeRepository.findById(byggRCase.getCaseType())
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, MessageFormat.format("CaseType {0} not found", byggRCase.getCaseType())));

		try (final var factory = Validation.buildDefaultValidatorFactory()) {
			final jakarta.validation.Validator validator = factory.getValidator();

			if (!caseTypeEntity.isNullableFacility()) {
				final Set<ConstraintViolation<ByggRCaseDTO>> facilityViolations = validator.validate(byggRCase, ByggRFacilityConstraints.class);
				if (!facilityViolations.isEmpty()) {
					throw new ConstraintViolationException(facilityViolations);
				}
			}

			final Set<ConstraintViolation<ByggRCaseDTO>> caseViolations = validator.validate(byggRCase, ByggRConstraints.class);

			if (!caseViolations.isEmpty()) {
				throw new ConstraintViolationException(caseViolations);
			}

			final Set<ConstraintViolation<ByggRCaseDTO>> attachmentViolations = validator.validate(byggRCase, AttachmentConstraints.class);

			if (!attachmentViolations.isEmpty()) {
				throw new ConstraintViolationException(attachmentViolations);
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
			validateFacilityTypes(byggRCase, caseTypeEntity);
		}
	}

	/**
	 * Validates that the FacilityTypes are compatible with the CaseType.
	 */
	private void validateFacilityTypes(final ByggRCaseDTO byggRCase, final CaseTypeEntity caseType) {
		boolean attefallFacilityType = false;
		String facilityType = null;

		for (final var facility : byggRCase.getFacilities()) {

			facilityType = facility.getFacilityType();

			if ((facilityType == null) && caseType.isNullableFacilityType()) {
				return;
			}
			if (facilityType == null) {
				throw Problem.valueOf(BAD_REQUEST, MessageFormat.format("FacilityType is not allowed to be null for CaseType {0}", byggRCase.getCaseType()));
			}

			attefallFacilityType = switch (FacilityType.valueOf(facilityType)) {
				case FURNISHING_OF_ADDITIONAL_DWELLING, ANCILLARY_BUILDING,
					ANCILLARY_HOUSING_BUILDING, DORMER, EXTENSION -> true;
				default -> false;
			};
		}

		final var rule = caseType.getFacilityTypeRule();
		if (("ATTEFALL_REQUIRED".equals(rule) && !attefallFacilityType) || ("ATTEFALL_REJECTED".equals(rule) && attefallFacilityType)) {
			throw Problem.valueOf(BAD_REQUEST, MessageFormat.format("FacilityType {0} is not compatible with CaseType {1}", facilityType, byggRCase.getCaseType()));
		}
	}

	public void validateCaseDataErrand(final OtherCaseDTO otherCase, final String municipalityId) {
		if (!caseTypeRegistry.isCaseDataType(otherCase.getCaseType(), municipalityId)) {
			throw Problem.valueOf(BAD_REQUEST, MessageFormat.format("CaseType {0} is not a valid CaseData type for municipality {1}", otherCase.getCaseType(), municipalityId));
		}
	}

	public void validateAttachments(final CaseDTO caseDTO) {
		try (final var factory = Validation.buildDefaultValidatorFactory()) {
			final var validator = factory.getValidator();

			final Set<ConstraintViolation<CaseDTO>> violations = validator.validate(caseDTO, AttachmentConstraints.class);

			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}
		}
	}

	public void validateFutureErrand(final FutureCaseDTO futureCase) {
		final var quantity = Optional.ofNullable(futureCase.getExtraParameters())
			.map(p -> p.get("Quantity"))
			.orElse(null);
		if (quantity == null || quantity.isBlank()) {
			throw Problem.valueOf(BAD_REQUEST,
				"extraParameters must contain a non-blank value for key: Quantity");
		}
	}

	public void validateEcosErrand(final EcosCaseDTO eCase) {
		final CaseTypeEntity caseTypeEntity = caseTypeRepository.findById(eCase.getCaseType())
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, MessageFormat.format("CaseType {0} not found", eCase.getCaseType())));

		final var ecosConfig = ecosCaseTypeConfigRepository.findById(eCase.getCaseType());

		if (ecosConfig.isPresent() && ecosConfig.get().getFacilityHandler() == EcosFacilityHandler.RISK_CLASS_UPDATE) {
			return;
		}

		try (final var factory = Validation.buildDefaultValidatorFactory()) {
			final var validator = factory.getValidator();

			final Set<ConstraintViolation<EcosCaseDTO>> violations = validator.validate(eCase, EcosConstraints.class);

			if (!violations.isEmpty()) {
				throw new ConstraintViolationException(violations);
			}

			final Set<ConstraintViolation<EcosCaseDTO>> attachmentViolations = validator.validate(eCase, AttachmentConstraints.class);

			if (!attachmentViolations.isEmpty()) {
				throw new ConstraintViolationException(attachmentViolations);
			}

			for (final var facilityDTO : eCase.getFacilities()) {
				if ((facilityDTO.getFacilityCollectionName() == null) && !caseTypeEntity.isNullableFacilityType()) {
					throw Problem.valueOf(BAD_REQUEST, MessageFormat.format("FacilityType is not allowed to be null for CaseType {0}", eCase.getCaseType()));
				}
			}
		}
	}

}
