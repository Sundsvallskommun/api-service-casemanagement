package se.sundsvall.casemanagement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static se.sundsvall.casemanagement.TestUtil.ADRESSPLATS_ID;
import static se.sundsvall.casemanagement.TestUtil.FNR;

import java.util.ArrayList;
import java.util.List;

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
import se.sundsvall.casemanagement.util.Constants;

@ExtendWith(MockitoExtension.class)
class FbServiceTest {

	@InjectMocks
	private FbService fbService;

	@Mock
	private FbClient fbClientMock;

	@Mock
	private RegisterbeteckningService registerbeteckningServiceMock;

	@Test
	void testGetPropertyInfoByPropertyDesignation() {
		final String propertyDesignation = "TEST 1:1";

		mockFb(propertyDesignation);

		final var result = fbService.getPropertyInfoByPropertyDesignation(propertyDesignation);
		assertEquals(FNR, result.getFnr());
		assertEquals(ADRESSPLATS_ID, result.getAdressplatsId());
	}

	@Test
	void testGetPropertyInfoByPropertyDesignationNotFound() {
		final String propertyDesignation = "TEST 1:1";
		final var problem = assertThrows(ThrowableProblem.class, () -> fbService.getPropertyInfoByPropertyDesignation(propertyDesignation));
		assertEquals(Status.BAD_REQUEST, problem.getStatus());
		assertEquals(String.format(Constants.ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND, propertyDesignation), problem.getDetail());
	}

	@Test
	void testGetPropertyOwnerByPropertyDesignation() {
		final PersonDTO personDTOMock = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, new ArrayList<>());
		TestUtil.mockFbPropertyOwners(fbClientMock, List.of(personDTOMock));

		final String propertyDesignation = "TEST 1:1";
		mockFb(propertyDesignation);

		final ResponseDto getPropertyOwnerAddressByPersOrgNrMockResponse = new ResponseDto();
		final DataItem dataItem = new DataItem();
		final AddressDTO personDTOMockAddressDTO = personDTOMock.getAddresses().get(0);
		dataItem.setUtdelningsadress1(personDTOMockAddressDTO.getStreet());
		dataItem.setLand(personDTOMockAddressDTO.getCountry());
		dataItem.setPostort(personDTOMockAddressDTO.getCity());
		dataItem.setPostnummer(personDTOMockAddressDTO.getPostalCode());
		dataItem.setCoAdress(personDTOMockAddressDTO.getCareOf());
		getPropertyOwnerAddressByPersOrgNrMockResponse.setData(List.of(dataItem));
		lenient().doReturn(getPropertyOwnerAddressByPersOrgNrMockResponse).when(fbClientMock).getPropertyOwnerAddressByPersOrgNr(any(), any(), any(), any());

		final var result = fbService.getPropertyOwnerByPropertyDesignation(propertyDesignation);
		assertEquals(1, result.size());

		final PersonDTO personResult = (PersonDTO) result.get(0);
		assertEquals(personDTOMock.getPersonalNumber(), personResult.getPersonalNumber());
		assertEquals(personDTOMock.getFirstName(), personResult.getFirstName());
		assertEquals(personDTOMock.getLastName(), personResult.getLastName());
		assertEquals(1, personResult.getAddresses().size());
		final AddressDTO addressDTO = personResult.getAddresses().get(0);
		assertEquals(AddressCategory.POSTAL_ADDRESS, addressDTO.getAddressCategories().get(0));
		assertEquals(personDTOMockAddressDTO.getStreet(), addressDTO.getStreet());
		assertEquals(personDTOMockAddressDTO.getCountry(), addressDTO.getCountry());
		assertEquals(personDTOMockAddressDTO.getCity(), addressDTO.getCity());
		assertEquals(personDTOMockAddressDTO.getPostalCode(), addressDTO.getPostalCode());
		assertEquals(personDTOMockAddressDTO.getCareOf(), addressDTO.getCareOf());
	}

	private void mockFb(String propertyDesignation) {
		final Registerbeteckningsreferens registerbeteckningsreferensMock = new Registerbeteckningsreferens();
		registerbeteckningsreferensMock.setBeteckning("SUNDSVALL FILLA 8:185");
		registerbeteckningsreferensMock.setBeteckningsid("ny-4020855");
		registerbeteckningsreferensMock.setRegisterenhet("e19981ad-34b2-4e14-88f5-133f61ca85aa");

		doReturn(registerbeteckningsreferensMock).when(registerbeteckningServiceMock).getRegisterbeteckningsreferens(propertyDesignation);

		final ResponseDto getPropertyInfoByUuidMockResponse = new ResponseDto();
		getPropertyInfoByUuidMockResponse.setData(List.of(new DataItem()));
		getPropertyInfoByUuidMockResponse.getData().get(0).setFnr(FNR);
		doReturn(getPropertyInfoByUuidMockResponse).when(fbClientMock).getPropertyInfoByUuid(any(), any(), any(), any());

		final ResponseDto getAddressInfoByUuidMockResponse = new ResponseDto();
		getAddressInfoByUuidMockResponse.setData(List.of(new DataItem()));
		getAddressInfoByUuidMockResponse.getData().get(0).setGrupp(List.of(new GruppItem()));
		getAddressInfoByUuidMockResponse.getData().get(0).getGrupp().get(0).setAdressplatsId(ADRESSPLATS_ID);
		doReturn(getAddressInfoByUuidMockResponse).when(fbClientMock).getAddressInfoByUuid(any(), any(), any(), any());

	}
}
