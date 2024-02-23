package se.sundsvall.casemanagement.integration.ecos;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.service.CitizenService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

import minutmiljo.AddPartyToCase;
import minutmiljo.AddPartyToCaseSvcDto;
import minutmiljo.AddressTypeSvcDto;
import minutmiljo.ArrayOfAddressTypeSvcDto;
import minutmiljo.ArrayOfContactInfoItemSvcDto;
import minutmiljo.ArrayOfContactInfoSvcDto;
import minutmiljo.ArrayOfPartyAddressSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfguid;
import minutmiljo.ContactInfoItemSvcDto;
import minutmiljo.ContactInfoSvcDto;
import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreatePersonParty;
import minutmiljo.OrganizationSvcDto;
import minutmiljo.PartyAddressSvcDto;
import minutmiljo.PersonSvcDto;
import minutmiljo.SearchParty;
import minutmiljo.SearchPartyResponse;
import minutmiljo.SearchPartySvcDto;

@Service
public class PartyService {

	private final MinutMiljoClient minutMiljoClient;

	private final CitizenService citizenService;

	public PartyService(final MinutMiljoClient minutMiljoClient, final CitizenService citizenService) {
		this.minutMiljoClient = minutMiljoClient;
		this.citizenService = citizenService;
	}


	public List<Map<String, ArrayOfguid>> findAndAddPartyToCase(final EnvironmentalCaseDTO eCase, final String caseId) {

		final var organizationDTOs = eCase.getStakeholders().stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.toList();

		final List<PersonDTO> privateDTOs = eCase.getStakeholders().stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.toList();

		final List<Map<String, ArrayOfguid>> mapped;

		if (!organizationDTOs.isEmpty()) {
			mapped = organizationDTOs.stream()
				.map((OrganizationDTO organizationDTO) -> mapAsOrganization(organizationDTO, privateDTOs))
				.toList();
		} else {
			mapped = privateDTOs.stream().
				map(this::mapAsPerson)
				.toList();
		}
		mapped.forEach(key -> addPartyToCase(caseId, key));

		return mapped;
	}

	private Map<String, ArrayOfguid> mapAsOrganization(final OrganizationDTO organizationDTO, final List<PersonDTO> personDTOs) {

		final var searchResult = searchPartyByOrganizationNumber(organizationDTO.getOrganizationNumber());
		var dto = new OrganizationSvcDto();

		if (!searchResult.getPartySvcDto().isEmpty()) {
			dto = (OrganizationSvcDto) searchResult.getPartySvcDto().getFirst();
		} else if (searchResult.getPartySvcDto().isEmpty() || searchResult.getPartySvcDto().getFirst() != null) {
			dto = new OrganizationSvcDto()
				.withNationalIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationDTO.getOrganizationNumber()))
				.withOrganizationName(organizationDTO.getOrganizationName())
				.withAddresses(getEcosAddresses(organizationDTO.getAddresses()));

			dto.withContactInfo(new ArrayOfContactInfoSvcDto().withContactInfoSvcDto(
				personDTOs.stream()
					.map(this::getEcosContactInfo)
					.flatMap(personDTO -> personDTO.getContactInfoSvcDto()
						.stream())
					.toList()));

			final var result = minutMiljoClient.createOrganizationParty(new CreateOrganizationParty().withOrganizationParty(dto));

			if (result == null) {
				return Collections.emptyMap();
			}
			dto.setId(result.getCreateOrganizationPartyResult());
		}

		final var roles = getEcosFacilityRoles(organizationDTO);
		return Map.of(dto.getId(), roles);
	}

	private Map<String, ArrayOfguid> mapAsPerson(final PersonDTO personDTO) {
		final var searchPartyResult = searchPartyByPersonId(personDTO.getPersonId());
		final PersonSvcDto dto;

		if (searchPartyResult != null && !searchPartyResult.getPartySvcDto().isEmpty()) {
			dto = (PersonSvcDto) searchPartyResult.getPartySvcDto().getFirst();
		} else {
			dto = getPersonSvcDto(personDTO);

			final var result = minutMiljoClient.createPersonParty(new CreatePersonParty().withPersonParty(dto));
			dto.setId(result.getCreatePersonPartyResult());
		}
		final var roles = getEcosFacilityRoles(personDTO);
		return Map.of(dto.getId(), roles);

	}

	private void addPartyToCase(final String caseId, final Map<String, ArrayOfguid> partyRoles) {

		partyRoles.forEach((partyId, roles) -> {
			final var addPartyToCase = new AddPartyToCase()
				.withModel(new AddPartyToCaseSvcDto()
					.withCaseId(caseId)
					.withPartyId(partyId)
					.withRoles(roles));

			minutMiljoClient.addPartyToCase(addPartyToCase);
		});
	}


	private PersonSvcDto getPersonSvcDto(final PersonDTO personDTO) {

		final var personSvcDto = new PersonSvcDto()
			.withFirstName(personDTO.getFirstName())
			.withLastName(personDTO.getLastName())
			.withAddresses(getEcosAddresses(personDTO.getAddresses()))
			.withContactInfo(getEcosContactInfo(personDTO).getContactInfoSvcDto().getFirst());

		personSvcDto.setNationalIdentificationNumber(CaseUtil.getSokigoFormattedPersonalNumber(citizenService.getPersonalNumber(personDTO.getPersonId())));

		return personSvcDto;
	}

	private ArrayOfguid getEcosFacilityRoles(final StakeholderDTO s) {

		return new ArrayOfguid()
			.withGuid(s.getRoles().stream()
				.map(role ->
					switch (role) {
						case INVOICE_RECIPIENT -> Constants.ECOS_ROLE_ID_FAKTURAMOTTAGARE;
						case OPERATOR -> Constants.ECOS_ROLE_ID_VERKSAMHETSUTOVARE;
						case CONTACT_PERSON -> Constants.ECOS_ROLE_ID_KONTAKTPERSON;
						case APPLICANT -> Constants.ECOS_ROLE_ID_SOKANDE;
						case INSTALLER -> Constants.ECOS_ROLE_ID_INSTALLATOR;
						default ->
							throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "The request contained a stakeholder role that was not expected. This should be discovered in the validation of the input. Something in the validation is wrong.");
					})
				.toList());
	}


	/**
	 * Search for party with and without hyphen in personal number
	 *
	 * @param personId personId for person to search for
	 * @return ArrayOfPartySvcDto
	 */
	private ArrayOfPartySvcDto searchPartyByPersonId(final String personId) {

		final var searchPartyWithHyphen = new SearchParty()
			.withModel(new SearchPartySvcDto()
				.withPersonalIdentificationNumber((CaseUtil
					.getSokigoFormattedPersonalNumber(citizenService.getPersonalNumber(personId)))));

		final var resultWithHyphen = minutMiljoClient.searchParty(searchPartyWithHyphen);

		if (resultWithHyphen != null && !resultWithHyphen.getSearchPartyResult().getPartySvcDto().isEmpty()) {
			return resultWithHyphen.getSearchPartyResult();
		} else {
			final var searchPartyWithoutHyphen = new SearchParty().withModel(new SearchPartySvcDto()
				.withPersonalIdentificationNumber(citizenService.getPersonalNumber(personId)));

			return Optional.ofNullable(minutMiljoClient.searchParty(searchPartyWithoutHyphen)).orElse(new SearchPartyResponse()).getSearchPartyResult();
		}
	}

	private ArrayOfContactInfoSvcDto getEcosContactInfo(final StakeholderDTO s) {
		final var arrayOfContactInfoSvcDto = new ArrayOfContactInfoSvcDto();
		final var contactInfoSvcDto = new ContactInfoSvcDto();
		final var arrayOfContactInfoItemSvcDto = new ArrayOfContactInfoItemSvcDto();

		if (s.getEmailAddress() != null) {
			final var item = new ContactInfoItemSvcDto();

			item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_OVRIGT);
			item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_EPOST);
			item.setValue(s.getEmailAddress());

			arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
		}
		if (s.getCellphoneNumber() != null) {
			final var item = new ContactInfoItemSvcDto();

			item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_MOBIL);
			item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON);
			item.setValue(s.getCellphoneNumber());

			arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
		}

		if (s.getPhoneNumber() != null) {
			final var item = new ContactInfoItemSvcDto();

			item.setContactDetailTypeId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_HUVUDNUMMER);
			item.setContactPathId(Constants.ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON);
			item.setValue(s.getPhoneNumber());

			arrayOfContactInfoItemSvcDto.getContactInfoItemSvcDto().add(item);
		}

		contactInfoSvcDto.setContactDetails(arrayOfContactInfoItemSvcDto);

		if (s instanceof final PersonDTO personDTO) {
			contactInfoSvcDto.setTitle(personDTO.getFirstName() + " " + personDTO.getLastName());
		} else if (s instanceof final OrganizationDTO organizationDTO) {
			contactInfoSvcDto.setTitle(organizationDTO.getAuthorizedSignatory());
		}

		arrayOfContactInfoSvcDto.getContactInfoSvcDto().add(contactInfoSvcDto);
		return arrayOfContactInfoSvcDto;
	}

	private ArrayOfPartyAddressSvcDto getEcosAddresses(final List<AddressDTO> addressDTOS) {
		if (addressDTOS == null) {
			return null;
		}

		return new ArrayOfPartyAddressSvcDto()
			.withPartyAddressSvcDto(
				addressDTOS.stream()
					.map(addressDTO -> new PartyAddressSvcDto()
						.withCareOfName(addressDTO.getCareOf())
						.withCountry(addressDTO.getCountry())
						.withPostalArea(addressDTO.getCity())
						.withPostCode(addressDTO.getPostalCode())
						.withStreetName(addressDTO.getStreet())
						.withStreetNumber(addressDTO.getHouseNumber())
						.withAddressTypes(new ArrayOfAddressTypeSvcDto()
							.withAddressTypeSvcDto(
								addressDTO.getAddressCategories().stream()
									.map(adressType -> new AddressTypeSvcDto()
										.withId(
											switch (adressType) {
												case INVOICE_ADDRESS -> Constants.ECOS_ADDRESS_TYPE_ID_FAKTURAADRESS;
												case POSTAL_ADDRESS -> Constants.ECOS_ADDRESS_TYPE_ID_POSTADRESS;
												case VISITING_ADDRESS -> Constants.ECOS_ADDRESS_TYPE_ID_BESOKSADRESS;
											}))
									.toList())))
					.toList());
	}


	public ArrayOfPartySvcDto searchPartyByOrganizationNumber(final String organizationNumber) {

		// Find party both with and without prefix "16"
		final var allParties = new ArrayOfPartySvcDto();

		// Search for party without prefix
		final var searchPartyWithoutPrefix = new SearchParty()
			.withModel(new SearchPartySvcDto()
				.withOrganizationIdentificationNumber(organizationNumber));

		final var partiesWithoutPrefix = minutMiljoClient.searchParty(searchPartyWithoutPrefix);

		// Search for party with prefix
		final var searchPartyWithPrefix = new SearchParty()
			.withModel(new SearchPartySvcDto()
				.withOrganizationIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber)));

		final var partiesWithPrefix = minutMiljoClient.searchParty(searchPartyWithPrefix);

		if (partiesWithoutPrefix != null) {
			allParties.getPartySvcDto().addAll(partiesWithoutPrefix.getSearchPartyResult().getPartySvcDto());
		}
		if (partiesWithPrefix != null) {
			allParties.getPartySvcDto().addAll(partiesWithPrefix.getSearchPartyResult().getPartySvcDto());
		}

		return allParties;
	}

}
