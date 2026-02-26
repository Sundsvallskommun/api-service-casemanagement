package se.sundsvall.casemanagement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.integration.fb.FbClient;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.util.Constants;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.casemanagement.TestUtil.ADRESSPLATS_ID;
import static se.sundsvall.casemanagement.TestUtil.FNR;

@ExtendWith(MockitoExtension.class)
class FbServiceTest {

	@Mock
	private FbClient fbClientMock;

	@Mock
	private RegisterbeteckningService registerbeteckningServiceMock;

	@InjectMocks
	private FbService fbService;

	@Test
	void getPropertyInfoByPropertyDesignation() {
		// Arrange
		final var propertyDesignation = "TEST 1:1";
		mockFb();

		// Act
		final var result = fbService.getPropertyInfoByPropertyDesignation(propertyDesignation);

		// Assert
		assertThat(result.getFnr()).isEqualTo(FNR);
		assertThat(result.getAdressplatsId()).isEqualTo(ADRESSPLATS_ID);
	}

	@Test
	void getPropertyInfoByPropertyDesignationNotFound() {
		// Arrange
		final var propertyDesignation = "TEST 1:1";
		// Act & Assert
		assertThatThrownBy(() -> fbService.getPropertyInfoByPropertyDesignation(propertyDesignation))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: The specified propertyDesignation(TEST 1:1) could not be found")
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST);
	}

	@Test
	void getPropertyInfoByPropertyDesignationNoFnr() {
		// Arrange
		final var propertyDesignation = "TEST 1:1";
		final var getPropertyInfoByUuidMockResponse = new ResponseDto();
		getPropertyInfoByUuidMockResponse.setData(List.of(new DataItem()));

		final var registerbeteckningsreferensMock = new Registerbeteckningsreferens();
		registerbeteckningsreferensMock.setBeteckning("SUNDSVALL FILLA 8:185");
		registerbeteckningsreferensMock.setBeteckningsid("ny-4020855");
		registerbeteckningsreferensMock.setRegisterenhet("e19981ad-34b2-4e14-88f5-133f61ca85aa");

		when(registerbeteckningServiceMock.getRegisterbeteckningsreferens("TEST 1:1")).thenReturn(registerbeteckningsreferensMock);
		when(fbClientMock.getPropertyInfoByUuid(any(), any(), any(), any())).thenReturn(getPropertyInfoByUuidMockResponse);

		// Act
		assertThatThrownBy(() -> fbService.getPropertyInfoByPropertyDesignation(propertyDesignation))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: The specified propertyDesignation(TEST 1:1) could not be found")
			.hasFieldOrPropertyWithValue("status", BAD_REQUEST);
	}

	@Test
	void getPropertyOwnerByPropertyDesignation() {
		// Arrange
		final var personDTOMock = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, new ArrayList<>());
		final var organizationDTOMock = (OrganizationDTO) TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, new ArrayList<>());
		final var propertyDesignation = "TEST 1:1";
		final var propertyOwners = List.of(personDTOMock, organizationDTOMock);
		final var personDTOMockAddressDTO = personDTOMock.getAddresses().getFirst();

		// Mock
		mockFb();
		when(fbClientMock.getPropertyOwnerByFnr(any(), any(), any(), any())).thenReturn(createPropertyOwnerByFnrResponse(propertyOwners));
		when(fbClientMock.getPropertyOwnerInfoByUuid(any(), any(), any(), any())).thenReturn(createPropertyOwnerInfoByUuidResponse(propertyOwners));
		when(fbClientMock.getPropertyOwnerAddressByPersOrgNr(any(), any(), any(), any())).thenReturn(createPropertyOwnerAddressByPersOrgNrResponse(personDTOMockAddressDTO));

		// Act
		final var result = fbService.getPropertyOwnerByPropertyDesignation(propertyDesignation);

		// Assert
		assertThat(result).hasSize(2);

		final var personResult = (PersonDTO) result.getFirst();
		assertThat(personResult.getPersonalNumber()).isEqualTo(personDTOMock.getPersonalNumber());
		assertThat(personResult.getFirstName()).isEqualTo(personDTOMock.getFirstName());
		assertThat(personResult.getLastName()).isEqualTo(personDTOMock.getLastName());
		assertThat(personResult.getAddresses()).hasSize(1);

		final var organizationResult = (OrganizationDTO) result.get(1);
		assertThat(organizationResult.getOrganizationNumber()).isEqualTo(organizationDTOMock.getOrganizationNumber());
		assertThat(organizationResult.getOrganizationName()).isEqualTo(organizationDTOMock.getOrganizationName());
		assertThat(organizationResult.getAddresses()).hasSize(1);

		final var addressDTO = personResult.getAddresses().getFirst();
		assertThat(addressDTO.getAddressCategories().getFirst()).isEqualTo(AddressCategory.POSTAL_ADDRESS);
		assertThat(addressDTO.getStreet()).isEqualTo(personDTOMockAddressDTO.getStreet());
		assertThat(addressDTO.getCountry()).isEqualTo(personDTOMockAddressDTO.getCountry());
		assertThat(addressDTO.getCity()).isEqualTo(personDTOMockAddressDTO.getCity());
		assertThat(addressDTO.getPostalCode()).isEqualTo(personDTOMockAddressDTO.getPostalCode());
		assertThat(addressDTO.getCareOf()).isEqualTo(personDTOMockAddressDTO.getCareOf());
	}

	@Test
	void getPropertyOwnerByPropertyDesignationNoValidOrganisationName() {

		// Arrange
		final var organizationDTOMock = (OrganizationDTO) TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, emptyList());

		final var propertyDesignation = "TEST 1:1";
		final List<StakeholderDTO> propertyOwners = List.of(organizationDTOMock);
		final var propertyOwnerInfoByUuid = createPropertyOwnerInfoByUuidResponse(propertyOwners);
		propertyOwnerInfoByUuid.getData().getFirst().setGallandeOrganisationsnamn(null);

		// Mock
		mockFb();
		when(fbClientMock.getPropertyOwnerByFnr(any(), any(), any(), any())).thenReturn(createPropertyOwnerByFnrResponse(propertyOwners));
		when(fbClientMock.getPropertyOwnerInfoByUuid(any(), any(), any(), any())).thenReturn(propertyOwnerInfoByUuid);

		// Act
		final var result = fbService.getPropertyOwnerByPropertyDesignation(propertyDesignation);

		// Assert
		assertThat(result).isEmpty();
	}

	@Test
	void getPropertyOwnerByPropertyDesignationNoAdress() {
		// Arrange
		final var personDTOMock = (PersonDTO) TestUtil.createStakeholderDTO(StakeholderType.PERSON, emptyList());
		final var propertyDesignation = "TEST 1:1";
		final List<StakeholderDTO> propertyOwners = List.of(personDTOMock);

		// Mock
		mockFb();
		when(fbClientMock.getPropertyOwnerByFnr(any(), any(), any(), any())).thenReturn(createPropertyOwnerByFnrResponse(propertyOwners));
		when(fbClientMock.getPropertyOwnerInfoByUuid(any(), any(), any(), any())).thenReturn(createPropertyOwnerInfoByUuidResponse(propertyOwners));
		when(fbClientMock.getPropertyOwnerAddressByPersOrgNr(any(), any(), any(), any())).thenReturn(new ResponseDto());

		// Act
		final var result = fbService.getPropertyOwnerByPropertyDesignation(propertyDesignation);

		// Assert
		assertThat(result).hasSize(1);

		final var personResult = (PersonDTO) result.getFirst();
		assertThat(personResult.getPersonalNumber()).isEqualTo(personDTOMock.getPersonalNumber());
		assertThat(personResult.getFirstName()).isEqualTo(personDTOMock.getFirstName());
		assertThat(personResult.getLastName()).isEqualTo(personDTOMock.getLastName());
		assertThat(personResult.getAddresses()).isEmpty();
	}

	private void mockFb() {
		final var registerbeteckningsreferensMock = new Registerbeteckningsreferens();
		registerbeteckningsreferensMock.setBeteckning("SUNDSVALL FILLA 8:185");
		registerbeteckningsreferensMock.setBeteckningsid("ny-4020855");
		registerbeteckningsreferensMock.setRegisterenhet("e19981ad-34b2-4e14-88f5-133f61ca85aa");

		final var getPropertyInfoByUuidMockResponse = new ResponseDto();
		getPropertyInfoByUuidMockResponse.setData(List.of(new DataItem()));
		getPropertyInfoByUuidMockResponse.getData().getFirst().setFnr(TestUtil.FNR);

		final var getAddressInfoByUuidMockResponse = new ResponseDto();
		getAddressInfoByUuidMockResponse.setData(List.of(new DataItem()));
		getAddressInfoByUuidMockResponse.getData().getFirst().setGrupp(List.of(new GruppItem()));
		getAddressInfoByUuidMockResponse.getData().getFirst().getGrupp().getFirst().setAdressplatsId(ADRESSPLATS_ID);

		when(fbClientMock.getPropertyInfoByUuid(any(), any(), any(), any())).thenReturn(getPropertyInfoByUuidMockResponse);
		when(registerbeteckningServiceMock.getRegisterbeteckningsreferens("TEST 1:1")).thenReturn(registerbeteckningsreferensMock);
		when(fbClientMock.getAddressInfoByUuid(any(), any(), any(), any())).thenReturn(getAddressInfoByUuidMockResponse);

	}

	private static ResponseDto createPropertyOwnerAddressByPersOrgNrResponse(final AddressDTO personDTOMockAddressDTO) {
		final var dataItem = new DataItem();
		dataItem.setUtdelningsadress1(personDTOMockAddressDTO.getStreet());
		dataItem.setLand(personDTOMockAddressDTO.getCountry());
		dataItem.setPostort(personDTOMockAddressDTO.getCity());
		dataItem.setPostnummer(personDTOMockAddressDTO.getPostalCode());
		dataItem.setCoAdress(personDTOMockAddressDTO.getCareOf());

		final var getPropertyOwnerAddressByPersOrgNrMockResponse = new ResponseDto();
		getPropertyOwnerAddressByPersOrgNrMockResponse.setData(List.of(dataItem));
		return getPropertyOwnerAddressByPersOrgNrMockResponse;
	}

	private ResponseDto createPropertyOwnerByFnrResponse(final List<StakeholderDTO> propertyOwners) {
		final var responseDto = new ResponseDto();
		final var dataitem = new DataItem();
		responseDto.setData(List.of(dataitem));
		dataitem.setGrupp(propertyOwners.stream().map(propertyOwner -> {
			final var gruppItem = new GruppItem();
			if (propertyOwner instanceof final PersonDTO personDTO) {
				gruppItem.setIdentitetsnummer(personDTO.getPersonalNumber());
				gruppItem.setUuid(UUID.randomUUID().toString());
			} else if (propertyOwner instanceof final OrganizationDTO organizationDTO) {
				gruppItem.setIdentitetsnummer(organizationDTO.getOrganizationNumber());
				gruppItem.setUuid(UUID.randomUUID().toString());
			}
			return gruppItem;
		})
			.toList());
		return responseDto;
	}

	private ResponseDto createPropertyOwnerInfoByUuidResponse(final List<StakeholderDTO> propertyOwners) {
		final var responseDto = new ResponseDto();
		responseDto.setData(propertyOwners.stream().map(propertyOwner -> {
			final var dataItem = new DataItem();
			if (propertyOwner instanceof final PersonDTO personDTO) {
				dataItem.setGallandeFornamn(personDTO.getFirstName());
				dataItem.setGallandeEfternamn(personDTO.getLastName());
				dataItem.setIdentitetsnummer(personDTO.getPersonalNumber());
				dataItem.setJuridiskForm(Constants.FB_JURIDISK_FORM_PRIVATPERSON);
			} else if (propertyOwner instanceof final OrganizationDTO organizationDTO) {
				dataItem.setGallandeOrganisationsnamn(organizationDTO.getOrganizationName());
				dataItem.setIdentitetsnummer(organizationDTO.getOrganizationNumber());
				dataItem.setJuridiskForm("16");
			}
			return dataItem;
		}).toList());
		return responseDto;
	}
}
