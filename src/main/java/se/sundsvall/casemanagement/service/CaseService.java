package se.sundsvall.casemanagement.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.integration.byggr.ByggrService;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;
import se.sundsvall.casemanagement.service.event.IncomingEcosCase;
import se.sundsvall.casemanagement.service.event.IncomingOtherCase;
import se.sundsvall.casemanagement.service.event.UpdateByggrCase;
import se.sundsvall.casemanagement.service.util.Validator;

import java.util.Optional;

import static se.sundsvall.casemanagement.service.mapper.CaseMapper.toCaseEntity;

@Service
public class CaseService {

	private final ApplicationEventPublisher eventPublisher;

	private final CaseRepository caseRepository;

	private final Validator validator;

	private final ByggrService byggrService;

	public CaseService(final ApplicationEventPublisher eventPublisher,
		final CaseRepository caseRepository, final Validator validator,
		final ByggrService byggrService) {
		this.eventPublisher = eventPublisher;
		this.caseRepository = caseRepository;
		this.validator = validator;
		this.byggrService = byggrService;
	}

	public void handleCase(final CaseDTO dto, final String municipalityId) {

		if (dto instanceof final ByggRCaseDTO byggRCase) {
			validator.validateByggrErrand(byggRCase);

			// Open-E cannot send any other requests than POST. This is a dirty workaround.
			Optional.ofNullable(byggRCase.getExtraParameters())
				.map(extraParameter -> extraParameter.get("oepAction"))
				.filter("PUT"::equalsIgnoreCase)
				.ifPresentOrElse(action -> handleUpdateByggRCase(byggRCase, municipalityId), () -> {
					saveCase(byggRCase, municipalityId);
					handleByggRCase(byggRCase, municipalityId);
				});
		} else if (dto instanceof final EcosCaseDTO ecosCase) {
			validator.validateEcosErrand(ecosCase);
			saveCase(ecosCase, municipalityId);
			handleEcosCase(ecosCase, municipalityId);
		} else if (dto instanceof final OtherCaseDTO otherCase) {
			saveCase(otherCase, municipalityId);
			handleOtherCase(otherCase, municipalityId);
		}
	}

	private void saveCase(final CaseDTO dto, final String municipalityId) {
		caseRepository.save(toCaseEntity(dto, municipalityId));
	}

	private void handleUpdateByggRCase(final ByggRCaseDTO byggRCaseDTO, final String municipalityId) {
		eventPublisher.publishEvent(new UpdateByggrCase(this, byggRCaseDTO, municipalityId));
	}

	private void handleByggRCase(final ByggRCaseDTO byggRCaseDTO, final String municipalityId) {
		eventPublisher.publishEvent(new IncomingByggrCase(this, byggRCaseDTO, municipalityId));
	}

	private void handleEcosCase(final EcosCaseDTO ecosCaseDTO, final String municipalityId) {
		eventPublisher.publishEvent(new IncomingEcosCase(this, ecosCaseDTO, municipalityId));
	}

	private void handleOtherCase(final OtherCaseDTO otherCaseDTO, final String municipalityId) {
		eventPublisher.publishEvent(new IncomingOtherCase(this, otherCaseDTO, municipalityId));
	}

}
