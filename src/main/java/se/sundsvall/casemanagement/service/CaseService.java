package se.sundsvall.casemanagement.service;

import static se.sundsvall.casemanagement.service.mapper.CaseMapper.toCaseEntity;

import java.util.Optional;

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
import se.sundsvall.casemanagement.service.util.Validator;

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

	public void handleCase(final CaseDTO dto) {

		if (dto instanceof final ByggRCaseDTO byggRCase) {
			validator.validateByggrErrand(byggRCase);

			var oepAction = Optional.ofNullable(byggRCase.getExtraParameters())
				.map(extraParameter -> extraParameter.get("oepAction"))
				.orElse(null);
			// Open-E cannot send any other requests than POST. This is a dirty workaround.
			if ("PUT".equalsIgnoreCase(oepAction)) {
				byggrService.putByggRCase(byggRCase);
				return;
			}

			saveCase(byggRCase);
			handleByggRCase(byggRCase);
		} else if (dto instanceof final EcosCaseDTO ecosCase) {
			validator.validateEcosErrand(ecosCase);
			saveCase(ecosCase);
			handleEcosCase(ecosCase);
		} else if (dto instanceof final OtherCaseDTO otherCase) {
			saveCase(otherCase);
			handleOtherCase(otherCase);
		}
	}

	private void saveCase(final CaseDTO dto) {
		caseRepository.save(toCaseEntity(dto));
	}

	private void handleByggRCase(final ByggRCaseDTO byggRCaseDTO) {
		eventPublisher.publishEvent(new IncomingByggrCase(this, byggRCaseDTO));
	}

	private void handleEcosCase(final EcosCaseDTO ecosCaseDTO) {
		eventPublisher.publishEvent(new IncomingEcosCase(this, ecosCaseDTO));
	}

	private void handleOtherCase(final OtherCaseDTO otherCaseDTO) {
		eventPublisher.publishEvent(new IncomingOtherCase(this, otherCaseDTO));
	}
}
