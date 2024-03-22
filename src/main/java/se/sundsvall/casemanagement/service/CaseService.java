package se.sundsvall.casemanagement.service;

import static se.sundsvall.casemanagement.service.mapper.CaseMapper.toCaseEntity;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
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

	public CaseService(ApplicationEventPublisher eventPublisher, CaseRepository caseRepository, Validator validator) {
		this.eventPublisher = eventPublisher;
		this.caseRepository = caseRepository;
		this.validator = validator;
	}

	public void handleCase(CaseDTO dto) {

		if (dto instanceof final ByggRCaseDTO pCase) {
			validator.validateByggrErrand(pCase);
			saveCase(pCase);
			handleByggRCase(pCase);
		} else if (dto instanceof final EcosCaseDTO eCase) {
			validator.validateEcosErrand(eCase);
			saveCase(eCase);
			handleEcosCase(eCase);
		} else if (dto instanceof final OtherCaseDTO otherCase) {
			saveCase(otherCase);
			handleOtherCase(otherCase);
		}
	}

	private void saveCase(CaseDTO dto) {
		caseRepository.save(toCaseEntity(dto));

	}

	private void handleByggRCase(final ByggRCaseDTO pCase) {
		eventPublisher.publishEvent(new IncomingByggrCase(this, pCase));
	}

	private void handleEcosCase(final EcosCaseDTO eCase) {
		eventPublisher.publishEvent(new IncomingEcosCase(this, eCase));
	}

	private void handleOtherCase(OtherCaseDTO dto) {
		eventPublisher.publishEvent(new IncomingOtherCase(this, dto));
	}
}
