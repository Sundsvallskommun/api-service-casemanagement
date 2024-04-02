package se.sundsvall.casemanagement.service;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toAddressDTO;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toAdressplatsId;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toFbPropertyInfo;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toFnr;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toOrganizationDTO;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toPersonDTO;
import static se.sundsvall.casemanagement.service.mapper.FbMapper.toPropertyUuids;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.integration.fb.FbClient;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;
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

		final var fnr = toFnr(propertyInfoResponse);
		if (isNull(fnr)) {
			return null;
		}

		final var addressInfoResponse = fbClient.getAddressInfoByUuid(
			List.of(registerbeteckningsreferens.getRegisterenhet()),
			fbDatabase,
			fbUsername,
			fbPassword
		);

		return toFbPropertyInfo(fnr, toAdressplatsId(addressInfoResponse));
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

		final var uuidList = toPropertyUuids(propertyOwnerByFnr);
		final var propertyOwnerInfoByUuidList = fbClient.getPropertyOwnerInfoByUuid(uuidList, fbDatabase, fbUsername, fbPassword);

		return propertyOwnerInfoByUuidList.getData().stream()
			.filter(data -> StringUtils.isNotEmpty(data.getIdentitetsnummer()))
			.map(this::toStakeholderDTO)
			.filter(Objects::nonNull)
			.toList();
	}

	private StakeholderDTO toStakeholderDTO(DataItem data) {
		if (isPropertyOwnerPerson(data)) {
			return toPersonDTO(data, getAddressForPropertyOwnerByUuid(data.getIdentitetsnummer()));
		} else if (isNotEmpty(data.getGallandeOrganisationsnamn())) {
			return toOrganizationDTO(data, getAddressForPropertyOwnerByUuid(data.getIdentitetsnummer()));
		}
		return null;
	}

	private boolean isPropertyOwnerPerson(final DataItem data) {
		return Constants.FB_JURIDISK_FORM_PRIVATPERSON.equals(data.getJuridiskForm())
			&& data.getGallandeFornamn() != null
			&& data.getGallandeEfternamn() != null;
	}

	private AddressDTO getAddressForPropertyOwnerByUuid(final String persOrgNr) {

		final ResponseDto response = fbClient.getPropertyOwnerAddressByPersOrgNr(
			List.of(persOrgNr),
			fbDatabase,
			fbUsername,
			fbPassword
		);

		return toAddressDTO(response);
	}
}
