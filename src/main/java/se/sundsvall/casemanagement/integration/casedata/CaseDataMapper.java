package se.sundsvall.casemanagement.integration.casedata;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
		return new generated.client.casedata.AttachmentDTO()
			.category(attachment.getCategory())
			.note(attachment.getNote())
			.name(attachment.getName())
			.extension(attachment.getExtension())
			.mimeType(attachment.getMimeType())
			.file(attachment.getFile())
			.extraParameters(attachment.getExtraParameters())
			.errandNumber(errandNumber);
	}

	public static ErrandDTO toErrandDTO(final OtherCaseDTO otherCase) {
		final var errand = new ErrandDTO()
			.caseType(otherCase.getCaseType())
			.externalCaseId(otherCase.getExternalCaseId())
			.description(otherCase.getDescription())
			.caseTitleAddition(otherCase.getCaseTitleAddition())
			.stakeholders(toStakeholderDTOs(otherCase.getStakeholders()))
			.extraParameters(otherCase.getExtraParameters())
			.channel(Optional.ofNullable(otherCase.getExternalCaseId())
				.map(id -> ErrandDTO.ChannelEnum.ESERVICE)
				.orElse(null))
			.facilities(Optional.ofNullable(otherCase.getFacilities())
				.map(CaseDataMapper::toFacilityDTOs)
				.orElse(null));

		Optional.ofNullable(otherCase.getExtraParameters())
			.map(extraParameters -> extraParameters.get(APPLICATION_PRIORITY_KEY))
			.filter(StringUtils::isNotBlank)
			.map(ErrandDTO.PriorityEnum::valueOf)
			.ifPresent(errand::setPriority);

		return errand;
	}

	public static List<generated.client.casedata.FacilityDTO> toFacilityDTOs(final List<FacilityDTO> facilityDTOS) {
		return Optional.ofNullable(facilityDTOS)
			.map(facilities -> facilities.stream()
				.map(CaseDataMapper::toFacilityDTO)
				.toList())
			.orElse(emptyList());
	}

	public static generated.client.casedata.FacilityDTO toFacilityDTO(final FacilityDTO facilityDTO) {
		return Optional.ofNullable(facilityDTO).map(f -> new generated.client.casedata.FacilityDTO()
				.mainFacility(f.isMainFacility())
				.facilityType(f.getFacilityType())
				.facilityCollectionName(f.getFacilityCollectionName())
				.description(f.getDescription())
				.extraParameters(f.getExtraParameters())
				.address(toFacilityAddressDTO(f.getAddress())))
			.orElse(null);
	}

	public static generated.client.casedata.AddressDTO toFacilityAddressDTO(final AddressDTO dto) {
		return Optional.ofNullable(dto).map(address -> new generated.client.casedata.AddressDTO()
				.city(address.getCity())
				.country(address.getCountry())
				.postalCode(address.getPostalCode())
				.street(address.getStreet())
				.houseNumber(address.getHouseNumber())
				.careOf(address.getCareOf())
				.attention(address.getAttention())
				.propertyDesignation(address.getPropertyDesignation())
				.apartmentNumber(address.getAppartmentNumber())
				.location(toCoordinatesDTO(address.getLocation()))
				.isZoningPlanArea(address.getIsZoningPlanArea())
				.invoiceMarking(address.getInvoiceMarking())
				.addressCategory(Optional.ofNullable(dto.getAddressCategories())
					.map(a -> generated.client.casedata.AddressDTO.AddressCategoryEnum.VISITING_ADDRESS)
					.orElse(null)))
			.orElse(null);
	}

	// Skapar en ny adress för varje AddressCategory som finns i AddressDTO.
	public static List<generated.client.casedata.AddressDTO> toStakeholderAddressDTOs(final List<AddressDTO> addressDTOs) {
		return Optional.ofNullable(addressDTOs).orElse(emptyList()).stream()
			.flatMap(address -> address.getAddressCategories().stream()
				.map(addressCategory -> new generated.client.casedata.AddressDTO()
					.addressCategory(generated.client.casedata.AddressDTO.AddressCategoryEnum.fromValue(addressCategory.toString()))
					.street(address.getStreet())
					.houseNumber(address.getHouseNumber())
					.postalCode(address.getPostalCode())
					.city(address.getCity())
					.country(address.getCountry())
					.careOf(address.getCareOf())
					.attention(address.getAttention())
					.propertyDesignation(address.getPropertyDesignation())
					.apartmentNumber(address.getAppartmentNumber())
					.location(toCoordinatesDTO(address.getLocation()))
					.isZoningPlanArea(address.getIsZoningPlanArea())
					.invoiceMarking(address.getInvoiceMarking())))
			.toList();
	}

	public static PatchErrandDTO toPatchErrandDTO(final OtherCaseDTO otherCaseDTO) {
		final var errand = new PatchErrandDTO()
			.caseType(PatchErrandDTO.CaseTypeEnum.fromValue(otherCaseDTO.getCaseType()))
			.externalCaseId(otherCaseDTO.getExternalCaseId())
			.description(otherCaseDTO.getDescription())
			.caseTitleAddition(otherCaseDTO.getCaseTitleAddition())
			.facilities(toFacilityDTOs(otherCaseDTO.getFacilities()))
			.extraParameters(otherCaseDTO.getExtraParameters());

		Optional.ofNullable(otherCaseDTO.getExtraParameters())
			.map(extraParameters -> extraParameters.get(APPLICATION_PRIORITY_KEY))
			.filter(StringUtils::isNotBlank)
			.map(PatchErrandDTO.PriorityEnum::valueOf)
			.ifPresent(errand::setPriority);

		return errand;
	}

	public static List<generated.client.casedata.StakeholderDTO> toStakeholderDTOs(final List<StakeholderDTO> stakeholderDTOs) {
		final var personDTOs = Optional.ofNullable(stakeholderDTOs).orElse(emptyList()).stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.map(stakeholder -> new generated.client.casedata.StakeholderDTO()
				.roles(toRoles(stakeholder.getRoles(), stakeholder.getExtraParameters()))
				.contactInformation(toContactInformationDTO(stakeholder))
				.addresses(toStakeholderAddressDTOs(stakeholder.getAddresses()))
				.extraParameters(stakeholder.getExtraParameters())
				.type(generated.client.casedata.StakeholderDTO.TypeEnum.PERSON)
				.firstName(stakeholder.getFirstName())
				.lastName(stakeholder.getLastName())
				.personId(stakeholder.getPersonId()));

		final var organizationDTOs = Optional.ofNullable(stakeholderDTOs).orElse(emptyList()).stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.map(stakeholder -> new generated.client.casedata.StakeholderDTO()
				.roles(toRoles(stakeholder.getRoles(), stakeholder.getExtraParameters()))
				.contactInformation(toContactInformationDTO(stakeholder))
				.addresses(toStakeholderAddressDTOs(stakeholder.getAddresses()))
				.extraParameters(stakeholder.getExtraParameters())
				.type(generated.client.casedata.StakeholderDTO.TypeEnum.ORGANIZATION)
				.organizationName(stakeholder.getOrganizationName())
				.organizationNumber(formatOrganizationNumber(stakeholder.getOrganizationNumber()))
				.authorizedSignatory(stakeholder.getAuthorizedSignatory()));

		return Stream.concat(organizationDTOs, personDTOs).toList();
	}

	public static generated.client.casedata.CoordinatesDTO toCoordinatesDTO(final CoordinatesDTO location) {
		return Optional.ofNullable(location).map(coordinatesDTO -> new generated.client.casedata.CoordinatesDTO()
				.latitude(location.getLatitude())
				.longitude(location.getLongitude()))
			.orElse(null);
	}

	public static List<ContactInformationDTO> toContactInformationDTO(final StakeholderDTO stakeholderDTO) {
		final var contactInformation = new ArrayList<ContactInformationDTO>();

		Optional.ofNullable(stakeholderDTO.getCellphoneNumber()).ifPresent(cellphoneNumber ->
			contactInformation.add(new ContactInformationDTO()
				.contactType(ContactInformationDTO.ContactTypeEnum.CELLPHONE)
				.value(cellphoneNumber)));

		Optional.ofNullable(stakeholderDTO.getPhoneNumber()).ifPresent(phoneNumber ->
			contactInformation.add(new ContactInformationDTO()
				.contactType(ContactInformationDTO.ContactTypeEnum.PHONE)
				.value(phoneNumber)));

		Optional.ofNullable(stakeholderDTO.getEmailAddress()).ifPresent(emailAddress ->
			contactInformation.add(new ContactInformationDTO()
				.contactType(ContactInformationDTO.ContactTypeEnum.EMAIL)
				.value(emailAddress)));

		return contactInformation;
	}


	static List<String> toRoles(final List<String> roles, final Map<String, String> extraParameters) {
		final var roleSet = new HashSet<>(roles);

		Optional.ofNullable(extraParameters)
			.map(params -> params.get("roles"))
			.map(rolesString -> rolesString.split(","))
			.stream()
			.flatMap(Arrays::stream)
			.map(String::trim)
			.filter(role -> !role.isBlank())
			.forEach(roleSet::add);

		return List.copyOf(roleSet);
	}

	private static String formatOrganizationNumber(final String organizationNumber) {
		return Optional.ofNullable(organizationNumber)
			.filter(number -> Stream.of(13, 12, 11, 10)
				.anyMatch(i -> number.length() == i))
			.map(number -> number.replaceAll("\\D", ""))
			.map(number -> number.substring(0, number.length() - 4) + "-" + number.substring(number.length() - 4))
			.orElse(null);
	}

}
