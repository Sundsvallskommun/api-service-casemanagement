package se.sundsvall.casemanagement.integration.ecos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.EnvironmentalCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.service.CitizenMappingService;
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
import minutmiljo.PartySvcDto;
import minutmiljo.PersonSvcDto;
import minutmiljo.SearchParty;
import minutmiljo.SearchPartySvcDto;

@Service
public class PartyService {

	private final MinutMiljoClient minutMiljoClient;

	private final CitizenMappingService citizenMappingService;

	public PartyService(final MinutMiljoClient minutMiljoClient, final CitizenMappingService citizenMappingService) {
		this.minutMiljoClient = minutMiljoClient;
		this.citizenMappingService = citizenMappingService;
	}

	public void findAndAddPartyToCase(final EnvironmentalCaseDTO caseInput, final String caseId) {

		// to the facility later.
		final var partyList = new ArrayList<PartySvcDto>();

		// The stakeholder is stored with associated roles so that we can set roles later.
		final var partyRoles = new HashMap<String, ArrayOfguid>();

		// If the stakeholder is missing in Ecos, we keep it in this list and create them later (CreateParty)
		final var missingStakeholderDTOS = new ArrayList<StakeholderDTO>();

		populatePartyList(caseInput, partyRoles, partyList, missingStakeholderDTOS);

		// -----> CreateParty
		createParty(partyRoles, partyList, missingStakeholderDTOS);

		// -----> AddPartyToCase
		addPartyToCase(partyRoles, partyList, caseId);
	}

	private void populatePartyList(final EnvironmentalCaseDTO eCase, final Map<String, ArrayOfguid> partyRoles, final List<PartySvcDto> partyList, final List<StakeholderDTO> missingStakeholderDTOS) {


		final var organizationDTOs = eCase.getStakeholders().stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.toList();

		final List<PersonDTO> privateDTOs = eCase.getStakeholders().stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.toList();


		if (!organizationDTOs.isEmpty()) {
			mapAsOrganization(organizationDTOs.get(0), privateDTOs);
		} else {
			mapAsPerson(privateDTOs);
		}


		for (final StakeholderDTO s : eCase.getStakeholders()) {
			ArrayOfPartySvcDto searchPartyResult = null;

			if (s instanceof final OrganizationDTO organizationDTO) {
				searchPartyResult = searchPartyByOrganizationNumber(organizationDTO.getOrganizationNumber());
			} else if (s instanceof final PersonDTO personDTO && (StringUtils.isNotBlank(personDTO.getPersonId()))) {
				searchPartyResult = searchPartyByPersonId(personDTO.getPersonId());
			}

			// If we get a result we put it in partyList, else we put it in missingStakeholders
			if (searchPartyResult == null || searchPartyResult.getPartySvcDto().isEmpty()) {
				// These, we are going to create later
				missingStakeholderDTOS.add(s);
			} else if (!searchPartyResult.getPartySvcDto().isEmpty()) {

				// Sometimes we get multiple search results, but we should only use one in the case.
				final var party = searchPartyResult.getPartySvcDto().get(0);
				partyList.add(party);

				// Adds stakeholder to a hashmap with the role so that we can connect the stakeholder to the case later
				partyRoles.put(party.getId(), getEcosFacilityRoles(s));
			}
		}
	}

	private void mapAsOrganization(final OrganizationDTO organizationDTO, final List<PersonDTO> personDTOs) {

		final var searchResult = searchPartyByOrganizationNumber(organizationDTO.getOrganizationNumber());
		final OrganizationSvcDto dto;

		if (searchResult.getPartySvcDto().isEmpty() && searchResult.getPartySvcDto().get(0) != null) {
			dto = new OrganizationSvcDto()
				.withNationalIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationDTO.getOrganizationNumber()))
				.withOrganizationName(organizationDTO.getOrganizationName())
				.withAddresses(getEcosAddresses(organizationDTO.getAddresses()));

		} else if (!searchResult.getPartySvcDto().isEmpty()) {
			dto = (OrganizationSvcDto) searchResult.getPartySvcDto().get(0);
		}

		final var filtered = personDTOs.stream()
			.filter(p -> contactNameIsAMatch(p, dto.getContactInfo()))
			.toList();

		if (!filtered.isEmpty()) {
			dto.getContactInfo().withContactInfoSvcDto(
				filtered.stream()
					.map(this::getEcosContactInfo)
					.flatMap(personDTO -> personDTO.getContactInfoSvcDto()
						.stream())
					.toList());
		}
		if (dto.getId() == null || !filtered.isEmpty()) {
			minutMiljoClient.createOrganizationParty(new CreateOrganizationParty().withOrganizationParty(dto));
		}

	}

	private boolean contactNameIsAMatch(final PersonDTO personDTO, final ArrayOfContactInfoSvcDto arrayOfContactInfoSvcDto) {

		return arrayOfContactInfoSvcDto.getContactInfoSvcDto().stream()
			.map(contactInfoSvcDto -> contactInfoSvcDto.getTitle()
				.equals(personDTO.getFirstName() + " " + personDTO.getLastName()))
			.findFirst()
			.orElseThrow();

	}

	private void mapAsPerson(final List<PersonDTO> privateDTOs) {

	}

	private void createParty(final Map<String, ArrayOfguid> partyRoles, final List<PartySvcDto> partyList, final List<StakeholderDTO> missingStakeholderDTOS) {

		for (final var s : missingStakeholderDTOS) {
			String guidResult = null;

			if (s instanceof final OrganizationDTO organizationDTO) {
				final var createOrganizationParty = new CreateOrganizationParty().withOrganizationParty(getOrganizationSvcDto(s, organizationDTO));
				guidResult = minutMiljoClient.createOrganizationParty(createOrganizationParty).getCreateOrganizationPartyResult();

			} else if (s instanceof final PersonDTO personDTO) {
				final var createPersonParty = new CreatePersonParty().withPersonParty(getPersonSvcDto(s, personDTO));
				guidResult = minutMiljoClient.createPersonParty(createPersonParty).getCreatePersonPartyResult();
			}

			if (guidResult != null) {
				final var party = new PartySvcDto().withId(guidResult);

				partyList.add(party);
				// Adds party in a hashmap with the role so that we can connect stakeholder to case with the
				// role later
				partyRoles.put(party.getId(), getEcosFacilityRoles(s));

			}
		}
	}

	private void addPartyToCase(final Map<String, ArrayOfguid> partyRoles, final List<PartySvcDto> partyList, final String caseId) {

		partyList.stream().map(p -> new AddPartyToCase()
				.withModel(new AddPartyToCaseSvcDto()
					.withCaseId(caseId)
					.withPartyId(p.getId())
					.withRoles(partyRoles.get(p.getId()))))
			.forEach(minutMiljoClient::addPartyToCase);
	}

	PersonSvcDto getPersonSvcDto(final StakeholderDTO s, final PersonDTO personDTO) {

		final var personSvcDto = new PersonSvcDto()
			.withFirstName(personDTO.getFirstName())
			.withLastName(personDTO.getLastName())
			.withAddresses(getEcosAddresses(s.getAddresses()))
			.withContactInfo(getEcosContactInfo(s).getContactInfoSvcDto().get(0));

		personSvcDto.setNationalIdentificationNumber(CaseUtil.getSokigoFormattedPersonalNumber(citizenMappingService.getPersonalNumber(personDTO.getPersonId())));

		return personSvcDto;
	}

	OrganizationSvcDto getOrganizationSvcDto(final StakeholderDTO s, final OrganizationDTO organizationDTO) {

		return new OrganizationSvcDto()
			.withNationalIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationDTO.getOrganizationNumber()))
			.withOrganizationName(organizationDTO.getOrganizationName())
			.withAddresses(getEcosAddresses(s.getAddresses()))
			.withContactInfo(getEcosContactInfo(s));
	}


	ArrayOfguid getEcosFacilityRoles(final StakeholderDTO s) {

		return new ArrayOfguid()
			.withGuid(s.getRoles().stream()
				.map(role ->
					switch (role) {
						case INVOICE_RECIPENT -> Constants.ECOS_ROLE_ID_FAKTURAMOTTAGARE;
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
					.getSokigoFormattedPersonalNumber(citizenMappingService.getPersonalNumber(personId)))));

		final var resultWithHyphen = minutMiljoClient.searchParty(searchPartyWithHyphen).getSearchPartyResult();

		if (resultWithHyphen != null && !resultWithHyphen.getPartySvcDto().isEmpty()) {
			return resultWithHyphen;
		} else {
			final var searchPartyWithoutHyphen = new SearchParty().withModel(new SearchPartySvcDto()
				.withPersonalIdentificationNumber(citizenMappingService.getPersonalNumber(personId)));

			return minutMiljoClient.searchParty(searchPartyWithoutHyphen).getSearchPartyResult();
		}
	}

	ArrayOfContactInfoSvcDto getEcosContactInfo(final StakeholderDTO s) {
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

	ArrayOfPartyAddressSvcDto getEcosAddresses(final List<AddressDTO> addressDTOS) {
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


	private ArrayOfPartySvcDto searchPartyByOrganizationNumber(final String organizationNumber) {

		// Find party both with and without prefix "16"
		final var allParties = new ArrayOfPartySvcDto();

		// Search for party without prefix
		final var searchPartyWithoutPrefix = new SearchParty()
			.withModel(new SearchPartySvcDto()
				.withOrganizationIdentificationNumber(organizationNumber));

		final var partiesWithoutPrefix = minutMiljoClient.searchParty(searchPartyWithoutPrefix).getSearchPartyResult();

		// Search for party with prefix
		final var searchPartyWithPrefix = new SearchParty()
			.withModel(new SearchPartySvcDto()
				.withOrganizationIdentificationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber)));

		final ArrayOfPartySvcDto partiesWithPrefix = minutMiljoClient.searchParty(searchPartyWithPrefix).getSearchPartyResult();

		if (partiesWithoutPrefix != null) {
			allParties.getPartySvcDto().addAll(partiesWithoutPrefix.getPartySvcDto());
		}
		if (partiesWithPrefix != null) {
			allParties.getPartySvcDto().addAll(partiesWithPrefix.getPartySvcDto());
		}

		return allParties;
	}

}
