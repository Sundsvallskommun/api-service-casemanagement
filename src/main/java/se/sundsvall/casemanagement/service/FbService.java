package se.sundsvall.casemanagement.service;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.integration.fb.FbClient;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class FbService {

	private final FbClient fbClient;

	private final RegisterbeteckningService registerbeteckningService;

	@Value("${integration.fb.username}")
	private String fbUsername;

	@Value("${integration.fb.password}")
	private String fbPassword;

	@Value("${integration.fb.database}")
	private String fbDatabase;

	public FbService(final FbClient fbClient, final RegisterbeteckningService registerbeteckningService) {
		this.fbClient = fbClient;
		this.registerbeteckningService = registerbeteckningService;
	}

	public FbPropertyInfo getPropertyInfoByPropertyDesignation(final String propertyDesignation) {

		final var trimmedPropertyDesignation = propertyDesignation.trim().toUpperCase();

		final var registerbeteckningsreferens = Optional.ofNullable(registerbeteckningService.getRegisterbeteckningsreferens(trimmedPropertyDesignation))
			.orElseThrow(() -> Problem.valueOf(Status.BAD_REQUEST, String.format(Constants.ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND, trimmedPropertyDesignation)));

		return Optional.ofNullable(getFbPropertyInfo(registerbeteckningsreferens))
			.orElseThrow(() -> Problem.valueOf(Status.BAD_REQUEST, String.format(Constants.ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND, trimmedPropertyDesignation)));

	}

	private FbPropertyInfo getFbPropertyInfo(final Registerbeteckningsreferens registerbeteckningsreferens) {

		final var propertyInfoResponse = fbClient.getPropertyInfoByUuid(
			List.of(registerbeteckningsreferens.getRegisterenhet()),
			fbDatabase,
			fbUsername,
			fbPassword
		);

		final var fnr = Optional.ofNullable(propertyInfoResponse)
			.map(ResponseDto::getData)
			.filter(CaseUtil::notNullOrEmpty)
			.map(data -> data.getFirst().getFnr())
			.orElse(null);

		if (fnr == null) {
			return null;
		}

		final var addressInfoResponse = fbClient.getAddressInfoByUuid(
			List.of(registerbeteckningsreferens.getRegisterenhet()),
			fbDatabase,
			fbUsername,
			fbPassword
		);

		final var adressPlatsId = Optional.ofNullable(addressInfoResponse)
			.map(ResponseDto::getData)
			.filter(CaseUtil::notNullOrEmpty)
			.map(data -> data.getFirst().getGrupp())
			.filter(CaseUtil::notNullOrEmpty)
			.map(grupp -> grupp.getFirst().getAdressplatsId())
			.orElse(null);

		return new FbPropertyInfo().withFnr(fnr).withAdressplatsId(adressPlatsId);
	}

	/**
	 * Get propertyOwners of specified property
	 *
	 * @param propertyDesignation the specified property
	 * @return List of propertyOwners
	 */
	public List<StakeholderDTO> getPropertyOwnerByPropertyDesignation(final String propertyDesignation) {

		final var propertyInfo = getPropertyInfoByPropertyDesignation(propertyDesignation);
		final var propertyOwnerByFnr = fbClient.getPropertyOwnerByFnr(
			List.of(propertyInfo.getFnr()),
			fbDatabase,
			fbUsername,
			fbPassword);

		final var uuidList = Optional.ofNullable(propertyOwnerByFnr)
			.map(ResponseDto::getData)
			.filter(CaseUtil::notNullOrEmpty)
			.map(data -> data.getFirst().getGrupp())
			.filter(CaseUtil::notNullOrEmpty)
			.map(grupp -> grupp.stream().map(GruppItem::getUuid).toList())
			.filter(CaseUtil::notNullOrEmpty)
			.orElse(emptyList());

		final var propertyOwnerInfoByUuidList = fbClient.getPropertyOwnerInfoByUuid(uuidList, fbDatabase, fbUsername, fbPassword);

		return propertyOwnerInfoByUuidList.getData().stream()
			.filter(data -> StringUtils.isNotEmpty(data.getIdentitetsnummer()))
			.map(data -> {
				final var addressDTO = getAddressForPropertyOwnerByUuid(data.getIdentitetsnummer());
				if (isPropertyOwnerPerson(data)) {
					return createPersonDTO(data, addressDTO);
				} else if (isNotEmpty(data.getGallandeOrganisationsnamn())) {
					return createOrganizationDTO(data, addressDTO);
				}
				return null;
			})
			.filter(Objects::nonNull)
			.toList();

	}

	private boolean isPropertyOwnerPerson(final DataItem data) {
		return Constants.FB_JURIDISK_FORM_PRIVATPERSON.equals(data.getJuridiskForm())
			&& data.getGallandeFornamn() != null
			&& data.getGallandeEfternamn() != null;
	}

	private PersonDTO createPersonDTO(final DataItem data, final AddressDTO addressDTO) {
		return PersonDTO.builder()
			.withPersonalNumber(data.getIdentitetsnummer())
			.withFirstName(data.getGallandeFornamn())
			.withLastName(data.getGallandeEfternamn())
			.withRoles(List.of(StakeholderRole.PROPERTY_OWNER.toString()))
			.withAddresses(Optional.ofNullable(addressDTO).map(List::of).orElse(emptyList()))
			.build();
	}

	private OrganizationDTO createOrganizationDTO(final DataItem data, final AddressDTO addressDTO) {
		return OrganizationDTO.builder()
			.withOrganizationNumber(data.getIdentitetsnummer())
			.withOrganizationName(data.getGallandeOrganisationsnamn())
			.withRoles(List.of(StakeholderRole.PROPERTY_OWNER.toString()))
			.withAddresses(Optional.ofNullable(addressDTO).map(List::of).orElse(emptyList()))
			.build();
	}


	private AddressDTO getAddressForPropertyOwnerByUuid(final String persOrgNr) {

		// Get the response from the client
		final ResponseDto response = fbClient.getPropertyOwnerAddressByPersOrgNr(
			List.of(persOrgNr),
			fbDatabase,
			fbUsername,
			fbPassword
		);

		final var dataItem = Optional.ofNullable(response.getData())
			.orElse(emptyList())
			.stream()
			.findFirst()
			.orElse(null);

		if (dataItem != null) {

			return AddressDTO.builder()
				.withAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS))
				.withCountry(dataItem.getLand() != null ? dataItem.getLand() : Constants.SWEDEN)
				.withCity(dataItem.getPostort())
				.withPostalCode(dataItem.getPostnummer())
				.withCareOf(dataItem.getCoAdress())
				.withStreet(getFirstNonNullUtdelningsadress(dataItem))
				.build();
		}

		return null;
	}

	private String getFirstNonNullUtdelningsadress(final DataItem dataItem) {
		// Get the first non-null utdelningsadress
		return Stream.of(dataItem.getUtdelningsadress1(), dataItem.getUtdelningsadress2(), dataItem.getUtdelningsadress3(), dataItem.getUtdelningsadress4())
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

}
