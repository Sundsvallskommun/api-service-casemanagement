package se.sundsvall.casemanagement.integration.casedata;

import static generated.client.casedata.Errand.ChannelEnum.ESERVICE;
import static generated.client.casedata.Stakeholder.TypeEnum.PERSON;
import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.MEX_CASE_TYPES;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.PRH_CASE_TYPES;
import static se.sundsvall.casemanagement.api.model.enums.Namespace.ANGE_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.Namespace.SBK_MEX;
import static se.sundsvall.casemanagement.api.model.enums.Namespace.SBK_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.CASE_DATA;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toAttachment;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toErrand;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toPatchErrand;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toStakeholders;
import static se.sundsvall.casemanagement.util.Constants.SERVICE_NAME;

import generated.client.casedata.Errand;
import generated.client.casedata.ExtraParameter;
import generated.client.casedata.Stakeholder;
import generated.client.casedata.Status;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class CaseDataService {

	private static final String NO_CASE_WAS_FOUND_IN_CASE_DATA_WITH_CASE_ID = "No case was found in CaseData with caseId: ";

	private static final String ARENDE_INKOMMIT_STATUS = "Ärende inkommit";

	private static final String KOMPLETTERING_INKOMMEN_STATUS = "Komplettering inkommen";

	private static final String AKTUALISERING_PHASE = "Aktualisering";

	private static final String KEY_PHASE_ACTION = "process.phaseAction";

	private static final String PHASE_ACTION_AUTOMATIC = "AUTOMATIC";

	private static final String MUNICIPALITY_ID_ANGE = "2260";

	private static final String PROCESS_ENGINE_FIRST_NAME = "Process";

	private static final String PROCESS_ENGINE_LAST_NAME = "Engine";

	private static final String ROLE_ADMINISTRATOR = "ADMINISTRATOR";

	private final CaseMappingService caseMappingService;
	private final CaseDataProperties caseDataProperties;
	private final CaseDataClient caseDataClient;

	public CaseDataService(final CaseMappingService caseMappingService,
		final CaseDataProperties caseDataProperties,
		final CaseDataClient caseDataClient) {
		this.caseMappingService = caseMappingService;
		this.caseDataProperties = caseDataProperties;
		this.caseDataClient = caseDataClient;
	}

	/**
	 * @param  otherCase      The case to be created in CaseData
	 * @param  municipalityId The municipalityId to be used in CaseData
	 * @return                errandNumber (example: PRH-2022-000001)
	 */
	public String postErrand(final OtherCaseDTO otherCase, final String municipalityId) {
		final var errandDTO = toErrand(otherCase);
		errandDTO.setPhase(AKTUALISERING_PHASE);
		final var statusDTO = new Status();
		statusDTO.setStatusType(ARENDE_INKOMMIT_STATUS);
		statusDTO.setCreated(now());
		errandDTO.setStatus(statusDTO);

		final var namespace = mapNamespace(otherCase.getCaseType(), municipalityId);

		if (isAutomatic(errandDTO, municipalityId, namespace)) {
			errandDTO.setExtraParameters(addPhaseAction(Optional.ofNullable(errandDTO.getExtraParameters()).orElse(emptyList())));
			errandDTO.setStakeholders(addAdministratorStakeholder(Optional.ofNullable(errandDTO.getStakeholders()).orElse(emptyList()), municipalityId, namespace));
		}

		// To keep collection instantiated and not suddenly
		// changed to null if openAPI decides to change the implementation. (again)
		errandDTO.setMessageIds(emptyList());
		errandDTO.setDecisions(emptyList());
		errandDTO.setNotes(emptyList());

		final var result = caseDataClient.postErrands(municipalityId, namespace, errandDTO);
		final var location = String.valueOf(result.getHeaders().getFirst(HttpHeaders.LOCATION));
		final var id = Long.valueOf(location.substring(location.lastIndexOf("/") + 1));

		final var errandNumber = getErrand(id, municipalityId, namespace).getErrandNumber();

		if (errandNumber != null) {
			otherCase.getAttachments().stream().map(
				attachment -> toAttachment(attachment, id))
				.forEach(attachmentDTO -> caseDataClient.postAttachment(municipalityId, namespace, id, attachmentDTO));
		}
		caseMappingService.postCaseMapping(otherCase, String.valueOf(id), CASE_DATA, municipalityId);

		return errandNumber;
	}

	public void patchErrandWithAttachment(final CaseMapping caseMapping, final List<AttachmentDTO> attachmentDTOS, final String municipalityId) {

		final var errandId = Long.valueOf(caseMapping.getCaseId());
		final var namespace = mapNamespace(caseMapping.getCaseType(), municipalityId);
		try {
			attachmentDTOS.forEach(attachment -> caseDataClient.postAttachment(municipalityId, namespace, errandId, toAttachment(attachment, errandId)));
		} catch (final ThrowableProblem e) {
			if (Objects.equals(e.getStatus(), NOT_FOUND)) {
				throw Problem.valueOf(NOT_FOUND, NO_CASE_WAS_FOUND_IN_CASE_DATA_WITH_CASE_ID + errandId);
			} else {
				throw e;
			}
		}
	}

	private Errand getErrand(final Long id, final String municipalityId, final String namespace) {
		try {
			return caseDataClient.getErrand(municipalityId, namespace, id);
		} catch (final ThrowableProblem e) {
			if (Objects.equals(e.getStatus(), NOT_FOUND)) {
				throw Problem.valueOf(NOT_FOUND, NO_CASE_WAS_FOUND_IN_CASE_DATA_WITH_CASE_ID + id);
			} else {
				throw e;
			}
		}
	}

	public CaseStatusDTO getStatus(final CaseMapping caseMapping, final String municipalityId) {
		final var namespace = mapNamespace(caseMapping.getCaseType(), municipalityId);

		final var errandDTO = getErrand(Long.valueOf(caseMapping.getCaseId()), municipalityId, namespace);

		final var latestStatus = Optional.ofNullable(Optional.ofNullable(errandDTO)
			.orElse(new Errand())
			.getStatuses())
			.orElse(List.of())
			.stream()
			.max(Comparator.comparing(Status::getCreated))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND));

		return CaseStatusDTO.builder()
			.withCaseType(caseMapping.getCaseType())
			.withSystem(caseMapping.getSystem())
			.withCaseId(caseMapping.getCaseId())
			.withExternalCaseId(caseMapping.getExternalCaseId())
			.withStatus(latestStatus.getStatusType())
			.withServiceName(caseMapping.getServiceName())
			.withTimestamp(latestStatus.getCreated().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
			.build();
	}

	/**
	 * This is unfortunately not a complete PUT-operation because of restrictions in OpenE. They need to send the complete
	 * object every time. And because we don't want to write over some data, like decisions, in CaseData, we need to do the
	 * update like
	 * this.
	 * <p>
	 * This method will first do a patch on all ErrandDTO-fields. After that it will do PUT on Statuses, Stakeholders and
	 * Attachments.
	 *
	 * @param caseId       The ID from CaseData.
	 * @param otherCaseDTO The updated case from OpenE.
	 */
	public void putErrand(final Long caseId, final OtherCaseDTO otherCaseDTO, final String municipalityId) {
		final var namespace = mapNamespace(otherCaseDTO.getCaseType(), municipalityId);

		caseDataClient.patchErrand(municipalityId, namespace, caseId, toPatchErrand(otherCaseDTO));

		final var statusDTO = new Status();
		statusDTO.setStatusType(KOMPLETTERING_INKOMMEN_STATUS);
		statusDTO.setCreated(now());
		caseDataClient.patchStatusOnErrand(municipalityId, namespace, caseId, statusDTO);
		caseDataClient.putStakeholdersOnErrand(municipalityId, namespace, caseId, toStakeholders(otherCaseDTO.getStakeholders()));

		final var result = caseDataClient.getAttachmentsByErrandNumber(municipalityId, namespace, otherCaseDTO.getExternalCaseId());
		if (result != null) {
			result.forEach(attachment -> caseDataClient.deleteAttachment(municipalityId, namespace, caseId, attachment.getId()));
		}
		otherCaseDTO.getAttachments().stream().map(attachment -> toAttachment(attachment, caseId))
			.forEach(attachmentDTO -> caseDataClient.postAttachment(municipalityId, namespace, caseId, attachmentDTO));
	}

	public String mapNamespace(final String caseType, final String municipalityId) {
		if (caseType == null) {
			return "OTHER";
		}
		final var enumValue = CaseType.valueOf(caseType);
		if (MEX_CASE_TYPES.contains(enumValue)) {
			return SBK_MEX.name();
		}
		if (PRH_CASE_TYPES.contains(enumValue) && !MUNICIPALITY_ID_ANGE.equals(municipalityId)) {
			return SBK_PARKING_PERMIT.name();
		}

		if ((PARKING_PERMIT.equals(enumValue) ||
			PARKING_PERMIT_RENEWAL.equals(enumValue) ||
			LOST_PARKING_PERMIT.equals(enumValue)) && MUNICIPALITY_ID_ANGE.equals(municipalityId)) {
			return ANGE_PARKING_PERMIT.name();
		}

		return "OTHER";
	}

	public List<Errand> getErrands(final String municipalityId, final String namespace, final String filter) {
		final var page = caseDataClient.getErrands(municipalityId, namespace, filter, "1000");
		return page.getContent();
	}

	public List<CaseStatusDTO> getStatusesByFilter(final String filter, final String municipalityId) {
		final List<CaseStatusDTO> caseStatuses = new ArrayList<>();
		for (final var namespace : caseDataProperties.namespaces().get(municipalityId)) {
			final var errands = getErrands(municipalityId, namespace, filter);
			errands.forEach(errand -> caseStatuses.add(toCaseStatusDTO(errand)));
		}
		return caseStatuses;
	}

	CaseStatusDTO toCaseStatusDTO(final Errand errand) {
		final var latestStatus = errand.getStatuses().stream()
			.max(Comparator.comparing(Status::getCreated))
			.orElse(null);

		return CaseStatusDTO.builder()
			.withSystem(CASE_DATA)
			.withExternalCaseId(errand.getExternalCaseId())
			.withCaseId(String.valueOf(errand.getId()))
			.withCaseType(errand.getCaseType())
			.withStatus(Optional.ofNullable(latestStatus)
				.map(Status::getStatusType)
				.orElse(null))
			.withTimestamp(Optional.ofNullable(latestStatus)
				.map(Status::getCreated)
				.map(dateTime -> dateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
				.orElse(null))
			.withServiceName(errand.getExtraParameters().stream()
				.filter(extraParameter -> SERVICE_NAME.equals(extraParameter.getKey()))
				.findFirst()
				.map(ExtraParameter::getValues)
				.map(List::getFirst)
				.orElse(null))
			.withErrandNumber(errand.getErrandNumber())
			.withNamespace(errand.getNamespace())
			.build();
	}

	private boolean isAutomatic(final Errand errand, final String municipalityId, final String namespace) {
		final var caseType = CaseType.valueOf(errand.getCaseType());
		return (PARKING_PERMIT.equals(caseType) ||
			PARKING_PERMIT_RENEWAL.equals(caseType) ||
			LOST_PARKING_PERMIT.equals(caseType)) &&
			ANGE_PARKING_PERMIT.name().equals(namespace) &&
			MUNICIPALITY_ID_ANGE.equals(municipalityId) &&
			ESERVICE.equals(errand.getChannel());
	}

	private List<ExtraParameter> addPhaseAction(final List<ExtraParameter> extraParameters) {
		final var newList = new ArrayList<>(extraParameters);
		newList.removeIf(extraParameter -> KEY_PHASE_ACTION.equals(extraParameter.getKey()));
		newList.add(new ExtraParameter(KEY_PHASE_ACTION).values(List.of(PHASE_ACTION_AUTOMATIC)));
		return newList;
	}

	private List<Stakeholder> addAdministratorStakeholder(final List<Stakeholder> stakeholders, final String municipalityId, final String namespace) {
		final var newList = new ArrayList<>(stakeholders);
		newList.add(new Stakeholder()
			.municipalityId(municipalityId)
			.namespace(namespace)
			.type(PERSON)
			.firstName(PROCESS_ENGINE_FIRST_NAME)
			.lastName(PROCESS_ENGINE_LAST_NAME)
			.roles(List.of(ROLE_ADMINISTRATOR)));
		return newList;
	}
}
