package se.sundsvall.casemanagement.integration.casedata;

import static java.util.Objects.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CoordinatesDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;

import generated.client.casedata.ContactInformationDTO;
import generated.client.casedata.ErrandDTO;
import generated.client.casedata.PatchErrandDTO;

public final class CaseDataMapper {

	private static final String APPLICATION_PRIORITY_KEY = "application.priority";

	private CaseDataMapper() {
		// Prevent instantiation
	}

	public static generated.client.casedata.AttachmentDTO toAttachment(final AttachmentDTO attachment, final String errandNumber) {
		final generated.client.casedata.AttachmentDTO attachmentDTO = new generated.client.casedata.AttachmentDTO();
		attachmentDTO.setCategory(attachment.getCategory());
		attachmentDTO.setName(attachment.getName());
		attachmentDTO.setExtension(attachment.getExtension());
		attachmentDTO.setMimeType(attachment.getMimeType());
		attachmentDTO.setFile(attachment.getFile());
		attachmentDTO.setExtraParameters(attachment.getExtraParameters());
		attachmentDTO.setNote(attachment.getNote());
		attachmentDTO.setErrandNumber(errandNumber);

		return attachmentDTO;
	}

	public static ErrandDTO toErrandDTO(final OtherCaseDTO otherCase) {
		final var errandDTO = new ErrandDTO();

		errandDTO.setCaseType(otherCase.getCaseType());
		errandDTO.setExternalCaseId(otherCase.getExternalCaseId());
		errandDTO.setDescription(otherCase.getDescription());
		errandDTO.setCaseTitleAddition(otherCase.getCaseTitleAddition());
		errandDTO.setStakeholders(toStakeholderDTOs(otherCase.getStakeholders()));
		errandDTO.setExtraParameters(otherCase.getExtraParameters());
		Optional.ofNullable(otherCase.getFacilities()).ifPresent(f -> errandDTO.setFacilities(toFacilityDTOs(f)));

		if (!isNull(otherCase.getExtraParameters())) {
			final String priority = otherCase.getExtraParameters().get(APPLICATION_PRIORITY_KEY);
			if (StringUtils.isNotBlank(priority)) {
				errandDTO.setPriority(ErrandDTO.PriorityEnum.valueOf(priority));
			}
		}

		return errandDTO;
	}

	public static List<generated.client.casedata.FacilityDTO> toFacilityDTOs(final List<FacilityDTO> facilityDTOS) {
		return facilityDTOS.stream()
			.map(CaseDataMapper::toFacilityDTO)
			.toList();
	}

	public static generated.client.casedata.FacilityDTO toFacilityDTO(final FacilityDTO facilityDTO) {
		return Optional.ofNullable(facilityDTO).map(f -> {
			final var newFacility = new generated.client.casedata.FacilityDTO();
			Optional.of(facilityDTO.isMainFacility()).ifPresent(newFacility::setMainFacility);
			Optional.ofNullable(facilityDTO.getFacilityType()).ifPresent(newFacility::setFacilityType);
			Optional.ofNullable(facilityDTO.getFacilityCollectionName()).ifPresent(newFacility::setFacilityCollectionName);
			Optional.ofNullable(facilityDTO.getDescription()).ifPresent(newFacility::setDescription);
			Optional.ofNullable(facilityDTO.getExtraParameters()).ifPresent(newFacility::setExtraParameters);
			Optional.ofNullable(facilityDTO.getAddress()).ifPresent(address -> newFacility.setAddress(toFacilityAddressDTO(address)));
			return newFacility;
		}).orElse(null);
	}

	public static generated.client.casedata.AddressDTO toFacilityAddressDTO(final AddressDTO dto) {
		return Optional.ofNullable(dto).map(address -> {
			final var addressDTO = new generated.client.casedata.AddressDTO();
			Optional.ofNullable(dto.getCity()).ifPresent(addressDTO::setCity);
			Optional.ofNullable(dto.getCountry()).ifPresent(addressDTO::setCountry);
			Optional.ofNullable(dto.getPostalCode()).ifPresent(addressDTO::setPostalCode);
			Optional.ofNullable(dto.getStreet()).ifPresent(addressDTO::setStreet);
			Optional.ofNullable(dto.getHouseNumber()).ifPresent(addressDTO::setHouseNumber);
			Optional.ofNullable(dto.getCareOf()).ifPresent(addressDTO::setCareOf);
			Optional.ofNullable(dto.getAttention()).ifPresent(addressDTO::setAttention);
			Optional.ofNullable(dto.getPropertyDesignation()).ifPresent(addressDTO::setPropertyDesignation);
			Optional.ofNullable(dto.getAppartmentNumber()).ifPresent(addressDTO::setApartmentNumber);
			Optional.ofNullable(dto.getLocation()).ifPresent(location -> addressDTO.setLocation(toCoordinatesDTO(location)));
			Optional.ofNullable(dto.getIsZoningPlanArea()).ifPresent(addressDTO::setIsZoningPlanArea);
			Optional.ofNullable(dto.getInvoiceMarking()).ifPresent(addressDTO::setInvoiceMarking);
			//Om det finns en kategori så ska den alltid sättas till VISITING_ADDRESS, om det inte skickas med så ska det vara null.
			Optional.ofNullable(dto.getAddressCategories()).ifPresent(a -> addressDTO.setAddressCategory(generated.client.casedata.AddressDTO.AddressCategoryEnum.VISITING_ADDRESS));
			return addressDTO;
		}).orElse(null);
	}

	// Skapar en ny adress för varje AddressCategory som finns i AddressDTO.
	public static List<generated.client.casedata.AddressDTO> toStakeholderAddressDTOs(final List<AddressDTO> addressDTOs) {
		final List<generated.client.casedata.AddressDTO> addresses = new ArrayList<>();

		if (addressDTOs != null) {
			addressDTOs.forEach(address -> address.getAddressCategories().forEach(addressCategory -> {
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
				addressDTO.setLocation(toCoordinatesDTO(address.getLocation()));
				addressDTO.setIsZoningPlanArea(address.getIsZoningPlanArea());
				addressDTO.setInvoiceMarking(address.getInvoiceMarking());

				addresses.add(addressDTO);
			}));
		}

		return addresses;
	}

	public static PatchErrandDTO toPatchErrandDTO(final OtherCaseDTO otherCaseDTO) {
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

	public static List<generated.client.casedata.StakeholderDTO> toStakeholderDTOs(final List<StakeholderDTO> stakeholderDTOs) {
		return stakeholderDTOs.stream()
			.map(stakeholder -> {
				final var dto = new generated.client.casedata.StakeholderDTO();
				dto.setRoles(stakeholder.getRoles());
				dto.setContactInformation(toContactInformationDTO(stakeholder));
				dto.setAddresses(toStakeholderAddressDTOs(stakeholder.getAddresses()));
				dto.setExtraParameters(stakeholder.getExtraParameters());

				if (stakeholder instanceof final PersonDTO personDTO) {
					dto.setType(generated.client.casedata.StakeholderDTO.TypeEnum.PERSON);
					dto.setFirstName(personDTO.getFirstName());
					dto.setLastName(personDTO.getLastName());
					dto.setPersonId(personDTO.getPersonId());

				} else if (stakeholder instanceof final OrganizationDTO organizationDTO) {
					dto.setType(generated.client.casedata.StakeholderDTO.TypeEnum.ORGANIZATION);
					dto.setOrganizationName(organizationDTO.getOrganizationName());
					dto.setOrganizationNumber(organizationDTO.getOrganizationNumber());
					dto.setAuthorizedSignatory(organizationDTO.getAuthorizedSignatory());
				}
				return dto;
			})
			.toList();
	}


	public static generated.client.casedata.CoordinatesDTO toCoordinatesDTO(final CoordinatesDTO location) {
		return Optional.ofNullable(location).map(c -> new generated.client.casedata.CoordinatesDTO()
			.latitude(location.getLatitude()).longitude(location.getLongitude())).orElse(null);
	}

	public static List<ContactInformationDTO> toContactInformationDTO(final StakeholderDTO stakeholderDTO) {
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

}
