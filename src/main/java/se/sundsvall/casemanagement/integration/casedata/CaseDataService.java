package se.sundsvall.casemanagement.integration.casedata;

import static java.util.Objects.isNull;
import static org.zalando.problem.Status.NOT_FOUND;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.CoordinatesDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.util.Constants;

import generated.client.casedata.ContactInformationDTO;
import generated.client.casedata.ErrandDTO;
import generated.client.casedata.PatchErrandDTO;
import generated.client.casedata.StatusDTO;

@Service
public class CaseDataService {

	private static final String NO_CASE_WAS_FOUND_IN_CASE_DATA_WITH_CASE_ID = "No case was found in CaseData with caseId: ";

	private static final String ARENDE_INKOMMIT_STATUS = "Ärende inkommit";

	private static final String KOMPLETTERING_INKOMMEN_STATUS = "Komplettering inkommen";

	private static final String AKTUALISERING_PHASE = "Aktualisering";

	private static final String APPLICATION_PRIORITY_KEY = "application.priority";

	private final CaseMappingService caseMappingService;

	private final CaseDataClient caseDataClient;

	public CaseDataService(final CaseMappingService caseMappingService, final CaseDataClient caseDataClient) {
		this.caseMappingService = caseMappingService;
		this.caseDataClient = caseDataClient;
	}

	/**
	 * @param otherCase The case to be created in CaseData
	 * @return errandNumber (example: PRH-2022-000001)
	 */
	public String postErrand(final OtherCaseDTO otherCase) {
		final var errandDTO = mapToErrandDTO(otherCase);
		errandDTO.setPhase(AKTUALISERING_PHASE);
		final var statusDTO = new StatusDTO();
		statusDTO.setStatusType(ARENDE_INKOMMIT_STATUS);
		statusDTO.setDateTime(OffsetDateTime.now());
		errandDTO.setStatuses(List.of(statusDTO));

		final var result = caseDataClient.postErrands(errandDTO);
		final var location = String.valueOf(result.getHeaders().getFirst(HttpHeaders.LOCATION));
		final var id = Long.valueOf(location.substring(location.lastIndexOf("/") + 1));

		final var errandNumber = getErrand(id).getErrandNumber();

		if (errandNumber != null) {
			otherCase.getAttachments().stream().map(
					attachment -> mapAttachment(attachment, errandNumber))
				.forEach(caseDataClient::postAttachment);
		}
		caseMappingService.postCaseMapping(otherCase, String.valueOf(id), SystemType.CASE_DATA);

		return errandNumber;
	}

	public void patchErrandWithAttachment(final String errandNumber, final List<AttachmentDTO> attachmentDTOS) {
		try {
			attachmentDTOS.forEach(attachment -> caseDataClient.postAttachment(mapAttachment(attachment, errandNumber)));
		} catch (final ThrowableProblem e) {
			if (Objects.equals(e.getStatus(), NOT_FOUND)) {
				throw Problem.valueOf(NOT_FOUND, NO_CASE_WAS_FOUND_IN_CASE_DATA_WITH_CASE_ID + errandNumber);
			} else {
				throw e;
			}
		}
	}

	private ErrandDTO getErrand(final Long id) {
		try {
			return caseDataClient.getErrand(id);
		} catch (final ThrowableProblem e) {
			if (Objects.equals(e.getStatus(), NOT_FOUND)) {
				throw Problem.valueOf(NOT_FOUND, NO_CASE_WAS_FOUND_IN_CASE_DATA_WITH_CASE_ID + id);
			} else {
				throw e;
			}
		}
	}

	public CaseStatusDTO getStatus(final CaseMapping caseMapping) {
		final var errandDTO = getErrand(Long.valueOf(caseMapping.getCaseId()));

		final var latestStatus = Optional.ofNullable(Optional.ofNullable(errandDTO)
				.orElse(new ErrandDTO())
				.getStatuses())
			.orElse(List.of())
			.stream()
			.max(Comparator.comparing(StatusDTO::getDateTime))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND));

		return CaseStatusDTO.builder()
			.withCaseType(caseMapping.getCaseType())
			.withSystem(caseMapping.getSystem())
			.withCaseId(caseMapping.getCaseId())
			.withExternalCaseId(caseMapping.getExternalCaseId())
			.withStatus(latestStatus.getStatusType())
			.withServiceName(caseMapping.getServiceName())
			.withTimestamp(latestStatus.getDateTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
			.build();
	}

	private generated.client.casedata.AttachmentDTO mapAttachment(final AttachmentDTO attachment, final String errandNumber) {
		final generated.client.casedata.AttachmentDTO attachmentDTO = new generated.client.casedata.AttachmentDTO();
		attachmentDTO.setCategory(attachment.getCategory());
		attachmentDTO.setName(attachment.getName());
		attachmentDTO.setExtension(attachment.getExtension());
		attachmentDTO.setMimeType(attachment.getMimeType());
		attachmentDTO.setFile(attachment.getFile());
		attachmentDTO.setExtraParameters(attachment.getExtraParameters());
		attachmentDTO.setErrandNumber(errandNumber);

		return attachmentDTO;
	}

	private ErrandDTO mapToErrandDTO(final OtherCaseDTO otherCase) {
		final ErrandDTO errandDTO = new ErrandDTO();
		errandDTO.setCaseType(otherCase.getCaseType());
		errandDTO.setExternalCaseId(otherCase.getExternalCaseId());
		errandDTO.setDescription(otherCase.getDescription());
		errandDTO.setCaseTitleAddition(otherCase.getCaseTitleAddition());
		errandDTO.setStakeholders(mapStakeholders(otherCase.getStakeholders()));
		errandDTO.setExtraParameters(otherCase.getExtraParameters());

		if (!isNull(otherCase.getExtraParameters())) {
			final String priority = otherCase.getExtraParameters().get(APPLICATION_PRIORITY_KEY);
			if (StringUtils.isNotBlank(priority)) {
				errandDTO.setPriority(ErrandDTO.PriorityEnum.valueOf(priority));
			}
		}

		return errandDTO;
	}

	private PatchErrandDTO mapToPatchErrandDTO(final OtherCaseDTO otherCaseDTO) {
		final PatchErrandDTO patchErrandDTO = new PatchErrandDTO();
		patchErrandDTO.setCaseType(PatchErrandDTO.CaseTypeEnum.fromValue(otherCaseDTO.getCaseType()));
		patchErrandDTO.setExternalCaseId(otherCaseDTO.getExternalCaseId());
		patchErrandDTO.setDescription(otherCaseDTO.getDescription());
		patchErrandDTO.setCaseTitleAddition(otherCaseDTO.getCaseTitleAddition());
		patchErrandDTO.setExtraParameters(otherCaseDTO.getExtraParameters());

		if (!isNull(otherCaseDTO.getExtraParameters())) {
			final String priority = otherCaseDTO.getExtraParameters().get(APPLICATION_PRIORITY_KEY);
			if (!isNull(priority) && !priority.isBlank()) {
				patchErrandDTO.setPriority(PatchErrandDTO.PriorityEnum.valueOf(priority));
			}
		}
		return patchErrandDTO;
	}

	private List<generated.client.casedata.StakeholderDTO> mapStakeholders(final List<StakeholderDTO> stakeholderDTOS) {
		final List<generated.client.casedata.StakeholderDTO> stakeholderDTODTOList = new ArrayList<>();
		stakeholderDTOS.forEach(stakeholder -> {
			final generated.client.casedata.StakeholderDTO stakeholderDTO = new generated.client.casedata.StakeholderDTO();

			stakeholderDTO.setRoles(stakeholder.getRoles());
			stakeholderDTO.setContactInformation(mapContactInformation(stakeholder));
			stakeholderDTO.setAddresses(mapAddresses(stakeholder.getAddresses()));
			stakeholderDTO.setExtraParameters(stakeholder.getExtraParameters());

			if (stakeholder instanceof final PersonDTO personDTO) {

				stakeholderDTO.setType(generated.client.casedata.StakeholderDTO.TypeEnum.PERSON);
				stakeholderDTO.setFirstName(personDTO.getFirstName());
				stakeholderDTO.setLastName(personDTO.getLastName());
				stakeholderDTO.setPersonId(personDTO.getPersonId());

			} else if (stakeholder instanceof final OrganizationDTO organizationDTO) {

				stakeholderDTO.setType(generated.client.casedata.StakeholderDTO.TypeEnum.ORGANIZATION);
				stakeholderDTO.setOrganizationName(organizationDTO.getOrganizationName());
				stakeholderDTO.setOrganizationNumber(organizationDTO.getOrganizationNumber());
				stakeholderDTO.setAuthorizedSignatory(organizationDTO.getAuthorizedSignatory());
			}

			stakeholderDTODTOList.add(stakeholderDTO);
		});

		return stakeholderDTODTOList;
	}

	private List<generated.client.casedata.AddressDTO> mapAddresses(final List<AddressDTO> addressDTOS) {
		final List<generated.client.casedata.AddressDTO> addressDTODTOList = new ArrayList<>();

		if (addressDTOS != null) {
			addressDTOS.forEach(address -> address.getAddressCategories().forEach(addressCategory -> {
				final generated.client.casedata.AddressDTO addressDTO = new generated.client.casedata.AddressDTO();

				addressDTO.setAddressCategory(generated.client.casedata.AddressDTO.AddressCategoryEnum.fromValue(addressCategory.toString()));
				addressDTO.setStreet(address.getStreet());
				addressDTO.setHouseNumber(address.getHouseNumber());
				addressDTO.setPostalCode(address.getPostalCode());
				addressDTO.setCity(address.getCity());
				addressDTO.setCountry(address.getCountry());
				addressDTO.setCareOf(address.getCareOf());
				addressDTO.setAttention(address.getAttention());
				addressDTO.setPropertyDesignation(address.getPropertyDesignation());
				addressDTO.setApartmentNumber(address.getAppartmentNumber());
				addressDTO.setLocation(mapCoordinates(Optional.ofNullable(address.getLocation())));
				addressDTO.setIsZoningPlanArea(address.getIsZoningPlanArea());
				addressDTO.setInvoiceMarking(address.getInvoiceMarking());

				addressDTODTOList.add(addressDTO);
			}));
		}

		return addressDTODTOList;
	}

	private generated.client.casedata.CoordinatesDTO mapCoordinates(final Optional<CoordinatesDTO> location) {
		final generated.client.casedata.CoordinatesDTO coordinatesDTO = new generated.client.casedata.CoordinatesDTO();
		coordinatesDTO.setLatitude(location.map(CoordinatesDTO::getLatitude).orElse(null));
		coordinatesDTO.setLongitude(location.map(CoordinatesDTO::getLongitude).orElse(null));
		return coordinatesDTO;
	}

	private List<ContactInformationDTO> mapContactInformation(final StakeholderDTO stakeholderDTO) {
		final List<ContactInformationDTO> contactInformationDTOList = new ArrayList<>();

		if (stakeholderDTO.getCellphoneNumber() != null) {
			final ContactInformationDTO contactInformationDTO = new ContactInformationDTO();
			contactInformationDTO.setContactType(ContactInformationDTO.ContactTypeEnum.CELLPHONE);
			contactInformationDTO.setValue(stakeholderDTO.getCellphoneNumber());
			contactInformationDTOList.add(contactInformationDTO);
		}
		if (stakeholderDTO.getPhoneNumber() != null) {
			final ContactInformationDTO contactInformationDTO = new ContactInformationDTO();
			contactInformationDTO.setContactType(ContactInformationDTO.ContactTypeEnum.PHONE);
			contactInformationDTO.setValue(stakeholderDTO.getPhoneNumber());
			contactInformationDTOList.add(contactInformationDTO);
		}
		if (stakeholderDTO.getEmailAddress() != null) {
			final ContactInformationDTO contactInformationDTO = new ContactInformationDTO();
			contactInformationDTO.setContactType(ContactInformationDTO.ContactTypeEnum.EMAIL);
			contactInformationDTO.setValue(stakeholderDTO.getEmailAddress());
			contactInformationDTOList.add(contactInformationDTO);
		}
		return contactInformationDTOList;
	}

	/**
	 * This is unfortunately not a complete PUT-operation because of restrictions in OpenE. They need to send the complete object every time.
	 * And because we don't want to write over some data, like decisions, in CaseData, we need to do the update like this.
	 * <p>
	 * This method will first do a patch on all ErrandDTO-fields. After that it will do PUT on Statuses, Stakeholders and Attachments.
	 *
	 * @param caseId The ID from CaseData.
	 * @param otherCaseDTO The updated case from OpenE.
	 */
	public void putErrand(final Long caseId, final OtherCaseDTO otherCaseDTO) {
		caseDataClient.patchErrand(caseId, mapToPatchErrandDTO(otherCaseDTO));

		final var statusDTO = new StatusDTO();
		statusDTO.setStatusType(KOMPLETTERING_INKOMMEN_STATUS);
		statusDTO.setDateTime(OffsetDateTime.now());
		caseDataClient.putStatusOnErrand(caseId, List.of(statusDTO));
		caseDataClient.putStakeholdersOnErrand(caseId, mapStakeholders(otherCaseDTO.getStakeholders()));

		final var result = caseDataClient.getAttachmentsByErrandNumber(otherCaseDTO.getExternalCaseId());
		if (result != null) {
			result.forEach(attachment -> caseDataClient.deleteAttachment(attachment.getId()));
		}


		otherCaseDTO.getAttachments().stream().map(attachment -> mapAttachment(attachment, otherCaseDTO.getExternalCaseId()))
			.forEach(caseDataClient::postAttachment);
	}

}
