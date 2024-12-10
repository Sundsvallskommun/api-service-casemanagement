package se.sundsvall.casemanagement.service.mapper;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.api.model.enums.AddressCategory.POSTAL_ADDRESS;
import static se.sundsvall.casemanagement.api.model.enums.StakeholderRole.PROPERTY_OWNER;
import static se.sundsvall.casemanagement.util.Constants.SWEDEN;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;

class FbMapperTest {

	@Test
	void toFnrFromNullOrEmpty() {
		assertThat(FbMapper.toFnr(null)).isNull();
		assertThat(FbMapper.toFnr(createResponse(null))).isNull();
		assertThat(FbMapper.toFnr(createResponse(emptyList()))).isNull();
	}

	@ParameterizedTest
	@MethodSource("toFnrArguments")
	void toFnr(List<Integer> fnrs, Integer expected) {
		final var response = createResponse(fnrs);

		assertThat(FbMapper.toFnr(response)).isEqualTo(expected);
	}

	private static Stream<Arguments> toFnrArguments() {
		return Stream.of(
			Arguments.of(List.of(123), 123),
			Arguments.of(List.of(123, 456), 123),
			Arguments.of(List.of(456, 123), 456));
	}

	@Test
	void toAdressplatsIdFromNullOrEmpty() {
		final var inputWithEmptyGruppList = createResponse(List.of(123));
		inputWithEmptyGruppList.getData().getFirst().setGrupp(emptyList());

		assertThat(FbMapper.toAdressplatsId(null)).isNull();
		assertThat(FbMapper.toAdressplatsId(createResponse(null))).isNull();
		assertThat(FbMapper.toAdressplatsId(createResponse(emptyList()))).isNull();
		assertThat(FbMapper.toAdressplatsId(createResponse(List.of(123)))).isNull();
		assertThat(FbMapper.toAdressplatsId(inputWithEmptyGruppList)).isNull();
	}

	@ParameterizedTest
	@MethodSource("toAdressplatsIdArguments")
	void toAdressplatsId(List<List<Integer>> adressplatsIds, Integer expected) {
		final var response = new ResponseDto();
		Optional.ofNullable(adressplatsIds).ifPresent(items -> {
			items.forEach(item -> {
				final var dataItem = new DataItem();
				dataItem.setGrupp(item.stream().map(FbMapperTest::createGruppItem).toList());

				final var dataList = Optional.ofNullable(response.getData()).orElse(new ArrayList<>());
				dataList.add(dataItem);
				response.setData(dataList);
			});
		});

		assertThat(FbMapper.toAdressplatsId(response)).isEqualTo(expected);
	}

	private static Stream<Arguments> toAdressplatsIdArguments() {
		return Stream.of(
			Arguments.of(List.of(List.of(123)), 123),
			Arguments.of(List.of(List.of(123, 456)), 123),
			Arguments.of(List.of(List.of(456, 123)), 456),
			Arguments.of(List.of(List.of(123, 456), List.of(678, 901)), 123));
	}

	@Test
	void toFbPropertyInfo() {
		final var fnr = new Random().nextInt();
		final var adressplatsId = new Random().nextInt();

		final var bean = FbMapper.toFbPropertyInfo(fnr, adressplatsId);

		assertThat(bean).hasAllNullFieldsOrPropertiesExcept("fnr", "adressplatsId");
		assertThat(bean.getFnr()).isEqualTo(fnr);
		assertThat(bean.getAdressplatsId()).isEqualTo(adressplatsId);
	}

	@Test
	void toFbPropertyInfoFromNull() {
		assertThat(FbMapper.toFbPropertyInfo(null, null)).hasAllNullFieldsOrProperties();
	}

	@Test
	void toPropertyUuidsFromNullOrEmpty() {
		final var inputWithEmptyGruppList = createResponse(List.of(123));
		inputWithEmptyGruppList.getData().getFirst().setGrupp(emptyList());

		assertThat(FbMapper.toPropertyUuids(null)).isEmpty();
		assertThat(FbMapper.toPropertyUuids(createResponse(null))).isEmpty();
		assertThat(FbMapper.toPropertyUuids(createResponse(emptyList()))).isEmpty();
		assertThat(FbMapper.toPropertyUuids(createResponse(List.of(123)))).isEmpty();
		assertThat(FbMapper.toPropertyUuids(inputWithEmptyGruppList)).isEmpty();
	}

	@ParameterizedTest
	@MethodSource("toPropertyUuidsArguments")
	void toPropertyUuids(List<List<String>> uuids, List<String> expected) {
		final var response = new ResponseDto();
		Optional.ofNullable(uuids).ifPresent(items -> {
			items.forEach(item -> {
				final var dataItem = new DataItem();
				dataItem.setGrupp(item.stream().map(FbMapperTest::createGruppItem).toList());

				final var dataList = Optional.ofNullable(response.getData()).orElse(new ArrayList<>());
				dataList.add(dataItem);
				response.setData(dataList);
			});
		});

		assertThat(FbMapper.toPropertyUuids(response)).isEqualTo(expected);
	}

	private static Stream<Arguments> toPropertyUuidsArguments() {
		return Stream.of(
			Arguments.of(List.of(List.of("111")), List.of("111")),
			Arguments.of(List.of(List.of("111", "222")), List.of("111", "222")),
			Arguments.of(List.of(List.of("111", "", " ", "222")), List.of("111", "222")));
	}

	@Test
	void toPersonDTO() {
		final var address = AddressDTO.builder().build();
		final var identitetsnummer = "identitetsnummer";
		final var gallandeEfternamn = "gallandeEfternamn";
		final var gallandeFornamn = "gallandeFornamn";
		final var dataItem = new DataItem();
		dataItem.setIdentitetsnummer(identitetsnummer);
		dataItem.setGallandeEfternamn(gallandeEfternamn);
		dataItem.setGallandeFornamn(gallandeFornamn);

		final var bean = FbMapper.toPersonDTO(dataItem, address);

		assertThat(bean).hasAllNullFieldsOrPropertiesExcept("addresses", "personalNumber", "firstName", "lastName", "roles");
		assertThat(bean.getAddresses()).containsExactly(address);
		assertThat(bean.getPersonalNumber()).isEqualTo(identitetsnummer);
		assertThat(bean.getFirstName()).isEqualTo(gallandeFornamn);
		assertThat(bean.getLastName()).isEqualTo(gallandeEfternamn);
		assertThat(bean.getRoles()).containsExactly(PROPERTY_OWNER.toString());
	}

	@Test
	void toPersonDTOWithNoAddressDTO() {
		final var identitetsnummer = "identitetsnummer";
		final var gallandeEfternamn = "gallandeEfternamn";
		final var gallandeFornamn = "gallandeFornamn";
		final var dataItem = new DataItem();
		dataItem.setIdentitetsnummer(identitetsnummer);
		dataItem.setGallandeEfternamn(gallandeEfternamn);
		dataItem.setGallandeFornamn(gallandeFornamn);

		final var bean = FbMapper.toPersonDTO(dataItem, null);

		assertThat(bean).hasAllNullFieldsOrPropertiesExcept("addresses", "personalNumber", "firstName", "lastName", "roles");
		assertThat(bean.getAddresses()).isEmpty();
		assertThat(bean.getPersonalNumber()).isEqualTo(identitetsnummer);
		assertThat(bean.getFirstName()).isEqualTo(gallandeFornamn);
		assertThat(bean.getLastName()).isEqualTo(gallandeEfternamn);
		assertThat(bean.getRoles()).containsExactly(PROPERTY_OWNER.toString());
	}

	@Test
	void toOrganizationDTO() {
		final var address = AddressDTO.builder().build();
		final var identitetsnummer = "identitetsnummer";
		final var gallandeOrganisationsnamn = "gallandeOrganisationsnamn";

		final var dataItem = new DataItem();
		dataItem.setIdentitetsnummer(identitetsnummer);
		dataItem.setGallandeOrganisationsnamn(gallandeOrganisationsnamn);

		final var bean = FbMapper.toOrganizationDTO(dataItem, address);

		assertThat(bean).hasAllNullFieldsOrPropertiesExcept("addresses", "organizationNumber", "organizationName", "roles");
		assertThat(bean.getAddresses()).containsExactly(address);
		assertThat(bean.getOrganizationNumber()).isEqualTo(identitetsnummer);
		assertThat(bean.getOrganizationName()).isEqualTo(gallandeOrganisationsnamn);
		assertThat(bean.getRoles()).containsExactly(PROPERTY_OWNER.toString());
	}

	@Test
	void toOrganizationDTOWithNoAddressDTO() {
		final var identitetsnummer = "identitetsnummer";
		final var gallandeOrganisationsnamn = "gallandeOrganisationsnamn";

		final var dataItem = new DataItem();
		dataItem.setIdentitetsnummer(identitetsnummer);
		dataItem.setGallandeOrganisationsnamn(gallandeOrganisationsnamn);

		final var bean = FbMapper.toOrganizationDTO(dataItem, null);

		assertThat(bean).hasAllNullFieldsOrPropertiesExcept("addresses", "organizationNumber", "organizationName", "roles");
		assertThat(bean.getAddresses()).isEmpty();
		assertThat(bean.getOrganizationNumber()).isEqualTo(identitetsnummer);
		assertThat(bean.getOrganizationName()).isEqualTo(gallandeOrganisationsnamn);
		assertThat(bean.getRoles()).containsExactly(PROPERTY_OWNER.toString());
	}

	@Test
	void toAddressDTOFromNullOrEmpty() {
		assertThat(FbMapper.toAddressDTO(null)).isNull();
		assertThat(FbMapper.toAddressDTO(createResponse(null))).isNull();
		assertThat(FbMapper.toAddressDTO(createResponse(emptyList()))).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		" "
	})
	@NullAndEmptySource
	void toAddressDTOWhenCountryIsNullOrEmptyOrBlank(String land) {
		final var dataItem = new DataItem();
		final var response = new ResponseDto();
		dataItem.setLand(land);
		response.setData(List.of(dataItem, new DataItem()));

		assertThat(FbMapper.toAddressDTO(response).getCountry()).isEqualTo(SWEDEN);
	}

	@Test
	void toAddressDTO() {
		final var postort = "postort";
		final var postnummer = "postnummer";
		final var coAdress = "coAdress";
		final var utdelningsDress4 = "utdelningsDress4";
		final var land = "land";
		final var dataItem = new DataItem();
		final var response = new ResponseDto();

		dataItem.setLand(land);
		dataItem.setPostort(postort);
		dataItem.setPostnummer(postnummer);
		dataItem.setCoAdress(coAdress);
		dataItem.setUtdelningsadress4(utdelningsDress4);
		response.setData(List.of(dataItem, new DataItem()));

		final var bean = FbMapper.toAddressDTO(response);

		assertThat(bean.getAddressCategories()).containsExactly(POSTAL_ADDRESS);
		assertThat(bean.getCity()).isEqualTo(postort);
		assertThat(bean.getPostalCode()).isEqualTo(postnummer);
		assertThat(bean.getCareOf()).isEqualTo(coAdress);
		assertThat(bean.getStreet()).isEqualTo(utdelningsDress4);
		assertThat(bean.getCountry()).isEqualTo(land);
	}

	private static ResponseDto createResponse(List<Integer> fnrs) {
		final var response = new ResponseDto();
		Optional.ofNullable(fnrs).ifPresent(items -> {
			response.setData(items.stream()
				.map(FbMapperTest::createItem)
				.toList());
		});
		return response;
	}

	private static DataItem createItem(Integer fnr) {
		final var item = new DataItem();
		item.setFnr(fnr);
		return item;
	}

	private static GruppItem createGruppItem(Integer adressplatsId) {
		final var item = new GruppItem();
		item.setAdressplatsId(adressplatsId);
		return item;
	}

	private static GruppItem createGruppItem(String uuid) {
		final var item = new GruppItem();
		item.setUuid(uuid);
		return item;
	}
}
