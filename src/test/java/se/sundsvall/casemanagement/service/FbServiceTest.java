package se.sundsvall.casemanagement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.ADRESSPLATS_ID;
import static se.sundsvall.casemanagement.TestUtil.FNR;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.integration.fb.FbClient;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;

@ExtendWith(MockitoExtension.class)
class FbServiceTest {

	@InjectMocks
	private FbService fbService;

	@Mock
	private FbClient fbClientMock;

	@Mock
	private RegisterbeteckningService registerbeteckningServiceMock;

	@NotNull
	private static ResponseDto getResponseDto(final AddressDTO personDTOMockAddressDTO) {
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

	@Test
	void testGetPropertyInfoByPropertyDesignation() {
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
	void testGetPropertyInfoByPropertyDesignationNotFound() {
		// Arrange
		final var propertyDesignation = "TEST 1:1";
		// Act & Assert
		assertThatThrownBy(() -> fbService.getPropertyInfoByPropertyDesignation(propertyDesignation))
			.isInstanceOf(ThrowableProblem.class)
			.hasMessage("Bad Request: The specified propertyDesignation(TEST 1:1) could not be found")
			.hasFieldOrPropertyWithValue("status", Status.BAD_REQUEST);
	}

	@Test
	void testGetPropertyOwnerByPropertyDesignation() {
		// Arrange
		final var personDTOMock = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, new ArrayList<>());
		final var propertyDesignation = "TEST 1:1";

		TestUtil.mockFbPropertyOwners(fbClientMock, List.of(personDTOMock));

		mockFb();

		final var personDTOMockAddressDTO = personDTOMock.getAddresses().getFirst();

		final var getPropertyOwnerAddressByPersOrgNrMockResponse = getResponseDto(personDTOMockAddressDTO);

		//Mock
		when(fbClientMock.getPropertyOwnerAddressByPersOrgNr(any(), any(), any(), any())).thenReturn(getPropertyOwnerAddressByPersOrgNrMockResponse);

		// Act
		final var result = fbService.getPropertyOwnerByPropertyDesignation(propertyDesignation);

		// Assert
		assertThat(result).hasSize(1);

		final var personResult = (PersonDTO) result.getFirst();
		assertThat(personResult.getPersonalNumber()).isEqualTo(personDTOMock.getPersonalNumber());
		assertThat(personResult.getFirstName()).isEqualTo(personDTOMock.getFirstName());
		assertThat(personResult.getLastName()).isEqualTo(personDTOMock.getLastName());
		assertThat(personResult.getAddresses()).hasSize(1);

		final var addressDTO = personResult.getAddresses().getFirst();
		assertThat(addressDTO.getAddressCategories().getFirst()).isEqualTo(AddressCategory.POSTAL_ADDRESS);
		assertThat(addressDTO.getStreet()).isEqualTo(personDTOMockAddressDTO.getStreet());
		assertThat(addressDTO.getCountry()).isEqualTo(personDTOMockAddressDTO.getCountry());
		assertThat(addressDTO.getCity()).isEqualTo(personDTOMockAddressDTO.getCity());
		assertThat(addressDTO.getPostalCode()).isEqualTo(personDTOMockAddressDTO.getPostalCode());
		assertThat(addressDTO.getCareOf()).isEqualTo(personDTOMockAddressDTO.getCareOf());
	}

	private void mockFb() {
		final var registerbeteckningsreferensMock = new Registerbeteckningsreferens();
		registerbeteckningsreferensMock.setBeteckning("SUNDSVALL FILLA 8:185");
		registerbeteckningsreferensMock.setBeteckningsid("ny-4020855");
		registerbeteckningsreferensMock.setRegisterenhet("e19981ad-34b2-4e14-88f5-133f61ca85aa");

		final ResponseDto getPropertyInfoByUuidMockResponse = new ResponseDto();
		getPropertyInfoByUuidMockResponse.setData(List.of(new DataItem()));
		getPropertyInfoByUuidMockResponse.getData().getFirst().setFnr(FNR);

		final ResponseDto getAddressInfoByUuidMockResponse = new ResponseDto();
		getAddressInfoByUuidMockResponse.setData(List.of(new DataItem()));
		getAddressInfoByUuidMockResponse.getData().getFirst().setGrupp(List.of(new GruppItem()));
		getAddressInfoByUuidMockResponse.getData().getFirst().getGrupp().getFirst().setAdressplatsId(ADRESSPLATS_ID);

		when(fbClientMock.getPropertyInfoByUuid(any(), any(), any(), any())).thenReturn(getPropertyInfoByUuidMockResponse);
		when(registerbeteckningServiceMock.getRegisterbeteckningsreferens("TEST 1:1")).thenReturn(registerbeteckningsreferensMock);
		when(fbClientMock.getAddressInfoByUuid(any(), any(), any(), any())).thenReturn(getAddressInfoByUuidMockResponse);

	}

}
