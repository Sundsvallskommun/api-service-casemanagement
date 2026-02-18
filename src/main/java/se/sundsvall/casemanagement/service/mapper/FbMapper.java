package se.sundsvall.casemanagement.service.mapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.util.CaseUtil;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.casemanagement.api.model.enums.AddressCategory.POSTAL_ADDRESS;
import static se.sundsvall.casemanagement.api.model.enums.StakeholderRole.PROPERTY_OWNER;
import static se.sundsvall.casemanagement.util.Constants.SWEDEN;

public final class FbMapper {

	private FbMapper() {
		// Intentionally empty
	}

	public static Integer toFnr(ResponseDto response) {
		return ofNullable(response)
			.map(ResponseDto::getData)
			.filter(CaseUtil::notNullOrEmpty)
			.map(data -> data.getFirst().getFnr())
			.orElse(null);
	}

	public static Integer toAdressplatsId(ResponseDto response) {
		return ofNullable(response)
			.map(ResponseDto::getData)
			.filter(CaseUtil::notNullOrEmpty)
			.map(data -> data.getFirst().getGrupp())
			.filter(CaseUtil::notNullOrEmpty)
			.map(grupp -> grupp.getFirst().getAdressplatsId())
			.orElse(null);
	}

	public static FbPropertyInfo toFbPropertyInfo(Integer fnr, Integer adressPlatsId) {
		return new FbPropertyInfo().withFnr(fnr).withAdressplatsId(adressPlatsId);
	}

	public static List<String> toPropertyUuids(ResponseDto response) {
		return ofNullable(response)
			.map(ResponseDto::getData)
			.filter(CaseUtil::notNullOrEmpty)
			.map(data -> data.getFirst().getGrupp())
			.filter(CaseUtil::notNullOrEmpty)
			.map(grupp -> grupp.stream()
				.map(GruppItem::getUuid)
				.filter(StringUtils::isNotBlank)
				.toList())
			.filter(CaseUtil::notNullOrEmpty)
			.orElse(emptyList());
	}

	public static PersonDTO toPersonDTO(final DataItem data, final AddressDTO addressDTO) {
		return PersonDTO.builder()
			.withPersonalNumber(data.getIdentitetsnummer())
			.withFirstName(data.getGallandeFornamn())
			.withLastName(data.getGallandeEfternamn())
			.withRoles(List.of(PROPERTY_OWNER.toString()))
			.withAddresses(ofNullable(addressDTO).map(List::of).orElse(emptyList()))
			.build();
	}

	public static OrganizationDTO toOrganizationDTO(final DataItem data, final AddressDTO addressDTO) {
		return OrganizationDTO.builder()
			.withOrganizationNumber(data.getIdentitetsnummer())
			.withOrganizationName(data.getGallandeOrganisationsnamn())
			.withRoles(List.of(PROPERTY_OWNER.toString()))
			.withAddresses(ofNullable(addressDTO).map(List::of).orElse(emptyList()))
			.build();
	}

	public static AddressDTO toAddressDTO(ResponseDto response) {
		if (isNull(response)) {
			return null;
		}

		return ofNullable(response.getData()).orElse(emptyList()).stream()
			.map(FbMapper::toAddressDTO)
			.findFirst()
			.orElse(null);
	}

	private static AddressDTO toAddressDTO(DataItem dataItem) {
		return AddressDTO.builder()
			.withAddressCategories(List.of(POSTAL_ADDRESS))
			.withCountry(isNotBlank(dataItem.getLand()) ? dataItem.getLand() : SWEDEN)
			.withCity(dataItem.getPostort())
			.withPostalCode(dataItem.getPostnummer())
			.withCareOf(dataItem.getCoAdress())
			.withStreet(getFirstNonNullUtdelningsadress(dataItem))
			.build();
	}

	private static String getFirstNonNullUtdelningsadress(final DataItem dataItem) {
		// Get the first non-null utdelningsadress
		return Stream.of(dataItem.getUtdelningsadress1(), dataItem.getUtdelningsadress2(), dataItem.getUtdelningsadress3(), dataItem.getUtdelningsadress4())
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}
}
