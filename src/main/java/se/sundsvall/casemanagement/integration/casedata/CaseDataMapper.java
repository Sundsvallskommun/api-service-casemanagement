package se.sundsvall.casemanagement.integration.casedata;

import generated.client.casedata.*;
import org.apache.commons.lang3.StringUtils;
import se.sundsvall.casemanagement.api.model.*;

import java.util.*;
import java.util.stream.Stream;

import static generated.client.casedata.ContactInformation.ContactTypeEnum.*;
import static generated.client.casedata.Stakeholder.TypeEnum.ORGANIZATION;
import static generated.client.casedata.Stakeholder.TypeEnum.PERSON;
import static java.util.Collections.emptyList;

public final class CaseDataMapper {

	private static final String APPLICATION_PRIORITY_KEY = "application.priority";

	private CaseDataMapper() {
		// Prevent instantiation
	}

	public static generated.client.casedata.Attachment toAttachment(final AttachmentDTO attachment, final String errandNumber) {
		return new generated.client.casedata.Attachment()
			.category(attachment.getCategory())
			.note(attachment.getNote())
			.name(attachment.getName())
			.extension(attachment.getExtension())
			.mimeType(attachment.getMimeType())
			.file(attachment.getFile())
			.extraParameters(attachment.getExtraParameters())
			.errandNumber(errandNumber);
	}

	public static Errand toErrand(final OtherCaseDTO otherCase) {
		final var errand = new Errand()
			.caseType(otherCase.getCaseType())
			.externalCaseId(otherCase.getExternalCaseId())
			.description(otherCase.getDescription())
			.caseTitleAddition(otherCase.getCaseTitleAddition())
			.stakeholders(toStakeholders(otherCase.getStakeholders()))
			.extraParameters(toExtraParameters(otherCase.getExtraParameters()))
			.channel(Optional.ofNullable(otherCase.getExternalCaseId())
				.map(id -> Errand.ChannelEnum.ESERVICE)
				.orElse(null))
			.facilities(Optional.ofNullable(otherCase.getFacilities())
				.map(CaseDataMapper::toFacilities)
				.orElse(null));

		Optional.ofNullable(otherCase.getExtraParameters())
			.map(extraParameters -> extraParameters.get(APPLICATION_PRIORITY_KEY))
			.filter(StringUtils::isNotBlank)
			.map(Errand.PriorityEnum::valueOf)
			.ifPresent(errand::setPriority);

		return errand;
	}

	public static List<Facility> toFacilities(final List<FacilityDTO> facilityDTOS) {
		return Optional.ofNullable(facilityDTOS)
			.map(facilities -> facilities.stream()
				.map(CaseDataMapper::toFacility)
				.toList())
			.orElse(emptyList());
	}

	public static Facility toFacility(final FacilityDTO facilityDTO) {
		return Optional.ofNullable(facilityDTO).map(f -> new Facility()
				.mainFacility(f.isMainFacility())
				.facilityType(f.getFacilityType())
				.facilityCollectionName(f.getFacilityCollectionName())
				.description(f.getDescription())
				.extraParameters(f.getExtraParameters())
				.address(toFacilityAddress(f.getAddress())))
			.orElse(null);
	}

	public static Address toFacilityAddress(final AddressDTO dto) {
		return Optional.ofNullable(dto).map(address -> new Address()
				.city(address.getCity())
				.country(address.getCountry())
				.postalCode(address.getPostalCode())
				.street(address.getStreet())
				.houseNumber(address.getHouseNumber())
				.careOf(address.getCareOf())
				.attention(address.getAttention())
				.propertyDesignation(address.getPropertyDesignation())
				.apartmentNumber(address.getAppartmentNumber())
				.location(toCoordinates(address.getLocation()))
				.isZoningPlanArea(address.getIsZoningPlanArea())
				.invoiceMarking(address.getInvoiceMarking())
				.addressCategory(Optional.ofNullable(dto.getAddressCategories())
					.map(a -> Address.AddressCategoryEnum.VISITING_ADDRESS)
					.orElse(null)))
			.orElse(null);
	}

	// Skapar en ny adress för varje AddressCategory som finns i AddressDTO.
	public static List<Address> toStakeholderAddresses(final List<AddressDTO> addressDTOs) {
		return Optional.ofNullable(addressDTOs).orElse(emptyList()).stream()
			.flatMap(address -> address.getAddressCategories().stream()
				.map(addressCategory -> new Address()
					.addressCategory(Address.AddressCategoryEnum.fromValue(addressCategory.toString()))
					.street(address.getStreet())
					.houseNumber(address.getHouseNumber())
					.postalCode(address.getPostalCode())
					.city(address.getCity())
					.country(address.getCountry())
					.careOf(address.getCareOf())
					.attention(address.getAttention())
					.propertyDesignation(address.getPropertyDesignation())
					.apartmentNumber(address.getAppartmentNumber())
					.location(toCoordinates(address.getLocation()))
					.isZoningPlanArea(address.getIsZoningPlanArea())
					.invoiceMarking(address.getInvoiceMarking())))
			.toList();
	}

	public static PatchErrand toPatchErrand(final OtherCaseDTO otherCaseDTO) {
		final var errand = new PatchErrand()
			.caseType(PatchErrand.CaseTypeEnum.fromValue(otherCaseDTO.getCaseType()))
			.externalCaseId(otherCaseDTO.getExternalCaseId())
			.description(otherCaseDTO.getDescription())
			.caseTitleAddition(otherCaseDTO.getCaseTitleAddition())
			.facilities(toFacilities(otherCaseDTO.getFacilities()))
			.extraParameters(toExtraParameters(otherCaseDTO.getExtraParameters()));

		Optional.ofNullable(otherCaseDTO.getExtraParameters())
			.map(extraParameters -> extraParameters.get(APPLICATION_PRIORITY_KEY))
			.filter(StringUtils::isNotBlank)
			.map(PatchErrand.PriorityEnum::valueOf)
			.ifPresent(errand::setPriority);

		return errand;
	}

	public static List<Stakeholder> toStakeholders(final List<StakeholderDTO> stakeholderDTOs) {
		final var persons = Optional.ofNullable(stakeholderDTOs).orElse(emptyList()).stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.map(stakeholder -> new Stakeholder()
				.roles(toRoles(stakeholder.getRoles(), stakeholder.getExtraParameters()))
				.contactInformation(toContactInformation(stakeholder))
				.addresses(toStakeholderAddresses(stakeholder.getAddresses()))
				.extraParameters(stakeholder.getExtraParameters())
				.type(PERSON)
				.firstName(stakeholder.getFirstName())
				.lastName(stakeholder.getLastName())
				.personId(stakeholder.getPersonId()));

		final var organizations = Optional.ofNullable(stakeholderDTOs).orElse(emptyList()).stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.map(stakeholder -> new Stakeholder()
				.roles(toRoles(stakeholder.getRoles(), stakeholder.getExtraParameters()))
				.contactInformation(toContactInformation(stakeholder))
				.addresses(toStakeholderAddresses(stakeholder.getAddresses()))
				.extraParameters(stakeholder.getExtraParameters())
				.type(ORGANIZATION)
				.organizationName(stakeholder.getOrganizationName())
				.organizationNumber(formatOrganizationNumber(stakeholder.getOrganizationNumber()))
				.authorizedSignatory(stakeholder.getAuthorizedSignatory()));

		return Stream.concat(organizations, persons).toList();
	}

	public static Coordinates toCoordinates(final CoordinatesDTO location) {
		return Optional.ofNullable(location).map(coordinatesDTO -> new Coordinates()
				.latitude(location.getLatitude())
				.longitude(location.getLongitude()))
			.orElse(null);
	}

	public static List<ContactInformation> toContactInformation(final StakeholderDTO stakeholderDTO) {
		final var contactInformation = new ArrayList<ContactInformation>();

		Optional.ofNullable(stakeholderDTO.getCellphoneNumber()).ifPresent(cellphoneNumber ->
			contactInformation.add(new ContactInformation()
				.contactType(CELLPHONE)
				.value(cellphoneNumber)));

		Optional.ofNullable(stakeholderDTO.getPhoneNumber()).ifPresent(phoneNumber ->
			contactInformation.add(new ContactInformation()
				.contactType(PHONE)
				.value(phoneNumber)));

		Optional.ofNullable(stakeholderDTO.getEmailAddress()).ifPresent(emailAddress ->
			contactInformation.add(new ContactInformation()
				.contactType(EMAIL)
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

	private static List<ExtraParameter> toExtraParameters(final Map<String, String> extraParameters) {
		return Optional.ofNullable(extraParameters).map(params -> params.entrySet().stream()
				.map(entry -> new ExtraParameter()
					.key(entry.getKey())
					.values(List.of(entry.getValue())))
				.toList())
			.orElse(emptyList());
	}

}
